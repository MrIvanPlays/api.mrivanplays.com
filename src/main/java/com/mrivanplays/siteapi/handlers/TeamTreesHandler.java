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
import java.net.SocketTimeoutException;
import okhttp3.Call;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import spark.Request;
import spark.Response;
import spark.Route;

public class TeamTreesHandler implements Route {

  @Override
  public Object handle(Request request, Response response) throws Exception {
    response.type("text");

    String rawParam = request.queryParams("raw");
    boolean raw = false;
    if (rawParam != null) {
      raw = Boolean.parseBoolean(rawParam);
    }

    Call call = Utils.call("https://teamtrees.org/");
    try (okhttp3.Response okHttpResponse = call.execute()) {
      if (okHttpResponse.code() == 503) {
        response.status(503);
        return error(503, "teamtrees.org is being overloaded with traffic");
      }
      response.status(200);
      Document document = Jsoup.parse(okHttpResponse.body().string());
      String count = document.selectFirst("div.counter").attr("data-count");
      if (raw) {
        return count;
      }

      ObjectNode node = new ObjectNode(Utils.objectMapper.getNodeFactory());
      node.put("success", true);
      node.put("trees", count);
      return node.toString();
    } catch (SocketTimeoutException e) {
      response.status(408);
      return error(408, "teamtrees.org timed out");
    }
  }

  private String error(int code, String message) {
    ObjectNode node = new ObjectNode(Utils.objectMapper.getNodeFactory());
    node.put("success", false);
    node.put("error", code);
    node.put("message", message);
    return node.toString();
  }
}
