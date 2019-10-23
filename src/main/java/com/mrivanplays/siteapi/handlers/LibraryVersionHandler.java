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

import com.google.gson.JsonObject;

import com.mrivanplays.siteapi.utils.Utils;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class LibraryVersionHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "text");

        String dependency = exchange.getRequestURI().toString().replace("/dependency/version/", "");

        URL url = new URL("https://repo.mrivanplays.com/repository/ivan/com/mrivanplays/" + dependency + "/maven-metadata.xml");
        HttpURLConnection connection = Utils.openConnection(url);
        if (connection.getResponseCode() == 404) {
            exchange.sendResponseHeaders(404, 0);
            JsonObject response = new JsonObject();
            response.addProperty("error", 404);
            response.addProperty("message", "Dependency not found on nexus");
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response.toString().getBytes());
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            exchange.sendResponseHeaders(200, 0);
            StringBuilder bufferBuilder = new StringBuilder();
            bufferBuilder.append("<?xml version=\"1.0\"?>").append("\n");
            for (String line : lines) {
                bufferBuilder.append(line).append("\n");
            }
            String buffer = bufferBuilder.toString();
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputSource inputSource = new InputSource();
                inputSource.setCharacterStream(new StringReader(buffer));
                Document doc = documentBuilder.parse(inputSource);
                // todo: make DOM read my xml
            } catch (ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            }
        }
    }
}
