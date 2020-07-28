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

    public static boolean UseListSMTP = !System.getProperty("user.name").equalsIgnoreCase("steve");

    private static String g_username = "FEDER-bio-HPC-jobs@list.lu";

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
        if(!UseListSMTP) {
            setUsername("lordjoe2000@gmail.com");
            setEncrypted_password("ihnub1aAK/e7lmKfr/DDd0PsRMIyFCsUory3H4ueoRiivLcfi56hGA==");
        }

        final String username = getUsername();
        String encrypted_password = getEncrypted_password();
        Properties props = new Properties();
        String password = null;
        if(encrypted_password != null) {
            password = Encrypt.decryptString(encrypted_password);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
        }
        else {
            props.put("mail.smtp.auth", "false");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.private.list.lu");
            props.put("mail.smtp.port", "425");
         }

        final String pwdx = password; // allow inner class access
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, pwdx);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient));
            //message.setSubject("Testing Subject");
            message.setSubject(subjectline);
            //message.setText("Dear Mail Crawler,"
            //	+ "\n\n No spam to my email, please!");

            message.setText(messagebody);

            Transport.send(message);

            System.out.println("EMail Sent");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendMailWithAttachment(String recipient, String subjectline, String messagebody, File attachment) {

        final String username = getUsername();
        String password1 = null;
        String encrypted_password = getEncrypted_password();
        if(encrypted_password != null)
           password1 = Encrypt.decryptString(encrypted_password);

        final String password = password1;
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");


        if(UseListSMTP) {
            props.put("mail.smtp.auth", "false");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.private.list.lu");
            props.put("mail.smtp.port", "587");
         }
        
        Session session = null;
          if(!UseListSMTP) {
               session = Session.getInstance(props,
                     new javax.mail.Authenticator() {
                         protected PasswordAuthentication getPasswordAuthentication() {
                             return new PasswordAuthentication(username, password);
                         }
                     });
         }
         else {
                session = Session.getInstance(props,null); // no authentication
          }

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

      //   UseListSMTP = true;
        File results = new File(args[0]);

        if(!UseListSMTP) {
            setUsername("lordjoe2000@gmail.com");
            setEncrypted_password("ihnub1aAK/e7lmKfr/DDd0PsRMIyFCsUory3H4ueoRiivLcfi56hGA==");
        }
        String recipient = "lordjoe2000@gmail.com";
        String subjectline = "Your BLAST Analysis is complete";
        String messagebody = "The results are attached!";

        sendMailWithAttachment(recipient, subjectline, messagebody,results);
        System.out.println("Done");
    }
}

