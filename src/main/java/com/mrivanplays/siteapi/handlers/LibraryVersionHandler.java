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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import okhttp3.Call;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import spark.Request;
import spark.Response;
import spark.Route;

public class LibraryVersionHandler implements Route {

  @Override
  public Object handle(Request request, Response response) throws Exception {
    response.type("text");

    String dependency = request.params(":id");

    String repoUrl = request.queryParams("repoUrl");
    if (repoUrl == null) {
      repoUrl = "https://repo.mrivanplays.com/repository/ivan/";
    }
    if (repoUrl.equalsIgnoreCase("mavenCentral")) {
      repoUrl = "https://repo1.maven.org/maven2/";
    }

    String rawGroupId = request.queryParams("groupId");
    if (rawGroupId == null) {
      rawGroupId = "com/mrivanplays";
    }

    String rawParam = request.queryParams("raw");
    boolean raw = false;
    if (rawParam != null) {
      raw = Boolean.parseBoolean(rawParam);
    }

    String groupId = rawGroupId.replace(".", "/") + "/";

    String requestUrl = repoUrl + groupId + dependency + "/maven-metadata.xml";

    Call call = Utils.call(requestUrl);
    try (okhttp3.Response okHttpResponse = call.execute()) {
      if (okHttpResponse.code() == 404) {
        response.status(404);
        return notFound().toString();
      }

      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(okHttpResponse.body().byteStream()))) {
        List<String> lines = reader.lines().collect(Collectors.toList());
        String buffer = Utils.inlineXML(lines);
        lines.clear();
        Document document = Jsoup.parse(buffer);
        Element version = document.selectFirst("latest");
        if (version == null) {
          response.status(404);
          return notFound().toString();
        }
        response.status(200);
        if (raw) {
          return version.text();
        }
        ObjectNode objectNode = new ObjectNode(Utils.objectMapper.getNodeFactory());
        objectNode.put("success", true);
        objectNode.put("version", version.text());
        return objectNode.toString();
      }
    }
  }

  private ObjectNode notFound() {
    ObjectNode objectNode = new ObjectNode(Utils.objectMapper.getNodeFactory());
    objectNode.put("success", false);
    objectNode.put("error", 404);
    objectNode.put("message", "Dependency not found");
    return objectNode;
  }
}
