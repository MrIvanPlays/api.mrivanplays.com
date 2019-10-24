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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.mrivanplays.siteapi.utils.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletOutputStream;

import spark.Request;
import spark.Response;
import spark.Route;

public class SpigotDownloadHandler implements Route {

    private WebClient webClient;
    private boolean requestGoingOn = false;

    public SpigotDownloadHandler() {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setTimeout(15000);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        List<HttpCookie> googleCookies = Utils.getCookies("https://www.google.com");

        if (!googleCookies.isEmpty()) {
            for (HttpCookie temp : googleCookies) {
                Cookie cookie = new Cookie(temp.getDomain(), temp.getName(), temp.getValue());
                webClient.getCookieManager().addCookie(cookie);
            }
        }

        // these cookies are very important!!!
        List<HttpCookie> cloudflareCookies = Utils.getCookies("https://www.cloudflare.com");
        if (!cloudflareCookies.isEmpty()) {
            for (HttpCookie temp : cloudflareCookies) {
                Cookie cookie = new Cookie(temp.getDomain(), temp.getName(), temp.getValue());
                webClient.getCookieManager().addCookie(cookie);
            }
        }

        JarUpdateChecker updateChecker = new JarUpdateChecker();
        UpdateCheckRunnable runnable = new UpdateCheckRunnable(updateChecker, this);
        Utils.executor.scheduleAtFixedRate(() -> {
            if (!requestGoingOn) {
                runnable.run();
            } else {
                Utils.executor.schedule(runnable, 1, TimeUnit.MINUTES);
            }
        }, 30, 30, TimeUnit.MINUTES);
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        requestGoingOn = true;
        String resourceId = request.params(":id");
        response.type("application/octec-stream");
        response.header("Content-Disposition", "attachment;filename=" + resourceId + ".jar");
        response.status(200);

        File file = new File(JarUpdateChecker.jarsFolder, resourceId + ".jar");
        if (!file.exists()) {
            file.createNewFile();
            try (InputStream in = getInputStream(Utils.downloadLink(resourceId))) {
                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                    write(in, out);
                }
            }
        }

        try (ServletOutputStream out = response.raw().getOutputStream()) {
            try (InputStream in = new FileInputStream(file)) {
                write(in, out);
            }
        }
        requestGoingOn = false;

        return null;
    }

    InputStream getInputStream(String url) throws IOException {
        WebRequest wr = new WebRequest(new URL(url), HttpMethod.GET);
        return webClient.getPage(wr).getEnclosingWindow().getEnclosedPage().getWebResponse().getContentAsStream();
    }

    void write(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[in.available()];
        int count;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
    }
}
