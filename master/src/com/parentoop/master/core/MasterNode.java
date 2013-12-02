package com.parentoop.master.core;


import com.parentoop.core.api.InputReader;
import com.parentoop.core.api.Mapper;
import com.parentoop.core.api.Reducer;
import com.parentoop.network.api.NodeServer;
import com.parentoop.network.api.messaging.MessageHandler;

import java.io.IOException;

public class MasterNode {

    private NodeServer mMasterServer;
    private MessageHandler mMasterReader;
    private InputReader mInputReader;
    private Mapper mMapper;
    private Reducer mReducer;


    public MasterNode(int port, int backlog) throws IOException {
        mMasterServer = new NodeServer(port, mMasterReader,backlog);
    }

    public MasterNode(int port) throws IOException{
    }

    public MasterNode() throws IOException{
    }

}
