package com.parentoop.master;

import com.parentoop.master.application.MasterApplication;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;

public class MasterMain {

    public static void main(String[] args) throws IOException {
        Scanner inputScanner = new Scanner(System.in);

        System.out.println("Starting server...");
        MasterApplication application = new MasterApplication(Arrays.asList(args).contains("-v"));
        application.start();

        System.out.println();
        System.out.println("Server online. Your address is: " + InetAddress.getLocalHost().getHostAddress());
        System.out.println("Provide the above address to slave machines for connection and connect a client to this server in order to execute map-reduce tasks.");
        System.out.println();

        while(true) {
            System.out.print("Server running. Type 'q' to shutdown: ");
            String in = inputScanner.nextLine();
            if (in.trim().equals("q")) break;
        }
        application.shutdown();
    }
}
