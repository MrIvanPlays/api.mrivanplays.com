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
package com.mrivanplays.siteapi.handlers;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.mrivanplays.siteapi.utils.Resource;
import com.mrivanplays.siteapi.utils.Utils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpigotDownloadHandler implements HttpHandler {

    private WebClient webClient;

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

        for (HttpCookie temp : Objects.requireNonNull(Utils.getCookies("https://www.google.com"))) {
            Cookie cookie = new Cookie(temp.getDomain(), temp.getName(), temp.getValue());
            webClient.getCookieManager().addCookie(cookie);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // todo: improve
        String resourceId = exchange.getRequestURI().toString().replace("/spigot/download/", "");
        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/octec-stream");
        headers.add("Content-Disposition", "attachment;filename=" + resourceId + ".jar");

        exchange.sendResponseHeaders(200, 0);
        try (OutputStream out = exchange.getResponseBody()) {

            Resource resource = new Resource(resourceId);

            try (InputStream in = getInputStream(resource.getDownloadUrl())) {
                byte[] buffer = new byte[in.available()];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
            }
        }

        exchange.getResponseBody().close();
    }

    private InputStream getInputStream(String url) throws IOException {
        WebRequest wr = new WebRequest(new URL(url), HttpMethod.GET);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return webClient.getPage(wr).getEnclosingWindow().getEnclosedPage().getWebResponse().getContentAsStream();
    }
}
