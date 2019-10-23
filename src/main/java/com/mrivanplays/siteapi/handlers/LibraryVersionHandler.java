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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mrivanplays.siteapi.utils.Utils;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import okhttp3.Call;
import spark.Request;
import spark.Response;
import spark.Route;

public class LibraryVersionHandler implements Route {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("text");

        String dependency = request.params(":id");

        okhttp3.Request okHttpRequest = new okhttp3.Request.Builder()
                .url("https://repo.mrivanplays.com/repository/ivan/com/mrivanplays/" + dependency + "/maven-metadata.xml")
                .header("User-Agent", Utils.userAgent)
                .build();
        Call call = Utils.okHttpClient.newCall(okHttpRequest);
        try (okhttp3.Response okHttpResponse = call.execute()) {
            if (okHttpResponse.code() == 404) {
                response.status(404);
                ObjectNode objectNode = new ObjectNode(Utils.objectMapper.getNodeFactory());
                objectNode.put("error", 404);
                objectNode.put("message", "Dependency not found on nexus");
                return objectNode.toString();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(okHttpResponse.body().byteStream()))) {
                List<String> lines = reader.lines().collect(Collectors.toList());
                response.status(200);
                StringBuilder bufferBuilder = new StringBuilder();
                bufferBuilder.append("<?xml version=\"1.0\"?>").append("\n");
                for (String line : lines) {
                    bufferBuilder.append(line).append("\n");
                }
                String buffer = bufferBuilder.toString();
                try {
                    // todo: make DOM recognize xml
                    DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    InputSource inputSource = new InputSource();
                    inputSource.setCharacterStream(new StringReader(buffer));
                    Document doc = documentBuilder.parse(inputSource);
                } catch (ParserConfigurationException | SAXException e) {
                    e.printStackTrace();
                }
                return "Currently in making stage.";
            }
        }
    }
}
