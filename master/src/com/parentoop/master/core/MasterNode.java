package com.parentoop.master.core;

import com.parentoop.client.ui.InputReader;
import com.parentoop.client.ui.Mapper;
import com.parentoop.client.ui.Reducer;
import com.parentoop.network.api.messaging.MessageReader;
import com.parentoop.network.api.messaging.MessageType;
import com.parentoop.network.api.NodeServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;

public class MasterNode {

    private NodeServer mMasterServer;
    private MessageReader mMasterReader;
    private InputReader mInputReader;
    private Mapper mMapper;
    private Reducer mReducer;


    public MasterNode(int port, int backlog) throws IOException {
        setUpMessageReader(mMasterReader);
        mMasterServer = new NodeServer(port, mMasterReader,backlog);
    }

    public MasterNode(int port) throws IOException{
        setUpMessageReader(mMasterReader);
        mMasterServer = new NodeServer(port, mMasterReader, NodeServer.DEFAULT_BACKLOG);
    }

    public MasterNode() throws IOException{
        setUpMessageReader(mMasterReader);
        mMasterServer = new NodeServer(NodeServer.DEFAULT_PORT, mMasterReader, NodeServer.DEFAULT_BACKLOG);
    }

    private void setUpMessageReader(MessageReader reader){
        mMasterReader = new MessageReader() {
            @Override
            public void read(MessageType type, ObjectInputStream inputStream, InetAddress senderAddress) throws IOException, ClassNotFoundException {
                //TODO
            }
        };
    }
}
