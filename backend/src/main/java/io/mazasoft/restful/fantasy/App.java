package io.mazasoft.restful.fantasy;

import fi.iki.elonen.NanoHTTPD;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Date;

public class App extends NanoHTTPD {
  public static void main(String[] args) {
    App app = new App(3000, resolvePath("../frontend/web"));

    try {
      app.start();
    } catch (IOException e) {
      System.err.println("Failed to start the server: " + e);
    }
  }

  private String assetPath;

  /**
   * Creates a new RESTful fantasy backend instance.
   * @param port is the port over which HTTP requests will be serviced.
   * @param assetPath is the absolute path to all the static web assets to
   *                  serve.
   */
  private App(int port, String assetPath) {
    super(port);

    this.assetPath = assetPath;
  }

  @Override
  public Response serve(IHTTPSession session) {
    log(session);

    if (isStaticAssetRequest(session)) return staticAssetRequestedBy(session);

    switch (toRoute(session)) {
      case "GET /": return ok("Hello, my name is server");
      case "GET /sup": return ok("Nothing much, human");
      case "POST /check/this/out": return ok("Woah!");
      default: return notFound("Not quite sure what you want...");
    }
  }

  @Override
  public void start() throws IOException {
    super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

    System.out.println("Server is now running on port 3000. Static assets " +
        "will be served from \"" + assetPath + "\"");
  }

  /**
   * Serves the static asset at the given path.
   * @param session the current HTTP server session.
   * @return a new HTTP response.
   */
  private Response staticAssetRequestedBy(IHTTPSession session) {
    Path staticAssetFilePath = Paths.get(assetPath + session.getUri());
    try {
      byte[] staticAssetFileContents = Files.readAllBytes(staticAssetFilePath);
      String staticAssetMimeType = Files.probeContentType(staticAssetFilePath);
      Charset staticAssetFileEncoding = Charset.forName("UTF-8");

      return newFixedLengthResponse(
        Response.Status.OK, staticAssetMimeType,
        new String(staticAssetFileContents, staticAssetFileEncoding));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return newFixedLengthResponse(
        Response.Status.INTERNAL_ERROR,
        "text/plain",
        "Seems that there was some sort of issue. Please try again later.");
  }

  /**
   * Checks to see if the incoming request refers to a static asset.
   * @param session the current HTTP server session.
   * @return true if the incoming request refers to a static asset.
   */
  private boolean isStaticAssetRequest(IHTTPSession session) {
    return session.getMethod() == Method.GET &&
        new File(assetPath + session.getUri()).exists();
  }

  /**
   * Logs an incoming HTTP session.
   * @param session the session to log.
   */
  private static void log(IHTTPSession session) {
    System.out.println("[" + (new Date()) + "] " + toRoute(session));
  }

  /**
   * Creates a plain-text HTTP response with status code 404.
   * @param content text to send back in the body of the response.
   * @return a new HTTP response.
   */
  private static Response notFound(String content) {
    return newFixedLengthResponse(
        Response.Status.NOT_FOUND,
        "text/plain",
        content);
  }

  /**
   * Creates a plain-text HTTP response with status code 200.
   * @param content text to send back in the body of the response.
   * @return a new HTTP response.
   */
  private static Response ok(String content) {
    return newFixedLengthResponse(
        Response.Status.OK,
        "text/plain",
        content);
  }

  /**
   * Resolves a potentially relative file path.
   * @param path the path to resolve.
   * @return the resolved path.
   */
  private static String resolvePath(String path) {
    try {
      return new File(path).getCanonicalPath();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Extracts a representative route string from the given session. For
   * instance, a GET request with the path "/hello/world", is turned
   * represented as a String that resembles "GET /hello/world".
   * @param session represents the context of the current HTTP request being
   *                serviced by this API.
   * @return a route string.
   */
  private static String toRoute(IHTTPSession session) {
    return session.getMethod().name() + " " + session.getUri();
  }
}
