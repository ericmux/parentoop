package com.parentoop.client.ui;


import com.parentoop.client.masterproxy.MasterMessageHandler;
import com.parentoop.client.masterproxy.MasterProxy;
import com.parentoop.core.networking.Messages;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class ClientPrompt {

    public static final String START_TASK_COMMAND = "run";
    public static final String DEFAULT_OUTPUT_NAME = "out.txt";


    private static String mTaskConfiguratorName;
    private static Path mJarPath;
    private static InetAddress mHostAddress = null;
    private static String mOutputName;

    public static void main(String[] args) throws IOException {
         startUIScript(System.in, System.out, args);
    }

    public static void startUIScript(InputStream inputStream, PrintStream printStream, String[] args) throws IOException {


        Scanner scanner = new Scanner(inputStream).useDelimiter("\\n");
        if(args.length < 3){
            args = new String[4];
            printStream.print("Please indicate the relative path to the .jar file containing your MapReduce settings: ");
            args[0] = scanner.next();

            printStream.print("Task configuration class name: ");
            args[1] = scanner.next();

            printStream.print("Host's IPAddress: ");
            args[2] = scanner.next();

            printStream.print("Output file name (or press Enter to use default name \"" + DEFAULT_OUTPUT_NAME + "\"): ");
            args[3] = scanner.next();
            if(args[3].isEmpty()) args[3] = DEFAULT_OUTPUT_NAME;

        } else {
            if (args.length == 3) {
                mOutputName = DEFAULT_OUTPUT_NAME;
            }
            else {
                mOutputName = args[4];
            }
        }

        mJarPath = Paths.get(args[0]);
        mTaskConfiguratorName = args[1];
        try {
            mHostAddress = InetAddress.getByName(args[2]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        mHostAddress = InetAddress.getLocalHost();

        printStream.println("Sending " + mJarPath + " to " + mHostAddress + "...");

        printStream.println(mHostAddress);
        MasterProxy masterProxy = new MasterProxy(mHostAddress, new MasterMessageHandler(printStream, mOutputName));
        masterProxy.sendFile(Messages.LOAD_JAR, mJarPath);

        printStream.print("Type \"run\" to start the task: ");

        while(!scanner.next().equals(START_TASK_COMMAND)){
            printStream.println("Invalid command, please try again.");
        }
        masterProxy.dispatchMessage(Messages.START_TASK, mTaskConfiguratorName);

        printStream.println("Task started!");

        printStream.close();
        scanner.close();
    }


}
