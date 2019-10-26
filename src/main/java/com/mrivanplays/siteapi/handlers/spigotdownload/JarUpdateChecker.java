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
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

public class JarUpdateChecker {

    public static File jarsFolder = new File("./usr/share/nginx/siteapi/spigotdownload/");

    public JarUpdateChecker() {
        if (!jarsFolder.exists()) {
            jarsFolder.mkdirs();
        }
    }

    /**
     * @return all jars that should be updated
     */
    public List<UpdateResponse> checkForUpdates() throws IOException {
        List<UpdateResponse> updateNeeded = new ArrayList<>();
        File[] files = jarsFolder.listFiles((dir, name) -> name.endsWith(".jar") || name.endsWith(".zip"));
        if (files == null || files.length == 0) {
            return updateNeeded; // empty list
        }
        for (File file : files) {
            String fullName = file.getName();
            String fileType = fullName.substring(fullName.indexOf('.') + 1);
            String id = fullName.replace(fileType, "");
            String requestURL = "https://api.spigotmc.org/legacy/update.php?resource=%s";
            Call call = Utils.call(String.format(requestURL, id));
            try (Response response = call.execute()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                String spigotVersion = reader.readLine();
                reader.close();
                File resourceJsonFile = new File(jarsFolder, id + ".json");
                Map<String, String> resourceJson = Utils.readResourceJsonIfExisting(resourceJsonFile);
                String jarVersion = resourceJson.get("version");
                if (!jarVersion.equalsIgnoreCase(spigotVersion)) {
                    updateNeeded.add(new UpdateResponse(file, resourceJsonFile, id, fileType));
                }
            }
        }
        return updateNeeded;
    }

    public static class UpdateResponse {

        private File file;
        private File resourceJsonFile;
        private String fileType;
        private String resourceId;

        public UpdateResponse(File file, File resourceJsonFile, String fileType, String resourceId) {
            this.file = file;
            this.fileType = fileType;
            this.resourceJsonFile = resourceJsonFile;
            this.resourceId = resourceId;
        }

        public File getFile() {
            return file;
        }

        public File getResourceJsonFile() {
            return resourceJsonFile;
        }

        public String getFileType() {
            return fileType;
        }

        public String getResourceId() {
            return resourceId;
        }
    }
}
