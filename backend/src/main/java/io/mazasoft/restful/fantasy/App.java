package io.mazasoft.restful.fantasy;

import fi.iki.elonen.NanoHTTPD;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Date;
import java.util.List;

public class App extends NanoHTTPD {
    public static void main(String[] args) {
        App app = new App(3030, resolvePath("../frontend/web"));

        try {
            app.start();
        } catch (IOException e) {
            System.err.println("Failed to start the server: " + e);
        }
    }

    private final String assetPath;
    private final Table<PlayerEntity> playerTable;
    private final Table<TeamEntity> teamTable;

    /**
     * Creates a new RESTful fantasy backend instance.
     *
     * @param port      is the port over which HTTP requests will be serviced.
     * @param assetPath is the absolute path to all the static web assets to
     *                  serve.
     */
    private App(int port, String assetPath) {
        super(port);

        this.assetPath = assetPath;
        this.playerTable = new Table<>(PlayerEntity.class);
        this.teamTable = new Table<>(TeamEntity.class);
    }

    @Override
    public Response serve(IHTTPSession session) {
        // Log each individual request to the command line so that it is easy to tell what is going on.
        log(session);

        // Check if a static asset is being requested (as opposed to an API request). Static assets look like files in
        // `frontend/web` (for instance, `index.html`). API requests look like `/sup`. If a static asset was requested
        // we need to read it from the file system and serve it.
        if (isStaticAssetRequest(session)) {
            return staticAssetRequestedBy(session);
        }

        // It is clear at this point that the client is requesting something of the API. So, handle the requests as
        // such.
        switch (toRoute(session)) {
            case "GET /sup":
                return ok("Nothing much, human");
            case "POST /check/this/out":
                return ok("Woah!");
            case "GET /teams":
                return ok(getAllTeams());
            case "PUT /teams":
                return ok(createTeam(requestBodyOf(session)));
            default:
                return notFound("Page Not Found");
        }
    }

    @Override
    public void start() throws IOException {
        super.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

        System.out.println("Server is now running on port " + getListeningPort() + ". Static assets " +
                "will be served from \"" + assetPath + "\".");
    }

    /**
     * Creates a new team in the database. Takes the JSON form of the team to be created and returns the freshly
     * created team - also in JSON form.
     *
     * @param teamJson JSON payload used to create the team.
     * @return the JSON representation of the created team.
     */
    private Entity createTeam(String teamJson) {
        TeamEntity entity = new TeamEntity();
        entity.copyFromJson(teamJson);

        return teamTable.create(entity);
    }

    /** Returns all of the teams in the database. */
    private List<TeamEntity> getAllTeams() {
        return teamTable.readAll();
    }

    /**
     * Serves the static asset at the given path.
     *
     * @param session the current HTTP server session.
     * @return a new HTTP response.
     */
    private Response staticAssetRequestedBy(IHTTPSession session) {
        String pathSuffix = session.getUri();

        // If the path suffix is just a slash - take that to mean that we should serve the index HTML.
        if (pathSuffix.equals("/")) {
            pathSuffix = "/index.html";
        }

        Path staticAssetFilePath = Paths.get(assetPath + pathSuffix);
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
     *
     * @param session the current HTTP server session.
     * @return true if the incoming request refers to a static asset.
     */
    private boolean isStaticAssetRequest(IHTTPSession session) {
        return session.getMethod() == Method.GET &&
                new File(assetPath + session.getUri()).exists();
    }

    /**
     * Reads the body of the request of the given session.
     * @param session the HTTP session for which the request body will be extracted.
     * @return the string form of the request body.
     */
    private static String requestBodyOf(IHTTPSession session) {
        // Use the content length header to assess how many bytes to read from the input stream.
        String contentLengthHeader = session.getHeaders().get("content-length");
        assert contentLengthHeader != null;

        // Make the content length into a number.
        Integer contentLength = Integer.parseInt(contentLengthHeader);

        byte[] bodyBuffer = new byte[contentLength];
        try {
            session.getInputStream().read(bodyBuffer, 0, contentLength);
            return new String(bodyBuffer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the request body", e);
        }
    }

    /**
     * Logs an incoming HTTP session.
     *
     * @param session the session to log.
     */
    private static void log(IHTTPSession session) {
        System.out.println("[" + (new Date()) + "] " + toRoute(session));
    }

    /**
     * Creates a plain-text HTTP response with status code 404.
     *
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
     *
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
     * Creates a JSON HTTP response with status code 200.
     *
     * @param entity entity to serialize into JSON.
     * @return a new HTTP response.
     */
    private static Response ok(Entity entity) {
        return newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                entity.toJson());
    }

    /**
     * Creates a JSON HTTP response with status code 200.
     *
     * @param entities entities to serialize into JSON.
     * @return a new HTTP response.
     */
    private static Response ok(List<? extends Entity> entities) {
        // Create a builder for concatenating together all of the JSONified entities.
        StringBuilder builder = new StringBuilder();

        // All JSON arrays start with a `[`.
        builder.append("[");

        // For each entity, turn it into JSON and append it to the JSON array. Make sure to include commas between each
        // JSON object.
        for (int i = 0; i < entities.size(); i++) {
            // The first JSON object should not have a comma in front, because that makes no sense.
            if (i > 0) {
                builder.append(",");
            }

            builder.append(entities.get(i).toJson());
        }

        // All JSON arrays end with a `]`.
        builder.append("]");

        // Combine all of the Strings in the builder into one string using the `toString` method.
        String entitiesJson = builder.toString();

        return newFixedLengthResponse(
                Response.Status.OK,
                "application/json",
                entitiesJson);
    }

    /**
     * Resolves a potentially relative file path.
     *
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
     *
     * @param session represents the context of the current HTTP request being
     *                serviced by this API.
     * @return a route string.
     */
    private static String toRoute(IHTTPSession session) {
        return session.getMethod().name() + " " + session.getUri();
    }
}
