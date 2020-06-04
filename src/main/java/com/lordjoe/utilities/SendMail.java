package com.lordjoe.utilities;

/**
 * com.lordjoe.utilities.SendMail
 * User: Steve
 * Date: 6/4/2020
 */

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

public class SendMail {


    private static String g_username;

    public static String getUsername() {
        return g_username;
    }

    public static void setUsername(String username) {
        g_username = username;
    }


    private static String g_encrypted_password;

    public static String getEncrypted_password() {
        return g_encrypted_password;
    }

    public static void setEncrypted_password(String encrypted_password) {
        g_encrypted_password = encrypted_password;
    }

    public static void sendMail(String recipient, String subjectline, String messagebody) {

        final String username = getUsername();
        final String password = Encrypt.decryptString(getEncrypted_password());

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("lordjoe2000@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient));
            //message.setSubject("Testing Subject");
            message.setSubject(subjectline);
            //message.setText("Dear Mail Crawler,"
            //	+ "\n\n No spam to my email, please!");

            message.setText(messagebody);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendMailWithAttachment(String recipient, String subjectline, String messagebody, File attachment) {

        final String username = getUsername();
        final String password = Encrypt.decryptString(getEncrypted_password());

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("lordjoe2000@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient));
            //message.setSubject("Testing Subject");
            message.setSubject(subjectline);
            //message.setText("Dear Mail Crawler,"
            //	+ "\n\n No spam to my email, please!");

            MimeBodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setText(messagebody);

            // Create a multipar message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            DataSource source = new FileDataSource(attachment);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(attachment.getName());
            multipart.addBodyPart(messageBodyPart);

            // Send the complete message parts
            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        File results = new File(args[0]);
        setUsername("lordjoe2000@gmail.com");
        setEncrypted_password("ihnub1aAK/e7lmKfr/DDd0PsRMIyFCsUory3H4ueoRiivLcfi56hGA==");
        String recipient = "simone.zorzan@gmail.com";
        String subjectline = "Your BLAST Analysis is complete";
        String messagebody = "The results are attached!";

        sendMailWithAttachment(recipient, subjectline, messagebody,results);
        System.out.println("Done");
    }
}

