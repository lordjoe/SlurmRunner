package com.lordjoe.ssh;

import com.lordjoe.utilities.Encrypt;
import com.lordjoe.utilities.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * com.lordjoe.ssh.ClusterProperties
 * User: Steve
 * Date: 10/22/2019
 */
public class ClusterProperties {

    private static Properties userProps = null;
    private static String privateKey;


    public static String getEmail() {
        return userProps.getProperty("email");
    }

    public static void setEmail(String email) {
        userProps.setProperty("email",email);
    }

    public static Properties getUserProperties() {
        if (userProps == null) {
            buildUserProps();
        }
        return userProps;
    }


    public static String getUserName() {
        return System.getProperty("user.name");
    }


    private static void buildUserProps() {
        if (userProps != null && !userProps.isEmpty())
            return;
        String user = getUserName();
        File userPropFile = new File(user + "Cluster.properties");
        userProps = new Properties();
        if (userPropFile.exists()) {
            try {
                userProps.load(new FileInputStream(userPropFile));
                return;
            } catch (IOException e) {
                // try plan B

            }
        }
        userProps.setProperty("user", DEFAULT_USER);
        userProps.setProperty("email",DEFAULT_EMAIL);
        userProps.setProperty("remoteUser", DEFAULT_USER);
        userProps.setProperty("remoteIP", DEFAULT_IP);
        userProps.setProperty("remotePort", Integer.toString(DEFAULT_PORT));
        userProps.setProperty("private.key", DEFAULT_PRIVATE_KEY1_FILE1);
        userProps.setProperty("public.key", DEFAULT_PUBLIC_KEY_FILE1);
        userProps.setProperty("passphrase", DEFAULT_PASS_PHRASE);

    }


    public static String getUser() {
        return getUserProperties().getProperty("remoteUser");
    }

    public static String getPrivateKeyFile() {
        return getUserProperties().getProperty("private.key");
    }

    public static String getPrivateKey() {
        if (privateKey == null) {
            privateKey = FileUtilities.readInFile(getPrivateKeyFile());
        }
        return privateKey;
    }

    public static String getPrivateKey1() {

        return PRIVATE_KEY1;
    }



    public static String getPublicKeyFile() {
        return getUserProperties().getProperty("public.pub");
    }


    public static String getIP() {
        return getUserProperties().getProperty("remoteIP");
    }


    public static String getPassPhrase() {
        String passphrase = getEncryptedPassPhrase();
        if (passphrase == null)
            return passphrase;
        return Encrypt.decryptString(passphrase);
    }


    public static String getEncryptedPassPhrase() {
        String passphrase = getUserProperties().getProperty("passphrase");
               return passphrase;

    }

    public static Integer getPort() {
        Properties userProperties = getUserProperties();
        String port = userProperties.getProperty("remotePort");
        return Integer.parseInt(port);
    }

    public static String DEFAULT_EMAIL = "lordjoe2000@gmail.com";
    public static String DEFAULT_PRIVATE_KEY1_FILE1 = "private.ppk"; //id_rsa_list.ppk";
    public static String DEFAULT_PUBLIC_KEY_FILE1 = "public.ppk"; //id_rsa_list.ppk";
    public static final String DEFAULT_IP = "10.60.1.4";

    public static final String DEFAULT_USER = "lewis";

    public static final int DEFAULT_PORT = 22;

    public static final String DEFAULT_PASS_PHRASE = "ihnub1aAK/e7lmKfr/DDd0PsRMIyFCsUory3H4ueoRiivLcfi56hGA==";

