package com.lordjoe.ssh;

/**
 * This class is used to save and load accounts values (mail-private key) from a file as properties
 * Values are stored as username,,,,,privateKey (change this description if this changes) and are splited accordingly in return fo methods
 * com.lordjoe.ssh.AccountsMailKey
 * User: Simone
 * Date: 09/07/2020
 * com.lordjoe.ssh.AccountsData
 */

import com.lordjoe.utilities.Encrypt;

import java.io.*;
import java.util.*;

public class AccountsData {

    public static final String DELIMITER = ",,,,,";


    public static final String ENCRYPTED_NAME = "/opt/blastserver/EncryptedProperties.txt";

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

            // saving the properties in file
            //accountsTable.setProperty("lordjoe2000@gmail.com", PRIVATE_KEY);
            //System.out.println("Properties has been set in HashTable: " + accountsTable);
            //saveProperties(accountsTable);
            //System.out.println("Properties has been saved in: " + accountsTable);

            // loading the saved properties
            loadEncryptedFile(ENCRYPTED_NAME,accountsTableX,accounts)  ;
                /*
                loadProperties(ENCRYPTED_NAME,accountsTableX);

                for (String o : accountsTableX.stringPropertyNames()) {
                    String email = o;
                    String value = accountsTableX.getProperty(o);
                    String[] items = value.split(DELIMITER);
                    //        AccountsData ac = new AccountsData(email,items[0],Encrypt.encryptString(items[1]));
                    AccountsData ac = new AccountsData(email,items[0], items[1]);
                    accounts.put(email,ac);
                }

                 */

        }
    }

    private static void loadEncryptedFile(String file,Properties temp,Map<String,AccountsData> decrypted) {
        try {
            decrypted.clear();
            loadProperties(file,temp);

            for (String o : temp.stringPropertyNames()) {
                String name = o;
                String value = temp.getProperty(o);
                String[] items = value.split(DELIMITER);
                //        AccountsData ac = new AccountsData(email,items[0],Encrypt.encryptString(items[1]));
                AccountsData ac = new AccountsData(name,items[0], items[1]);
                decrypted.put(name,ac);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }

    private static void loadUnEncryptedFile(String file,Properties temp,Map<String,AccountsData> decrypted) {
        try {
            decrypted.clear();
            loadProperties(file,temp);

            for (String o : temp.stringPropertyNames()) {
                String name = o;
                String value = temp.getProperty(o);
                String[] items = value.split(DELIMITER);
                String encryptedPublicKey = encryptAsNeeded(items[1]) ;
                AccountsData ac = new AccountsData(name,items[0], encryptedPublicKey);
                decrypted.put(name,ac);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    public static String encryptAsNeeded(String in)   {
        if(in.contains("Comment: rsa-key-"))
            return Encrypt.encryptString(in);
        return in;
    }


    public static String decryptAsNeeded(String in)   {
        if(in.contains("Comment: rsa-key-"))
            return in;
        return Encrypt.decryptString(in);
    }

    //Return and array of strings with all emails for which a private key is present
    public static String[] getAllUsersMails(){
        guaranteeUsers();
        List<String>  emails = new ArrayList<>();
        for (String s : accounts.keySet()) {
            AccountsData accountsData = accounts.get(s);
            emails.add(accountsData.email);
        }
        Collections.sort(emails);
        String[] strings = emails.toArray(new String[] {});
        return strings;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountsData that = (AccountsData) o;
        return Objects.equals(userName, that.userName) &&
                Objects.equals(email, that.email) &&
                Objects.equals(encryptedPublicKey, that.encryptedPublicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, email, encryptedPublicKey);
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
        loadUsers(name,accountsTableX,accounts);

    }

    protected static void loadUsers(String name,Properties p1,Map<String,AccountsData> users) {
        try {
            // saving the properties in file
            //accountsTable.setProperty("lordjoe2000@gmail.com", PRIVATE_KEY);
            //System.out.println("Properties has been set in HashTable: " + accountsTable);
            //saveProperties(accountsTable);
            //System.out.println("Properties has been saved in: " + accountsTable);

            // loading the saved properties
            loadProperties(name,p1);

            for (String o : p1.stringPropertyNames()) {
                String email = o;
                String value = p1.getProperty(o);
                String[] items = value.split(DELIMITER);
                AccountsData ac = new AccountsData(email,items[0],items[1]);
                //      AccountsData ac = new AccountsData(email,items[0], items[1]);
                users.put(email,ac);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    /**
     *
     * @param propFileName
     */
    protected static  void createDecryptedUsers(String outfile)
    {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(outfile));
            guaranteeUsers();
            for (String o : accounts.keySet()) {
                String name = o;
                AccountsData value = accounts.get(o);
                out.print(o);
                out.print("=");
                out.print(value.email);
                out.print(DELIMITER);
                String s = decryptAsNeeded(value.encryptedPublicKey);
                s = s.replace("\n","\\n");
                out.print(s);
                out.println();
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     *
     * @param propFileName
     * @param outfile
     */
    protected static  void createEncryptedUsers(String propFileName,String outfile)
    {
        try {
            loadUnEncryptedFile(propFileName,accountsTableX,accounts) ;

            PrintWriter out = new PrintWriter(new FileWriter(outfile));
            for (Object o : accountsTableX.keySet()) {
                String email = (String)o;
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
                item = decryptAsNeeded(item);
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


    public static void usage(String[] args)  {
        System.out.println("usage <filename> create open file from encrypted at /opt/blastserver ");
        System.out.println("usage <filename> <outfile> create ebecrypted file from open file");
    }

    private static void validateUsers(String arg, String arg1, String arg2) {
        Properties p0 = new Properties();
        Properties p1 = new Properties();
        Map<String,AccountsData> decrypted0 = new HashMap<>();
        Map<String,AccountsData> decrypted = new HashMap<>();
        guaranteeUsers();
        loadUnEncryptedFile(arg,p0,decrypted0);
        loadEncryptedFile(arg1,p1,decrypted);
        assert(p0.size() == p1.size());
        assert(decrypted0.size() == decrypted.size());
        for (String s : decrypted.keySet()) {
            AccountsData ac1 = decrypted.get(s);
            AccountsData ac2 = decrypted0.get(s);
            assert(ac1.equals(ac2) );
        }
    }


    public static void main(String[] args) throws IOException {
        if(true) {
            switch (args.length) {
                case 1:
                    createDecryptedUsers(args[0]);
                    return;
                case 2:
                    createEncryptedUsers(args[0],args[1]);
                    return;
                case 3:
                    validateUsers(args[0],args[1],args[2]);
                    return;
                default:
                    usage(args);
                    return;
            }
        }

        System.out.println("Working Directory = " + System.getProperty("user.dir"));


        System.out.println("Steven username is "+getUserName("lordjoe2000@gmail.com"));
        System.out.println("Steven Private key is "+getPrivateKey("lordjoe2000@gmail.com"));
        System.out.println("Simone username is "+getUserName("simone.zorzan@list.lu"));
        System.out.println("Simone Private key is "+getPrivateKey("simone.zorzan@list.lu"));
    }

}
