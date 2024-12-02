package registration;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class Registration extends NanoHTTPD {

    private static final Logger logger = Logger.getLogger(Registration.class.getName());

    public Registration() throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("Server started on http://localhost:8080/");
    }

    public Registration(int port) {
        super(port);
    }

    public static Properties loadProperties() {
        Properties props = new Properties();
        try {
            FileInputStream fileInput = new FileInputStream("src/main/java/registration/config.properties");
            props.load(fileInput);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Configuration File error", e);
        }
        return props;
    }

    // Method to establish a connection to the database
    private static Connection connect() {
        Properties props = loadProperties();

        final String JDBC_URL = props.getProperty("sql.JDBC_URL");
        final String JDBC_USER = props.getProperty("sql.JDBC_USER");
        final String JDBC_PASSWORD = props.getProperty("sql.JDBC_PASSWORD");

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection error: ", e);
        }
        return connection;
    }

    // Method to check if record exists
    public static boolean isAttendeeExists(String email) {
        String sql = "SELECT COUNT(*) FROM attendees WHERE email = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // Attendee exists
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Existing user in Database error: ", e);
        }
        return false;
    }

    private String AttendeeExistsPage(String firstName, String lastName) {
        String message = "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Registration Error</title>" +
                "    <link href=\"https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css\" rel=\"stylesheet\">" +
                "</head>" +
                "<body class=\"bg-yellow-500 flex items-center justify-center min-h-screen\">" +
                "    <div class=\"bg-black p-8 rounded-lg shadow-lg w-full max-w-md text-center\">" +
                "        <h2 class=\"text-2xl font-semibold text-yellow-600 mb-4\">Attendee Already Registered</h2>" +
                "        <p class=\"text-white mb-6\">Hello " + firstName + " " + lastName + ", you are already registered with us.</p>" +
                "        <a href=\"/\" class=\"inline-block bg-yellow-600 text-white py-2 px-4 rounded hover:bg-yellow-700 focus:outline-none focus:ring-2 focus:ring-yellow-500\">" +
                "            Back to Home" +
                "        </a>" +
                "    </div>" +
                "</body>" +
                "</html>";
        return message;
    }

    // Method to insert a new attendee
    public Response insertAttendee(String firstName, String lastName, String email, String phone, String positions) {
        if (isAttendeeExists(email)) {
            System.out.println("Attendee already exists in the database.");
            return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "text/html", AttendeeExistsPage(firstName, lastName));
        }

        String sql = "INSERT INTO attendees (first_name, last_name, email, phone, positions) " + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, email);
            statement.setString(4, phone);
            statement.setString(5, positions);

            statement.executeUpdate();

            System.out.println("Attendee submitted successfully.");
            SendEmail.createEmail(email, firstName, lastName);

            return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, "text/html", confirmationMessage());

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Inserting attendee error: ", e);
            return NanoHTTPD.newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", error400Page());
        }
    }

    private String error404Page() {
        String message = "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>404 Not Found</title>" +
                "    <link href=\"https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css\" rel=\"stylesheet\">" +
                "</head>" +
                "<body class=\"bg-yellow-500 flex items-center justify-center min-h-screen\">" +
                "    <div class=\"bg-black p-8 rounded-lg shadow-lg w-full max-w-md text-center\">" +
                "        <h2 class=\"text-2xl font-semibold text-yellow-600 mb-4\">404 Not Found</h2>" +
                "        <p class=\"text-white-700 mb-6\">The page you are looking for does not exist.</p>" +
                "        <a href=\"/index.html\" class=\"inline-block bg-yellow-600 text-black py-2 px-4 rounded hover:bg-yellow-700 focus:outline-none focus:ring-2 focus:ring-yellow-500\">" +
                "            Back to Home" +
                "        </a>" +
                "    </div>" +
                "</body>" +
                "</html>";
        return message;
    }

    private String error400Page() {
        String message = "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>400 Bad Request</title>" +
                "    <link href=\"https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css\" rel=\"stylesheet\">" +
                "</head>" +
                "<body class=\"bg-yellow-500 flex items-center justify-center min-h-screen\">" +
                "    <div class=\"bg-white p-8 rounded-lg shadow-lg w-full max-w-md text-center\">" +
                "        <h2 class=\"text-2xl font-semibold text-yellow-600 mb-4\">400 Bad Request</h2>" +
                "        <p class=\"text-gray-700 mb-6\">Your request could not be understood by the server.</p>" +
                "        <a href=\"/index.html\" class=\"inline-block bg-yellow-600 text-white py-2 px-4 rounded hover:bg-yellow-700 focus:outline-none focus:ring-2 focus:ring-yellow-500\">" +
                "            Back to Home" +
                "        </a>" +
                "    </div>" +
                "</body>" +
                "</html>";
        return message;
    }

    private String confirmationMessage() {
        String message = "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Registration Confirmation</title>" +
                "    <link href=\"https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css\" rel=\"stylesheet\">" +
                "</head>" +
                "<body class=\"bg-yellow-500 flex items-center justify-center min-h-screen\">" +
                "    <div class=\"bg-black p-8 rounded-lg shadow-lg w-full max-w-md text-center\">" +
                "        <h2 class=\"text-2xl font-semibold text-yellow-600 mb-4\">Thank You for Registering!</h2>" +
                "        <p class=\"text-white mb-6\">We have received your registration details. You will receive an email confirmation shortly.</p>" +
                "        <a href=\"/\" class=\"inline-block bg-yellow-600 text-white py-2 px-4 rounded hover:bg-yellow-700 focus:outline-none focus:ring-2 focus:ring-yellow-500\">" +
                "            Back to Home" +
                "        </a>" +
                "    </div>" +
                "</body>" +
                "</html>";
        return message;
    }

    @Override
    public Response serve(IHTTPSession session) {
        System.out.println("Requested URI: " + session.getUri());
        System.out.println("Request Method: " + session.getMethod());

        if (session.getMethod() == Method.GET && session.getUri().equals("/Registration")) {
            System.out.println("Matched /Registration endpoint");

            // Retrieve parameters
            String firstName = session.getParameters().getOrDefault("first_name", List.of("")).getFirst();
            String lastName = session.getParameters().getOrDefault("last_name", List.of("")).getFirst();
            String email = session.getParameters().getOrDefault("email", List.of("")).getFirst();
            String phone = session.getParameters().getOrDefault("phone", List.of("")).getFirst();
            String positions = session.getParameters().getOrDefault("positions", List.of("")).getFirst();

            // Insert into the database if required parameters are provided
            if (!firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty()) {
                return insertAttendee(firstName, lastName, email, phone, positions);
            } else {
                // If parameters are missing, return an error message
                return NanoHTTPD.newFixedLengthResponse(Status.BAD_REQUEST, "text/html", error400Page());
            }
        }

        System.out.println("URI not matched. Returning 404.");
        return NanoHTTPD.newFixedLengthResponse(Status.NOT_FOUND, "text/html", error404Page());
    }

    public static void main(String[] args) {
        try {
            new Registration();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server startup error: ", e);
        }
    }
}