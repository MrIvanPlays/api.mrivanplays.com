/*
    Copyright (c) 2019 Ivan Pekov
    Copyright (c) 2019 Contributors

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/
package com.mrivanplays.siteapi.handlers.spigotdownload;

import com.mrivanplays.siteapi.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Response;

public class JarUpdateChecker {

    public static File jarsFolder = new File("./usr/share/nginx/siteapi/spigotdownload/");

    private final String requestURL = "https://api.spigotmc.org/legacy/update.php?resource=%s";

    public JarUpdateChecker() {
        if (!jarsFolder.exists()) {
            jarsFolder.mkdirs();
        }
    }

    /**
     * @return all jars that should be updated
     */
    public List<String> checkForUpdates() throws IOException {
        List<String> updateNeeded = new ArrayList<>();
        File[] files = jarsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null || files.length == 0) {
            return updateNeeded; // empty list
        }
        for (File file : files) {
            String id = file.getName().replace(".jar", "");
            Call call = Utils.call(String.format(requestURL, id));
            try (Response response = call.execute()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                String spigotVersion = reader.readLine();
                reader.close();
                String jarVersion = retrieveJarVersion(file);
                if (jarVersion.equalsIgnoreCase("exec file delete")) {
                    file.delete();
                    continue;
                }
                if (!jarVersion.equalsIgnoreCase(spigotVersion)) {
                    updateNeeded.add(id);
                }
            }
        }
        return updateNeeded;
    }

    private String retrieveJarVersion(File file) throws IOException {
        JarFile jar = new JarFile(file);
        JarEntry pluginDotYml = jar.getJarEntry("plugin.yml");
        if (pluginDotYml == null) {
            // not a plugin (perhaps skript or a standalone program), delete the file
            return "exec file delete";
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(jar.getInputStream(pluginDotYml)))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            String version = "";
            for (String line : lines) {
                String[] split = line.split(":");
                if (split[0].equalsIgnoreCase("version")) {
                    version = split[1];
                    break;
                }
            }
            lines.clear();
            return version;
        }
    }
}
