package com.parentoop.slave.node.phases;

import com.parentoop.core.data.DataPool;
import com.parentoop.core.data.Datum;
import com.parentoop.core.networking.Messages;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

public class ReducePhase extends Phase {

    private final ExecutorService mReducersThreadPool = Executors.newCachedThreadPool();
    private final ExecutorService mValueSendersThreadPool = Executors.newCachedThreadPool();
    private Collection<InetAddress> mSlaveAddresses;
    private List<String> mKeys;
    private Map<String, DataPool> mValues = new HashMap<>();
    private Map<String, Future<Serializable>> mResults = new HashMap<>();
    private int mRequests = 0;

    @Override
    public void execute(Message message, PeerCommunicator sender) {
        switch (message.getCode()) {
            case Messages.LOAD_SLAVE_ADDRESSES: // param = { address }
                mSlaveAddresses = Arrays.asList(message.<InetAddress[]>getData());
                mRequests = mSlaveAddresses.size();
                break;
            case Messages.REDUCE_KEYS: // param = { key }
                mKeys = Arrays.asList(message.<String[]>getData());
                startReduce();
                break;
            case Messages.KEY_VALUE: // param = (key, value)
                Datum datum = message.getData();
                //noinspection unchecked
                mValues.get(datum.getKey()).yield(datum.getValue());
                break;
            case Messages.REQUEST_VALUES: // param = key
                ValueSender task = new ValueSender(sender, message.<String>getData());
                mValueSendersThreadPool.submit(task);
                break;
            case Messages.END_OF_DATA_STREAM: // no param
                // TODO: Make it as function of key so as to return results as soon as they appear
                mRequests--;
                if (mRequests == 0) {
                    collect();
                    terminate();
                }
        }
    }

    private void terminate() {
        mStorage.terminate();
        closeSlaves();
        nextPhase(LoadPhase.class);
    }

    private void startReduce() {
        for (String key : mKeys) {
            DataPool pool = new DataPool();
            mValues.put(key, pool);
            requestValues(key);
            Future<Serializable> result = mReducersThreadPool.submit(new ReduceTask(key, pool));
            mResults.put(key, result);
        }
    }

    private void requestValues(String key) {
        for (InetAddress slaveAddress : mSlaveAddresses) {
            mRequests++;
            dispatchMessageToSlave(slaveAddress, new Message(Messages.REQUEST_VALUES, key));
        }
    }

    public void collect() {
        for (Map.Entry<String, Future<Serializable>> entry : mResults.entrySet()) {
            Future<Serializable> future = entry.getValue();
            try {
                Datum datum = new Datum(entry.getKey(), future.get());
                dispatchMessageToMaster(new Message(Messages.RESULT_PAIR, datum));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        dispatchMessageToMaster(new Message(Messages.END_OF_RESULT_STREAM));
    }

    private class ReduceTask implements Callable<Serializable> {

        private final String mKey;
        private final DataPool mDataPool;

        private ReduceTask(String key, DataPool dataPool) {
            mKey = key;
            mDataPool = dataPool;
        }

        @Override
        public Serializable call() throws Exception {
            //noinspection unchecked
            return mReducer.reduce(mKey, mDataPool);
        }
    }

    private class ValueSender implements Runnable {

        private final String mKey;
        private final PeerCommunicator mDestination;

        private ValueSender(PeerCommunicator destination, String key) {
            mDestination = destination;
            mKey = key;
        }

        @Override
        public void run() {
            try {
                for (Serializable value : mStorage.read(mKey)) {
                    mDestination.dispatchMessage(new Message(Messages.KEY_VALUE, value));
                }
                mDestination.dispatchMessage(new Message(Messages.END_OF_DATA_STREAM));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
