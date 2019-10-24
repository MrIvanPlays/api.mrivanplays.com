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
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import spark.Request;
import spark.Response;
import spark.Route;

public class MemeHandler implements Route {

    private List<MemeRequests> requests;
    private ScheduledExecutorService executor;

    public MemeHandler() {
        requests = new ArrayList<>();
        executor = Utils.executor;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        response.type("text");

        MemeRequestsData memeRequestsData = get(request.ip());
        LocalTime timeNow = LocalTime.now();
        if (memeRequestsData.existed) {
            MemeRequests memeRequests = memeRequestsData.request;
            Duration duration = Duration.between(memeRequests.firstRequestAt, timeNow);
            if (duration.toMinutes() < 1) {
                if (memeRequests.requestsLastMinute == 100) {
                    response.status(Utils.ratelimitErrorCode);
                    long tryAgainAfter = (60 - duration.getSeconds()) + 2;
                    ObjectNode jsonResponse = new ObjectNode(Utils.objectMapper.getNodeFactory());
                    jsonResponse.put("success", false);
                    jsonResponse.put("error", Utils.ratelimitErrorCode);
                    jsonResponse.put("message", "Rate limit for a minute exceeded. Try again after " + tryAgainAfter + " seconds.");

                    return jsonResponse.toString();
                } else {
                    requests.remove(memeRequests);
                    int requestsLastMinute = memeRequests.requestsLastMinute;
                    memeRequests.requestsLastMinute = requestsLastMinute + 1;
                    requests.add(memeRequests);
                }
            }
        }

        Call call = Utils.request("https://reddit.com/r/meme/random.json");
        try (okhttp3.Response okHttpResponse = call.execute()) {
            response.status(200);

            JsonNode node = Utils.objectMapper.readTree(new InputStreamReader(okHttpResponse.body().byteStream()));
            ObjectNode jsonResponse = new ObjectNode(Utils.objectMapper.getNodeFactory());
            jsonResponse.put("success", true);
            jsonResponse.put("image", node.get(0).with("data").withArray("children").get(0).with("data").get("url").asText());

            return jsonResponse.toString();
        }
    }

    private MemeRequestsData get(String ip) {
        Optional<MemeRequests> search = requests.stream().filter(request -> request.ip.equalsIgnoreCase(ip)).findFirst();
        if (search.isPresent()) {
            return new MemeRequestsData(true, search.get());
        } else {
            MemeRequests request = new MemeRequests(ip, 1, LocalTime.now());
            requests.add(request);
            executor.schedule(() -> {
                MemeRequests newRequest = requests.stream().filter(r -> r.ip.equalsIgnoreCase(ip)).findFirst().get();
                requests.remove(newRequest);
            }, 1, TimeUnit.MINUTES);
            return new MemeRequestsData(false, request);
        }
    }

    private static class MemeRequests {
        String ip;
        int requestsLastMinute;
        LocalTime firstRequestAt;

        MemeRequests(String ip, int requestsLastMinute, LocalTime firstRequestAt) {
            this.ip = ip;
            this.requestsLastMinute = requestsLastMinute;
            this.firstRequestAt = firstRequestAt;
        }
    }

    private static class MemeRequestsData {
        boolean existed;
        MemeRequests request;

        MemeRequestsData(boolean existed, MemeRequests request) {
            this.existed = existed;
            this.request = request;
        }
    }
}
