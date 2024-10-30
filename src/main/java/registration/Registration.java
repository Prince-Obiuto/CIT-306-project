package registration;

import java.io.IOException;
import java.sql.*;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class Registration extends NanoHTTPD {
        
    public Registration() throws IOException {
		super(8080);
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		System.out.println("Server started on http://localhost:8080/");
	}

    public Registration(int port) {
        super(port);
    }

    // Method to establish a connection to the database
    private static Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/event site", "root", "princeobiuto");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
    
    //Method to check if record exists
    public static boolean isAttendeeExists(String email) {
        String sql = "SELECT COUNT(*) FROM attendees WHERE email = ?";
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // Attendee exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Method to insert a new attendee
    public static void insertAttendee(String firstName, String lastName, String email, String phone, String role) {
    	if (isAttendeeExists(email)) {
            System.out.println("Attendee already exists in the database.");
            return;
    	}
            
        String sql = "INSERT INTO attendees (first_name, last_name, email, phone, role) " + "VALUES (?, ?, ?)";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, role);

            stmt.executeUpdate();

            System.out.println("Attendee submitted successfully.");
            src.main.java.registration.SendEmail.createEmail(email, firstName, lastName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public Response serve(IHTTPSession session) {
    	if (session.getUri().equals("/Registration")) {
    		String firstName = session.getParameters().getOrDefault("first_name", List.of("")).get(0);
            String lastName = session.getParameters().getOrDefault("last_name", List.of("")).get(0);
            String email = session.getParameters().getOrDefault("email", List.of("")).get(0);
            String phone = session.getParameters().getOrDefault("phone", List.of("")).get(0);
            String role = session.getParameters().getOrDefault("role", List.of("")).get(0);
            
            // Insert into the database
            insertAttendee(firstName, lastName, email, phone, role);

            String confirmationPage = String.format("/confirmation.html?first_name=%s&last_name=%s", firstName, lastName);
            Response response = NanoHTTPD.newFixedLengthResponse(Response.Status.REDIRECT, "text/html", "");
            response.addHeader("Location", confirmationPage);  // Set the 'Location' header for redirection

            return response;
    	}
    	return NanoHTTPD.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "404 Error \nNot Found");
    }

    public static void main(String[] args) {
    	try {
    		new Registration();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
}
