package registration;

import com.google.gson.Gson;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminFunctions extends NanoHTTPD {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/attendees";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "princeobiuto";
    private static final Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(Registration.class.getName());

    public AdminFunctions() throws IOException {
        super(8081);
        try {
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
            System.out.println("Server started on http://localhost:8081");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Response addCorsHeaders(Response response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        return response;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        try {
            if (Method.OPTIONS.equals(method)) {
                // Handle preflight request for CORS
                try (Response response = newFixedLengthResponse(Response.Status.OK, "text/plain", "")) {
                    return addCorsHeaders(response);
                }
            }

            if ("/attendees".equalsIgnoreCase(uri)) {
                if (Method.GET.equals(method)) {
                    return addCorsHeaders(newFixedLengthResponse(Response.Status.OK, "application/json", getAttendees()));
                } else if (Method.DELETE.equals(method) && session.getParameters().containsKey("id")) {
                    int id = Integer.parseInt(session.getParameters().get("id").get(0));
                    deleteAttendee(id);
                    return addCorsHeaders(newFixedLengthResponse(Response.Status.OK, "application/json", "{\"status\":\"deleted\"}"));
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "GetMethod method error: ", e);
            return addCorsHeaders(newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"Server error\"}"));
        }

        return addCorsHeaders(newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", "{\"error\":\"Not Found\"}"));
    }


    private String getAttendees() throws SQLException {
        List<Map<String, Object>> attendees = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery("SELECT * FROM attendees")) {
            while (result.next()) {
                Map<String, Object> attendee = new HashMap<>();
                attendee.put("id", result.getInt("id"));
                attendee.put("first_name", result.getString("first_name"));
                attendee.put("last_name", result.getString("last_name"));
                attendee.put("email", result.getString("email"));
                attendees.add(attendee);
            }
        }
        return gson.toJson(attendees);
    }

    private void deleteAttendee(int id) throws SQLException {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
             PreparedStatement statement = conn.prepareStatement("DELETE FROM attendees WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public static void main(String[] args) {
        try {
            new AdminFunctions();
        } catch (IOException e) {
            System.err.println("Couldn't start server:\n" + e);
        }
    }
}