package com.parentoop.client.masterproxy;

import com.parentoop.core.networking.Messages;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.network.api.messaging.MessageHandler;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MasterMessageHandler implements MessageHandler{

    PrintStream mPrintStream;
    String mOutputName;

    public MasterMessageHandler(PrintStream printStream, String outputName) {
        mPrintStream = printStream;
    }

    @Override
    public final void handle(Message message, PeerCommunicator sender) {
        int type = message.getCode();
        switch (type) {
            case Messages.LOAD_JAR:
                handleLoadJar(message, sender);
                break;
            case Messages.FAILURE:
                handleFailure(message, sender);
                break;
            case Messages.MAPPING:
                handleMapping(message, sender);
                break;
            case Messages.REDUCING:
                handleReducing(message, sender);
                break;
            case Messages.SEND_RESULT:
                handleSendResult(message, sender);
                break;
        }
    }

    protected void handleLoadJar(Message message, PeerCommunicator sender){
        mPrintStream.println(message.getData());
    }

    protected void handleFailure(Message message, PeerCommunicator sender){
        mPrintStream.println("Task failed. Master's node response:");
        mPrintStream.println((String) message.getData());
        System.exit(0);
    }

    protected void handleMapping(Message message, PeerCommunicator sender){
        mPrintStream.println("\rMapping phase" + message.getData() + "% complete...");
    }

    protected void handleReducing(Message message, PeerCommunicator sender){
        mPrintStream.println("\rReducing phase" + message.getData() + "% complete...");
    }

    protected void handleSendResult(Message message, PeerCommunicator sender){
        mPrintStream.println("Task finished. Writing" + mOutputName + "to local directory.");
        Path receivedResult = message.getData();
        try {
            Files.copy(receivedResult, Paths.get("/result"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
