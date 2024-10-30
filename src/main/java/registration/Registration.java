package src.main.java.registration;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class Registration extends NanoHTTPD {
        
    public Submit() throws IOException {
		super(8080);
		start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		System.out.println("Server started on http://localhost:8080/");
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
    public static void insertAttendee(String firstName, String middleName, String lastName, String email, String dob, String regNumber, String faculty, String department, String nationality, String country, String stateOfOrigin) {
    	if (isAttendeeExists(email)) {
            System.out.println("Attendee already exists in the database.");
            return;
    	}
            
        String sql = "INSERT INTO attendees (first_name, middle_name, last_name, email, dob, reg_number, faculty, department, nationality, country, state_of_origin) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (conn != null) {
                stmt.setString(1, firstName);
                stmt.setString(2, middleName);
                stmt.setString(3, lastName);
                stmt.setString(4, email);
                stmt.setString(5, dob);
                stmt.setString(6, regNumber);
                stmt.setString(7, faculty);
                stmt.setString(8, department);
                stmt.setString(9, nationality);
                stmt.setString(10, country);
                stmt.setString(11, stateOfOrigin);

                stmt.executeUpdate();
                
                System.out.println("Attendee submitted successfully.");
                SendEmail.createEmail(email, firstName, lastName);
            } /*else {
                System.out.println("Failed to make connection to database.");
            }*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public Response serve(IHTTPSession session) {
    	if (session.getUri().equals("/Submit")) {
    		String firstName = session.getParameters().getOrDefault("first_name", List.of("")).get(0);
            String middleName = session.getParameters().getOrDefault("middle_name", List.of("")).get(0);
            String lastName = session.getParameters().getOrDefault("last_name", List.of("")).get(0);
            String email = session.getParameters().getOrDefault("email", List.of("")).get(0);
            String dob = session.getParameters().getOrDefault("dob", List.of("")).get(0);
            String regNumber = session.getParameters().getOrDefault("reg_number", List.of("")).get(0);
            String faculty = session.getParameters().getOrDefault("faculty", List.of("")).get(0);
            String department = session.getParameters().getOrDefault("department", List.of("")).get(0);
            String nationality = session.getParameters().getOrDefault("nationality", List.of("")).get(0);
            String country = session.getParameters().getOrDefault("country", List.of("")).get(0);
            String stateOfOrigin = session.getParameters().getOrDefault("state_of_origin", List.of("")).get(0);
            
            // Insert into the database
            insertAttendee(firstName, middleName, lastName, email, dob, regNumber, faculty, department, nationality, country, stateOfOrigin);

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
