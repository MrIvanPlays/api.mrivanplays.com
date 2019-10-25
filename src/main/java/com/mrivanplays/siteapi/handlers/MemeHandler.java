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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mrivanplays.siteapi.utils.Utils;

import java.io.InputStreamReader;

import okhttp3.Call;
import spark.Request;
import spark.Response;
import spark.Route;

public class MemeHandler implements Route {

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("text");
        response.status(200);

        Call call = Utils.call("https://reddit.com/r/meme/random.json");
        try (okhttp3.Response okHttpResponse = call.execute()) {
            JsonNode node = Utils.objectMapper.readTree(new InputStreamReader(okHttpResponse.body().byteStream()));
            ObjectNode jsonResponse = new ObjectNode(Utils.objectMapper.getNodeFactory());
            jsonResponse.put("image", node.get(0).with("data").withArray("children").get(0).with("data").get("url").asText());

            return jsonResponse.toString();
        }
    }
}
