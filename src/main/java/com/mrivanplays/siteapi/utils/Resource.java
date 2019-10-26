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
package com.mrivanplays.siteapi.utils;

public class Resource {

    private String downloadUrl;
    private String resourceId;
    private String version;
    private String fileType;
    private String name;
    private boolean premium;

    public Resource(String downloadUrl, String resourceId, String version, String fileType, String name) {
        this.downloadUrl = downloadUrl;
        this.resourceId = resourceId;
        this.version = version;
        this.fileType = fileType;
        this.name = name;
    }

    public Resource(boolean premium) {
        this(null, null, null, null, null);
        this.premium = premium;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getVersion() {
        return version;
    }

    public String getFileType() {
        return fileType;
    }

    public String getName() {
        return name;
    }

    public boolean isPremium() {
        return premium;
    }
}
