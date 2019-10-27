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
package com.mrivanplays.siteapi;

import static spark.Spark.get;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.notFound;
import static spark.Spark.port;

import com.mrivanplays.siteapi.handlers.DefaultHandler;
import com.mrivanplays.siteapi.handlers.LibraryVersionHandler;
import com.mrivanplays.siteapi.handlers.MemeHandler;
import com.mrivanplays.siteapi.handlers.TeamTreesHandler;

public class Server {

  public static void main(String[] args) {
    port(5858);
    initExceptionHandler(Throwable::printStackTrace);

    DefaultHandler defaultHandler = new DefaultHandler();
    notFound(defaultHandler);

    MemeHandler memeHandler = new MemeHandler();
    get("/meme", memeHandler);
    get("/meme/", memeHandler);

//    SpigotDownloadHandler sdh = new SpigotDownloadHandler();
//    get("/spigot/download/:id", sdh);
//    get("/spigot/download/:id/", sdh);

    LibraryVersionHandler lvh = new LibraryVersionHandler();
    get("/library/version/:id", lvh);
    get("/library/version/:id/", lvh);

    TeamTreesHandler tth = new TeamTreesHandler();
    get("/trees", tth);
    get("/trees/", tth);

    get("/", defaultHandler);
  }
}
