package src.main.java.registration;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.PasswordAuthentication;

public class SendEmail {

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
            e.printStackTrace();
        }
        final String username = props.getProperty("email.username");
        final String password = props.getProperty("email.password");
    
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
    
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Registration Confirmation");
            String messageContent = "<h1>Dear Sir/Madam</h1> <p>Thank you for registering! Your registration was successful, and we look forward to seeing you at the event.\n\nBest regards,\nEvent Team</p>";
            message.setContent(messageContent, "text/html");
    
            Transport.send(message);
            System.out.println("Email sent successfully to " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}