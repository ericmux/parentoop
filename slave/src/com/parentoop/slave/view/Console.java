package com.parentoop.slave.view;

import com.parentoop.slave.executor.PhaseExecutor;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

public class Console {

    private static boolean sVerbose = false;

    public static void println(String string) {
        if (sVerbose) System.out.println(string); // Fix
    }

    public static void print(String string) {
        if (sVerbose) System.out.print(string); // Fix
    }

    public static void main(String[] args) {
        Console console = new Console(System.in, System.out, args);
        console.initialize();
    }

    private InetAddress mMasterAddress;
    private final InputStream mInput;
    private final PrintStream mOutput;

    private Console(InputStream in, PrintStream out, String[] args) {
        mInput = in;
        mOutput = out;
        setArguments(args);
    }

    private void setArguments(String[] args) {
        for (String arg : args) {
            arg = arg.trim();
            if (Arrays.asList("-v", "--verbose").contains(arg)) {
                sVerbose = true;
            } else {
                setMasterAddress(arg);
            }
        }
    }

    private boolean setMasterAddress(String host) {
        try {
            mMasterAddress = InetAddress.getByName(host);
            return true;
        } catch (UnknownHostException e) {
            mMasterAddress = null;
            return false;
        }
    }


    public void initialize() {
        if (mMasterAddress == null) askAddress();
        mOutput.print("Address valid, press enter to connect... ");
        boolean connected = false;
        while (!connected) {
            try {
                PhaseExecutor executor = new PhaseExecutor(mMasterAddress);
                executor.initialize();
                connected = true;
            } catch (IOException e) {
                mOutput.println("The address entered does not exist, try again.");
                askAddress();
            }
        }
        mOutput.println("It should be connected.");
    }

    private void askAddress() {
        Scanner scanner = new Scanner(mInput).useDelimiter("\\n");
        mOutput.print("Enter the Master node IP address: ");
        String address = scanner.next();
        while (!setMasterAddress(address)) {
            mOutput.print("The address entered is not valid, enter again: ");
            address = scanner.next();
        }
    }
}
