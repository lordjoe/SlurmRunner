package com.lordjoe.ssh;

import com.lordjoe.utilities.Encrypt;

/**
 * com.lordjoe.ssh.ClusterProperties
 * User: Steve
 * Date: 10/22/2019
 */
public class ClusterProperties {

    public static String PRIVATE_KEY1_FILE1 = "/home/Steve/.ssh/HPC.ppk";// "private.ppk"; //id_rsa_list.ppk";
    public static final String PUBLIC_KEY1_FILE1 = "/home/Steve/.ssh/HPC.pub";// "public.pub";

    public static final String PASS_PHRASE1 = "Alaron&Aurana&Bob" ;

    public static final String IP = "10.60.1.4";

    public static final String USER = "lewis";

    public static final int PORT = 22;

    public static final String PRIVATE_KEY=
            "PuTTY-User-Key-File-2: ssh-rsa\n" +
                    "Encryption: none\n" +
                    "Comment: rsa-key-20191022\n" +
                    "Public-Lines: 6\n" +
                    "AAAAB3NzaC1yc2EAAAABJQAAAQEAyQTEZqjzm05uOcGoFVOd0hN0Tc49UDgqbpCI\n" +
                    "pIYVnUlkXR8bjjWz3b3CzDYU7CzZEmTlN+jOHrYoYtg7r1UhOIHgw2bIYtnOQCp8\n" +
                    "uHweDHfodUtaHkYpxDHjTzBuhzJ+c0k/aAvWCs+zMwZ/bHtXCsPPWjvm19SRZnQZ\n" +
                    "RtQn5L60j+M+XyVQ0Xf9c0tt3b1922n514e+FhZj4ePi0QTkrWqimBYvWJWN+qZl\n" +
                    "PtQSW1F06SSwc+M/uj9EvnSbAZrWbFrgEefmVXhInrR7OLOr+nqDhoyqUYc+Jlwd\n" +
                    "J8HU75pj6TE9Cz+WLX4LF+3ar0AymPYsC7W5plUfTkD461SyIQ==\n" +
                    "Private-Lines: 14\n" +
                    "AAABAEEx+oIo9Q/GaPAjIcG6QQXcy8Y1DPdsKW/3hkMyZ9/8IIznfo78qQmlU+80\n" +
                    "InYcYhPNsh/4ejN5WTTQgg9Z4ULCVrvp+80IXpFZ40LAeHK/GvWb5eBOG2I5sYF3\n" +
                    "fcr7nqHuPhPoKb5RMzMks7R7B3kc6U2ykA6lzkq93q8wDPA9Rhp5JORPVz2O7jZx\n" +
                    "jRvEyOOwL0B1rPeiG0QnjjkE+8DtStCeTupajlcHslVQ/Ub6PJxNZOuc1Gfq8XgW\n" +
                    "ZDsO7+SKFuBJyIoyfC80YXWpre3JYWvEjuzIQk4PVSDyj1XTPTadkNjsCSFO9zCx\n" +
                    "Km76VAK2ex1Ro/JDcHkawvCM590AAACBAPU6KXwQPd0Zycahm37VhaITCcQtOFC8\n" +
                    "fPKw+stMfPbQ6Abmq2ugfC9296rrKrY/OmSolTguYOoOO1OHgEsAOJAhulOxRbHF\n" +
                    "0DRSfBdGHc+mtzi1un7p64jIyBLBrYMHrdEOCXgBw3FHdP3Xdi2GX3gqQD0tqT6z\n" +
                    "CBXmljCeSJzbAAAAgQDR2W7l5EgmueAtqdHRR+5wX6uj+OSYCLrydBnLqg3lnvxK\n" +
                    "WNf7ECzSTKxUd0T51pOACUuBGbQcFkTeBK9FoJ7in0wbLRQKOuHAEAGMx8qutpZe\n" +
                    "bzQdYtzo7OTD6yhtSfaWHWybwfmzagA9v6q/qAeMu63zQ6YWyFJunO2Z51mfswAA\n" +
                    "AIAxKzwZgBnj1BCKHT0iRStuJUE1yy59e5PyabSoISP7iwynsC4I3dN0O70ev64r\n" +
                    "OAQavpFT9pDlC2twndtIHsAw9A72UzdMg0GNz5VbTsI8zCqAZtVrvvKldz2JNRRh\n" +
                    "OMEdh3IURy5R+E2Ip8YZUpgYVn03CK+zi9TAqRyuFK0e3A==\n" +
                    "Private-MAC: d15bed7d5a608dfbd550b169e8c32b69c7412631\n";

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
        String s = Encrypt.encryptString(PRIVATE_KEY1);
        System.out.println("EncyrptedPrivateKey1 = ");
        System.out.println(s);
        System.out.println();
        s = Encrypt.encryptString(PRIVATE_KEY );
        System.out.println("EncyrptedPrivateKey  = ");
        System.out.println(s);
    }

}
