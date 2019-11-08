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
import com.mrivanplays.teamtreesclient.Donation;
import com.mrivanplays.teamtreesclient.FullGoalData;
import com.mrivanplays.teamtreesclient.SiteResponse;
import com.mrivanplays.teamtreesclient.TeamTreesClient;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import spark.Request;
import spark.Response;
import spark.Route;

public class TeamTreesHandler implements Route {

  private TeamTreesClient client;
  private ObjectNode rateLimitError;
  private Map<String, Integer> currentRateLimit;

  public TeamTreesHandler() {
    client = new TeamTreesClient();
    rateLimitError = new ObjectNode(Utils.objectMapper.getNodeFactory());
    rateLimitError.put("success", false);
    rateLimitError.put("error", 80);
    rateLimitError.put("message", "Rate limit exceeded");
    currentRateLimit = new HashMap<>();
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    executor.scheduleAtFixedRate(() -> {
      if (!currentRateLimit.isEmpty()) {
        currentRateLimit.clear();
      }
    }, 30, 30, TimeUnit.SECONDS);
  }

  @Override
  public Object handle(Request request, Response response) throws Exception {
    response.type("text");
    String requesterIp = request.ip();
    if (!currentRateLimit.containsKey(requesterIp)) {
      currentRateLimit.put(requesterIp, 1);
    } else {
      int currentRequests = currentRateLimit.get(requesterIp);
      if (currentRequests > 30) {
        response.status(80);
        return rateLimitError.toString();
      }
      currentRateLimit.replace(requesterIp, currentRequests + 1);
    }
    SiteResponse<FullGoalData> siteResponse = client.retrieveFullData().join();
    if (siteResponse.getResponseCode() == 408) {
      response.status(408);
      return error(408, "teamtrees.org timed out");
    }
    if (siteResponse.getResponseCode() == 503) {
      response.status(503);
      return error(503, "teamtrees.org is being overloaded with traffic");
    }
    Optional<FullGoalData> dataOptional = siteResponse.getData();
    if (dataOptional.isPresent()) {
      response.status(200);
      FullGoalData data = dataOptional.get();
      ObjectNode node = new ObjectNode(Utils.objectMapper.getNodeFactory());
      node.put("success", true);
      node.put("trees", data.getTrees());
      node.put("treesLeft", data.getTreesLeft());
      node.put("percentDone", data.getPercentDone());
      node.put("daysLeft", data.getDaysLeft());
      Donation lastDon = data.getMostRecentDonation();
      ObjectNode lastDonation = new ObjectNode(Utils.objectMapper.getNodeFactory());
      lastDonation.put("nameFrom", lastDon.getName());
      lastDonation.put("treesDonated", lastDon.getTreesDonated());
      lastDonation.put("donatedAt", lastDon.getDateAt());
      lastDonation.put("message", lastDon.getMessage());
      node.set("lastDonation", lastDonation);
      Donation topDon = data.getTopDonation();
      ObjectNode topDonation = new ObjectNode(Utils.objectMapper.getNodeFactory());
      topDonation.put("nameFrom", topDon.getName());
      topDonation.put("treesDonated", topDon.getTreesDonated());
      topDonation.put("donatedAt", topDon.getDateAt());
      topDonation.put("message", topDon.getMessage());
      node.set("topDonation", topDonation);
      return node.toString();
    } else {
      response.status(400);
      return error(400, "Other error occurred while trying to retrieve data");
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
