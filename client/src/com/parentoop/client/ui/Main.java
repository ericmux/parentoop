package com.parentoop.client.ui;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String taskConfigBuilderName;
        Path pathToJar;
        InetAddress hostAddress = null;

        Scanner scanner = new Scanner(System.in);
        if(args.length < 3){
            args = new String[3];
            System.out.print("Please indicate the relative path to the .jar file containing your MapReduce settings: ");
            args[0] = scanner.next();

            System.out.print("Task configuration class canonical name: ");
            args[1] = scanner.next();

            System.out.print("Host's IPAddress: ");
            args[2] = scanner.next();

        }

        pathToJar = Paths.get(args[0]);
        taskConfigBuilderName = args[1];
        try {
            hostAddress = InetAddress.getByName(args[2]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        //TODO send JAR + taskConfigBuilderName to Master, specified by the hostAddress.

    }


}
