package com.parentoop.client.masterproxy;

import com.parentoop.core.networking.Ports;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.NodeClient;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.file.Path;

public class MasterProxy {

    private NodeClient mNodeClient;
    private MasterMessageHandler mMessageHandler;
    private InetAddress mMasterAddress;

    public MasterProxy(InetAddress address, MasterMessageHandler masterMessageHandler) throws IOException {
        mMessageHandler = masterMessageHandler;
        mMasterAddress = address;
        mNodeClient = new NodeClient(mMasterAddress, Ports.MASTER_CLIENT_PORT, masterMessageHandler);
    }

    public void sendFile(int type, Path path) throws IOException {
        Message file = new Message(type, path);
        mNodeClient.dispatchMessage(file);
    }

    public void dispatchMessage(int type, Serializable message) throws IOException {
        mNodeClient.dispatchMessage(new Message(type, message));
    }

    public void shutdown() throws IOException {
        mNodeClient.shutdown();
    }







}
