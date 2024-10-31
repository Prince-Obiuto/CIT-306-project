package registration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class SendEmail {
	
	private static final Logger logger = Logger.getLogger(Registration.class.getName());

    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        FileInputStream fileInput = new FileInputStream("config.properties");
        props.load(fileInput);
        return props;
    }
    
    public static void createEmail(String recipientEmail, String firstName, String lastName) {
            Properties props = null;
            try {
                props = loadProperties();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Load properties error: ", e);
            }
        final String username = props.getProperty("email.username");
        final String password = props.getProperty("email.password");

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        
        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(username, password);
                }
            });
        
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("Registration Confirmation");
                String messageContent = "<div style=\"font-family: Arial, sans-serif; color: #333; max-width: 600px; margin: 0 auto; background-color: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 20px;\">"
                        + "<h1 style=\"color: #f59e0b; font-size: 24px; text-align: center;\">Thank You for Registering!</h1>"
                        + "<p style=\"color: #333; font-size: 16px; line-height: 1.5;\">Dear Sir/Madam,</p>"
                        + "<p style=\"color: #333; font-size: 16px; line-height: 1.5;\">We are thrilled to confirm your registration. Your registration was successful, and we look forward to seeing you at the event!</p>"
                        + "<hr style=\"border: 0; height: 1px; background-color: #ddd; margin: 20px 0;\">"
                        + "<div style=\"text-align: center;\">"
                        + "<p style=\"color: #f59e0b; font-size: 18px; font-weight: bold;\">Event Details</p>"
                        + "<p style=\"color: #333; font-size: 14px;\">Date: <strong>Saturday, November 4th, 2023</strong><br>Location: <strong>Main Auditorium, XYZ Venue</strong></p>"
                        + "<a href=\"#\" style=\"display: inline-block; text-decoration: none; background-color: #f59e0b; color: #fff; padding: 10px 20px; border-radius: 4px; font-size: 16px; font-weight: bold; margin-top: 20px;\">View Event Details</a>"
                        + "</div>"
                        + "<p style=\"margin-top: 30px; color: #333; font-size: 14px;\">If you have any questions, feel free to reach out to us at <a href=\"mailto:support@example.com\" style=\"color: #f59e0b;\">support@example.com</a>.</p>"
                        + "<p style=\"margin-top: 20px; color: #333; font-size: 14px;\">Looking forward to an amazing event together!</p>"
                        + "<p style=\"color: #333; font-size: 14px;\">Best regards,<br><strong>Event Team</strong></p>"
                        + "</div>";
                message.setContent(messageContent, "text/html");
        
                Transport.send(message);
                System.out.println("Email sent successfully to " + recipientEmail);
            } catch (MessagingException e) {
                logger.log(Level.SEVERE, "Set and send message error: ", e);
                throw new RuntimeException(e);
            } 
    }
}