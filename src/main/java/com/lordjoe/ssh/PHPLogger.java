package com.lordjoe.ssh;

import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * com.lordjoe.ssh.PHPLogger
 * User: Steve
 * This is a class to look into files passed in by php
 * Date: 3/27/20
 */
public class PHPLogger {
    public static void main(String[] args) throws Exception {
        PrintWriter out = new PrintWriter(new FileWriter("PHPLoggerFile.txt"));
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
             out.print(arg + " ");
        }
        out.close();
    }
}
