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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mrivanplays.siteapi.utils.Resource;
import com.mrivanplays.siteapi.utils.Utils;
import com.mrivanplays.siteapi.handlers.spigotdownload.JarUpdateChecker.UpdateResponse;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

public class UpdateCheckRunnable implements Runnable {

    private JarUpdateChecker updateChecker;
    private SpigotDownloadHandler spigotDownloadHandler;

    public UpdateCheckRunnable(JarUpdateChecker updateChecker, SpigotDownloadHandler spigotDownloadHandler) {
        this.updateChecker = updateChecker;
        this.spigotDownloadHandler = spigotDownloadHandler;
    }

    @Override
    public void run() {
        try {
            List<UpdateResponse> updateNeeded = updateChecker.checkForUpdates();
            for (UpdateResponse response : updateNeeded) {
                String resourceId = response.getResourceId();
                Resource resource = Utils.resource(resourceId);
                if (resource.getFileType().equalsIgnoreCase("Via external site")) {
                    response.getFile().delete();
                    return;
                }
                if (!spigotDownloadHandler.nameMatcher.matcher(resource.getName()).matches()) {
                    response.getFile().delete();
                    response.getResourceJsonFile().delete();
                    return;
                }
                File file;
                if (!resource.getFileType().equalsIgnoreCase(response.getFileType())) {
                    response.getFile().delete();
                    file = new File(JarUpdateChecker.jarsFolder, resourceId + resource.getFileType());
                } else {
                    File responseFile = response.getFile();
                    responseFile.delete();
                    file = responseFile;
                }
                file.createNewFile();
                File resourceJsonFile = response.getResourceJsonFile();
                resourceJsonFile.delete();
                resourceJsonFile.createNewFile();
                ObjectNode objectNode = new ObjectNode(Utils.objectMapper.getNodeFactory());
                objectNode.put("name", resource.getName());
                objectNode.put("fileType", resource.getFileType());
                objectNode.put("version", resource.getVersion());
                try (Writer writer = new FileWriter(resourceJsonFile)) {
                    writer.write(objectNode.toString());
                }
                try (InputStream in = spigotDownloadHandler.getInputStream(resource.getDownloadUrl())) {
                    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                        spigotDownloadHandler.write(in, out);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
