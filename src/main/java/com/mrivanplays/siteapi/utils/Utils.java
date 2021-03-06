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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Utils {

  private static String userAgent =
      "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1";
  private static CookieManager cookieManager;
  public static OkHttpClient okHttpClient;
  public static ObjectMapper objectMapper;
  public static ScheduledExecutorService executor;

  static {
    executor = Executors.newScheduledThreadPool(1);
    okHttpClient = new OkHttpClient.Builder().dispatcher(new Dispatcher(executor)).build();
    cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    CookieHandler.setDefault(cookieManager);
    objectMapper = new ObjectMapper();
  }

  public static List<HttpCookie> getCookies(String urlName) {
    try {
      URL url = new URL(urlName);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.addRequestProperty("User-Agent", userAgent);
      connection.getContent();

      CookieStore cookieJar = cookieManager.getCookieStore();
      return cookieJar.getCookies();
    } catch (IOException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  public static Call call(String url) {
    Request request = new Request.Builder().url(url).header("User-Agent", userAgent).build();
    return okHttpClient.newCall(request);
  }

  public static Resource resource(String spigotId) throws IOException {
    Call call = call("https://spigotmc.org/resources/" + spigotId);
    try (Response response = call.execute()) {
      if (response.code() == 404) {
        return null;
      }
      Document document = Jsoup.parse(response.body().string());
      Element redirect = document.selectFirst("a.inner[href]");
      if (redirect == null) {
        return new Resource(true);
      }
      String downloadUrl = "https://spigotmc.org/" + redirect.attr("href");
      Element nameVar = document.selectFirst("h1");
      String nameText =
          nameVar
              .text()
              .replaceAll("/", "")
              .replaceAll("\\\\", "")
              .replaceAll("\\|", "")
              .replace(".", "")
              .replace("-", "")
              .split(" ")[0];
      Element version = nameVar.selectFirst("span.muted");
      String sizeAndType = redirect.text().replace("Download Now ", "");
      if (sizeAndType.equalsIgnoreCase("Via external site")) {
        return new Resource(downloadUrl, spigotId, version.text(), sizeAndType, nameText);
      }
      String type = sizeAndType.split(" ")[2];
      return new Resource(downloadUrl, spigotId, version.text(), type, nameText);
    }
  }

  public static Map<String, String> readResourceJsonIfExisting(File file) throws IOException {
    Map<String, String> map = new HashMap<>();
    if (file.exists()) {
      JsonNode read = Utils.objectMapper.readTree(file);
      String name = read.findValuesAsText("name").get(0);
      String fileType = read.findValuesAsText("fileType").get(0);
      String version = read.findValuesAsText("version").get(0);
      map.put("name", name);
      map.put("fileType", fileType);
      map.put("version", version);
    }
    return map;
  }

  public static String inlineXML(List<String> xmlFile) {
    StringBuilder builder = new StringBuilder();
    builder.append("<?xml version=\"1.0\"?>").append("\n");
    for (String line : xmlFile) {
      builder.append(line).append("\n");
    }
    return builder.toString();
  }
}