    public static final String PRIVATE_KEY1 =
            "-----BEGIN RSA PRIVATE KEY-----\n" +
                    "Proc-Type: 4,ENCRYPTED\n" +
                    "DEK-Info: AES-128-CBC,9447186D40D7DE35D7D3A86492DB386A\n" +
                    "\n" +
                    "IdhXZEZfJ9wLN4wPjpZnUN3zr7UIsnjXkIDwuEDeVbNw/Oniwry+t2/smFX4LmPz\n" +
                    "PKUPsnZ/fcihYBwgG225jGSsEl8F7Mt6hyot+VS5QSkoe3sRlP/wqFPbilX65388\n" +
                    "cg6gjd9QT6pFG9VLLBdKtm69tPakxz/Qk0YIFQIsPBZP9I5CBtS4H9aZbJII7mtf\n" +
                    "Oqzl/Z3ITD+RjCSE/N7ZQJrKqhuUxsfjl/8wGI4lph0wQjp814UmvvaP50BRNcUO\n" +
                    "hzcwuIPMHUmLQiNpVIeFwNaWaaqcHLfGwVula6frzS/9OckYzoz7LcYrVDzICM8N\n" +
                    "Dl1ErIyF6E/Ohh9CxVbEWR5IcsAZ9DVKiz8CloidN2vcWD3ax/lmBh/uel4gJgiq\n" +
                    "vo/7kTXyMXcfvHHsPfDMeZD7zpOYR8mTcJgwUxGdBOyedQ/WdbrwchaLnOTS1Qqt\n" +
                    "tmFuDu3rdp6BdWcqr6LjoQgAzgWiQgrranzRQAfBArn3wfrFJzcDfGstd9iVa9BH\n" +
                    "hqGMi+YfWHoVXRjx4f20Hu4iJjOxC34KZFeVWHwIBoTnh9Zg4IIfWuWlJaaya+T3\n" +
                    "bsRw1vllmsnIpvHInm1O8VQxwNa9p+4SUloC2vOA+xFMd8mUIjlOC5LZ+AZ2s5tb\n" +
                    "WWer8/miG3PPxOYdo+xCqQ80y/Y4cIgJWjDew4cBz06UrX05cdrSpKoYLDgQPeSH\n" +
                    "QHR6FT8g13OGFiCzXxqZAB0cDOAMSoB5JOuE0w+GAhhHGCdfct7Fe6IUHiTRLrZJ\n" +
                    "QALL6eRSBJQM8UuoNnbTqkKhUvB7RcDoxotIWgt7Qmen8/4FGsn9oI40bCLw/bv5\n" +
                    "1x+ar2TvV3FRxJ7i0Ot455IyQJIvRmtC0E0EwpRWtPoHu3o/p8la6Hzg5Rfbldhh\n" +
                    "e3p0V0hZpwVfCfT0tVb8jHNo06N/+yyYzoP6Xx3J6Gv5v6wtuRsdL+Y14ATtLmAS\n" +
                    "L3K/D1RCiamxac6m2zeZYtiYjbPpAkecc+4S4KJYiLMERLtQ+1QxLCjiGbupPQTq\n" +
                    "c7TP5odEb7QHJhEq+FpidlEtBwMAqhwe9122QEX1aM4DXpPailWljliAGDMIzAyO\n" +
                    "qOVWMn7SOPJCo59BS6f3QXWjpmjfAwYTR16GFY25ZflWgdFHTmCOQoHxZ+MYw5LF\n" +
                    "ItTpmDIKMVu+sBD2ynkCRzgdv7vM28C/YGPsm7hi1wI3ixyk5lPnQC1yIJ/BLe5i\n" +
                    "bG9lIbeWd/CArLE6oKwYg6GrafWKiCImwPViqXICtdurMxCd2cqSfSFQRLSvUeB5\n" +
                    "gmZLl8S0AvU3N4Ya65azlkn+cOiUdma/wqjQzITaOM9hxaHAE0TcqmdkmaiRofCR\n" +
                    "jJJV2NW6ZYerJmOagQRGvOmNy8P0WRh/ZX06l02s/OjJkExMTxEAGjmzObSArfZ+\n" +
                    "Y5H3dKoJj/LwqBkCjedjAQUxhovxcFFr+g/gOcTE5ueOf/wOP1lIexmGqRzjcj7C\n" +
                    "WWvHlDgUsseVvYPXAKoa1Wecv9K651p6ssyOUmHNLSs5WqyQv7ovnqaaNQ3nUVdt\n" +
                    "JTVTvuKAQP6PsOPQylWQvzZM0D0a9XykS7/oemDnAltZhg0hys+q/XztfezhI3CW\n" +
                    "-----END RSA PRIVATE KEY-----";

    public static void main(String[] args) {

        String s = Encrypt.encryptString("FooBar");
        System.out.println("EncyrptedPrivateKey1 = ");
        System.out.println(s);
        System.out.println();

    }
}


