package com.parentoop.master.core;


import com.parentoop.core.api.InputReader;
import com.parentoop.core.api.Mapper;
import com.parentoop.core.api.Reducer;
import com.parentoop.network.api.MessageReader;
import com.parentoop.network.api.NodeServer;

import java.io.IOException;

public class MasterNode {

    private NodeServer mMasterServer;
    private MessageReader mMasterReader;
    private InputReader mInputReader;
    private Mapper mMapper;
    private Reducer mReducer;


    public MasterNode(int port, int backlog) throws IOException {
        mMasterServer = new NodeServer(port, mMasterReader,backlog);
    }

    public MasterNode(int port) throws IOException{
        mMasterServer = new NodeServer(port, mMasterReader, NodeServer.DEFAULT_BACKLOG);
    }

    public MasterNode() throws IOException{
        mMasterServer = new NodeServer(NodeServer.DEFAULT_PORT, mMasterReader, NodeServer.DEFAULT_BACKLOG);
    }

}
