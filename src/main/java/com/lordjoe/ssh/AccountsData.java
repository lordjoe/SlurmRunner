package com.lordjoe.ssh;

/**
 * This class is used to save and load accounts values (mail-private key) from a file as properties
 * Values are stored as username,,,,,privateKey (change this description if this changes) and are splited accordingly in return fo methods
 * com.lordjoe.ssh.AccountsMailKey
 * User: Simone
 * Date: 09/07/2020
 */

import com.lordjoe.utilities.Encrypt;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class AccountsData {

    public static final String DELIMITER = ",,,,,";


      public static final String ENCRYPTED_NAME = "EncryptedProperties.txt";

    private static Properties accountsTableX = new Properties();
    private static Map<String,AccountsData> accounts = new HashMap<>();

    public static String PRIVATE_KEY1_FILE1 = "/home/Steve/.ssh/HPC.ppk";// "private.ppk"; //id_rsa_list.ppk";
    public static final String PUBLIC_KEY1_FILE1 = "/home/Steve/.ssh/HPC.pub";// "public.pub";

    


    //Get the private key using mail as properties index
    //if the email is unknown return empty string (function containsMail shall be used before getting the key)
    public static String getPrivateKey(String email){
        guaranteeUsers();
        if (accounts.containsKey(email)){
           return accounts.get(email).getClearPrivateKey();
        }else
            return ("");
    }

    //Get the username to login into the HPC using mail as properties index
    //if the email is unknown return empty string (function containsMail shall be used before getting the key)
    public static String getUserName(String email){
        guaranteeUsers();
        if (accounts.containsKey(email)){
            AccountsData accountsData = accounts.get(email);
            return accountsData.userName;
          }else
            return ("");
    }

    //Return true if the email is known
    public static boolean containsMail(String email){
        guaranteeUsers();
        if (accounts.containsKey(email)){
            return true;
        }else
            return false;
    }

    protected static void guaranteeUsers() {
        if(accounts.isEmpty())   {

            try {
                // saving the properties in file
                //accountsTable.setProperty("lordjoe2000@gmail.com", PRIVATE_KEY);
                //System.out.println("Properties has been set in HashTable: " + accountsTable);
                //saveProperties(accountsTable);
                //System.out.println("Properties has been saved in: " + accountsTable);

                // loading the saved properties
                loadProperties(ENCRYPTED_NAME,accountsTableX);

                for (String o : accountsTableX.stringPropertyNames()) {
                    String email = o;
                    String value = accountsTableX.getProperty(o);
                    String[] items = value.split(DELIMITER);
            //        AccountsData ac = new AccountsData(email,items[0],Encrypt.encryptString(items[1]));
                         AccountsData ac = new AccountsData(email,items[0], items[1]);
                    accounts.put(email,ac);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);

            }

        }
    }

    //Return and array of strings with all emails for which a private key is present
    protected static String[] getAllUsersMails(){
        guaranteeUsers();
        Set<String> keys = accounts.keySet();
        Object[] keysArray = keys.toArray();

        String[] keysArrayStr= new String[keysArray.length];
        System.arraycopy(keysArray,0,keysArrayStr,0,keysArray.length);

        return(keysArrayStr);
    }



    static void loadProperties(String FileName,Properties p) throws IOException {
        File file=new File(FileName);
        FileInputStream fi = new FileInputStream(file);
        p.load(fi);
        fi.close();
        //System.out.println("After Loading properties: " + p);
    }

    public final String userName;
    public final String email;
    public final String encryptedPublicKey;

    public AccountsData(String userName, String email, String encryptedPublicKey) {
        this.userName = userName;
        this.email = email;
        this.encryptedPublicKey = encryptedPublicKey;
    }

    public String getClearPrivateKey()
    {
        return Encrypt.decryptString(encryptedPublicKey);
    }

    public String asTextString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(email);
        sb.append("=");
        sb.append(userName);
        sb.append(DELIMITER);
        sb.append(encryptedPublicKey);


        return sb.toString();
    }

    protected static void loadUsers(String name)
    {
        try {
            // saving the properties in file
            //accountsTable.setProperty("lordjoe2000@gmail.com", PRIVATE_KEY);
            //System.out.println("Properties has been set in HashTable: " + accountsTable);
            //saveProperties(accountsTable);
            //System.out.println("Properties has been saved in: " + accountsTable);

            // loading the saved properties
            loadProperties(name,accountsTableX);

            for (String o : accountsTableX.stringPropertyNames()) {
                String email = o;
                String value = accountsTableX.getProperty(o);
                String[] items = value.split(DELIMITER);
                 AccountsData ac = new AccountsData(email,items[0],Encrypt.encryptString(items[1]));
          //      AccountsData ac = new AccountsData(email,items[0], items[1]);
                accounts.put(email,ac);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    protected static  void createEncryptedUsers(String propFileName)
    {
        try {
            loadUsers(propFileName);

            PrintWriter out = new PrintWriter(new FileWriter(ENCRYPTED_NAME));
            for (String o : accounts.keySet()) {
                String email = o;
                AccountsData value = accounts.get(o);
                out.println(value.asTextString());
            }
            out.close();
            // Validate keys
            for (String o : accounts.keySet()) {
                String email = o;
                String privateKey = getPrivateKey(o);
                String property = accountsTableX.getProperty(o);
                String[] items = property.split(DELIMITER);
                String item = items[1];
                if(!privateKey.equals(item)) {
                    String[] lines = privateKey.split("\n");
                    String[] lines1 = item.split("\n");
                    for (int i = 0; i < lines1.length; i++) {
                        String s1 = lines[i];
                        String s = lines1[i];
                        if(!s1.equals(s))
                            throw new UnsupportedOperationException("Fix This bad key");
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    public static void main(String[] args) throws IOException {

        System.out.println("Working Directory = " + System.getProperty("user.dir"));


        System.out.println("Steven username is "+getUserName("lordjoe2000@gmail.com"));
        System.out.println("Steven Private key is "+getPrivateKey("lordjoe2000@gmail.com"));
        System.out.println("Simone username is "+getUserName("simone.zorzan@list.lu"));
        System.out.println("Simone Private key is "+getPrivateKey("simone.zorzan@list.lu"));
    }
}
