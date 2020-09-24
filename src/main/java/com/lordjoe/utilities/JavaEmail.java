package com.lordjoe.utilities;

/**
 * com.lordjoe.utilities.JaavEmail
 * User: Steve
 * Date: 9/23/20
 * https://howtodoinjava.com/for-fun-only/how-to-send-email-in-java-using-gmail-smtp-server/
 * /Given below is program which can be used to send emails using Gmail SMTP server.
 */


import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Properties;

public class JavaEmail {
    public static boolean UseListSMTP = true; // !System.getProperty("user.name").equalsIgnoreCase("steve");

    private static String g_username = "FEDER-bio-HPC-jobs@list.lu";

    public static String getUsername() {
        return g_username;
    }

    public static void setUsername(String username) {
        g_username = username;
    }

    private static String EncryptedPassword = "ihnub1aAK/e7lmKfr/DDd0PsRMIyFCsUory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEYory3H4ueoRiivLcfi56hGKK8tx+LnqEY";
      private static String g_encrypted_password = EncryptedPassword;


    public static String getEncrypted_password() {
        return g_encrypted_password;
    }

    public static void setEncrypted_password(String encrypted_password) {
        g_encrypted_password = encrypted_password;
    }

    Session mailSession;


    private void setMailServerProperties() {
        Properties emailProperties = System.getProperties();
        if (!UseListSMTP) {
            setEncrypted_password(EncryptedPassword);
            setUsername("lordjoe2000@gmail.com");
            emailProperties.put("mail.smtp.port", "587");
            emailProperties.put("mail.smtp.auth", "true");
            emailProperties.put("mail.smtp.starttls.enable", "true");
            emailProperties.put("mail.smtp.host", "smtp.gmail.com");
        }
        else {
            emailProperties.put("mail.smtp.auth", "false");
            emailProperties.put("mail.smtp.starttls.enable", "true");
            emailProperties.put("mail.smtp.host", "smtp.private.list.lu");
            emailProperties.put("mail.smtp.port", "25");
        }
        mailSession = Session.getDefaultInstance(emailProperties, null);

    }

    public void sendEmail(String emailSubject, String emailBody, String toEmails) throws AddressException, MessagingException {
        /**
         * Sender's credentials
         * */
        String fromUser = getUsername();
        String encrypted_password1 = getEncrypted_password();
        String fromUserEmailPassword = Encrypt.decryptString(encrypted_password1);

        String emailHost;
        if (!UseListSMTP) {
            emailHost = "smtp.gmail.com";
        }
        else {
            emailHost = "smtp.private.list.lu";
        }
        Transport transport = mailSession.getTransport("smtp");
        transport.connect(emailHost, fromUser, fromUserEmailPassword);
        /**
         * Draft the message
         * */
        MimeMessage emailMessage = draftEmailMessage(toEmails, emailSubject, emailBody);
        /**
         * Send the mail
         * */
        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
        transport.close();
        System.out.println("Email sent successfully.");
    }

    public void sendEmail(String emailSubject, String emailBody, String toEmails, File attachment) throws AddressException, MessagingException {
        /**
         * Sender's credentials
         * */
        String fromUser = getUsername();
        String encrypted_password1 = getEncrypted_password();
        String fromUserEmailPassword = Encrypt.decryptString(encrypted_password1);

        String emailHost;
        if (!UseListSMTP) {
            emailHost = "smtp.gmail.com";
        }
        else {
            emailHost = "smtp.private.list.lu";
        }
        Transport transport = mailSession.getTransport("smtp");
        transport.connect(emailHost, fromUser, fromUserEmailPassword);
        /**
         * Draft the message
         * */
        MimeMessage emailMessage = draftEmailMessage(toEmails, emailSubject, emailBody, attachment);
        /**
         * Send the mail
         * */
        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
        transport.close();
        System.out.println("Email sent successfully.");
    }

    private MimeMessage draftEmailMessage(String recipient, String emailSubject, String emailBody)
            throws AddressException, MessagingException {
        MimeMessage emailMessage = new MimeMessage(mailSession);
        /**
         * Set the mail recipients
         * */
        emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        emailMessage.setSubject(emailSubject);
        /**
         * If sending HTML mail
         * */
        emailMessage.setContent(emailBody, "text/html");
        /**
         * If sending only text mail
         * */
        //emailMessage.setText(emailBody);// for a text email
        return emailMessage;
    }

    public MimeMessage draftEmailMessage(String recipient, String emailSubject, String emailBody, File attachment) {
        try {
            MimeMessage message = new MimeMessage(mailSession);
            /**
             * Set the mail recipients
             * */
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(emailSubject);
            //message.setText("Dear Mail Crawler,"
            //	+ "\n\n No spam to my email, please!");

            MimeBodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setText(emailBody);

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
            return message;
        } catch (MessagingException e) {
            throw new RuntimeException(e);

        }

    }

    public void sendMailWithAttachment(String recipient, String subjectline, String messagebody, File attachment) {
        /**
         * Sender's credentials
         * */
        String fromUser = getUsername();
        String encrypted_password1 = getEncrypted_password();
        String fromUserEmailPassword = Encrypt.decryptString(encrypted_password1);

        try {
            final String username = getUsername();
            String password1 = null;
            String encrypted_password = encrypted_password1;
            if (encrypted_password != null)
                password1 = Encrypt.decryptString(encrypted_password);

            MimeMessage messsage = draftEmailMessage(recipient, subjectline, messagebody, attachment);

            String emailHost;
            if (!UseListSMTP) {
                emailHost = "smtp.gmail.com";
            }
            else {
                emailHost = "smtp.private.list.lu";
            }
            Transport transport = mailSession.getTransport("smtp");
            transport.connect(emailHost, fromUser, fromUserEmailPassword);


            transport.sendMessage(messsage, messsage.getAllRecipients());
            transport.close();

//            final String password = password1;
//            Properties props = new Properties();
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.starttls.enable", "true");
//            props.put("mail.smtp.host", "smtp.gmail.com");
//            props.put("mail.smtp.port", "587");
//
//
//            if (UseListSMTP) {
//                props.put("mail.smtp.auth", "false");
//                props.put("mail.smtp.starttls.enable", "true");
//                props.put("mail.smtp.host", "smtp.private.list.lu");
//                props.put("mail.smtp.port", "25");
//            }
//
//            Session session = null;
//            if (!UseListSMTP) {
//                session = Session.getInstance(props,
//                        new javax.mail.Authenticator() {
//                            protected PasswordAuthentication getPasswordAuthentication() {
//                                return new PasswordAuthentication(username, password);
//                            }
//                        });
//            } else {
//                session = Session.getInstance(props, null); // no authentication
//            }
//
//
//            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String args[]) {
        try {
            JavaEmail javaEmail = new JavaEmail();
            javaEmail.setMailServerProperties();
            String emailSubject = "Test email subject";
            String emailBody = "This is an email sent by <b>//howtodoinjava.com</b>.";
        //    String toEmails = "lordjoe2000@gmail.com";
            String toEmails = "simone.zorzan@list.lu";
            if (args.length > 0) {
                javaEmail.sendEmail(emailSubject, emailBody, toEmails, new File(args[0]));

            } else {
                javaEmail.sendEmail(emailSubject, emailBody, toEmails);
            }
        } catch (MessagingException e) {
            throw new RuntimeException(e);

        }
    }


}
