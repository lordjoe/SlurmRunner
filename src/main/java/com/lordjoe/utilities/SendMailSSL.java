package com.lordjoe.utilities;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendMailSSL {

    public static void main(String[] args) throws Exception {

   
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.debug", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        prop.put("mail.smtp.socketFactory.fallback", "false");


 
    //    SMTPAuthenticator auth = new SMTPAuthenticator();
        Session session = Session.getInstance(prop, AuthenticationUtilities.INSTANCE);


        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("lordjoe2000@gmail.com"));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse("lordjoe2000@gmail.com")
        );
        message.setSubject("Testing Gmail SSL");
        message.setText("Dear Mail Crawler,"
                + "\n\n Please do not spam my email!");

        Transport.send(message);

        System.out.println("Done");

    }
}