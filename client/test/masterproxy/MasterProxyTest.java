package masterproxy;

import com.parentoop.client.masterproxy.MasterMessageHandler;
import com.parentoop.client.masterproxy.MasterProxy;
import com.parentoop.client.ui.ClientPrompt;
import com.parentoop.core.networking.Messages;
import com.parentoop.core.networking.Ports;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.NodeServer;
import com.parentoop.network.api.PeerCommunicator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MasterProxyTest {

    private static final long FAKE_NETWORK_THRESHOLD = 100;
    private static final Path JAR_PATH = Paths.get("commonio.jar");
    private static final Path OUTPUT_PATH = Paths.get("out.txt");

    private NodeServer mNodeServer;
    private MasterProxy mMasterProxy;

    private ReentrantLock mLock;
    private Condition mCondition;
    private Message mReceivedMessage;
    private PrintStream mPrintStream;

    @BeforeClass
    public static void init() throws IOException {
        JAR_PATH.toFile().createNewFile();
        JAR_PATH.toFile().deleteOnExit();
        OUTPUT_PATH.toFile().createNewFile();
        OUTPUT_PATH.toFile().deleteOnExit();
    }

    @Before
    public void setUp() throws IOException, InterruptedException {
        mPrintStream = System.out;
        mNodeServer = new NodeServer(Ports.MASTER_CLIENT_PORT, new MessageReceiveSignaler(mPrintStream));
        mNodeServer.startServer();

        mMasterProxy = new MasterProxy(InetAddress.getLocalHost(), new MessageReceiveSignaler(mPrintStream));
        Thread.sleep(FAKE_NETWORK_THRESHOLD);

        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
    }


    @After
    public void tearDown() throws IOException, InterruptedException {
        if (mMasterProxy != null) mMasterProxy.shutdown();
        if (mNodeServer != null) mNodeServer.shutdown();
        Thread.sleep(FAKE_NETWORK_THRESHOLD);

        mLock = null;
        mReceivedMessage = null;
        mCondition = null;
    }

    @Test
    public void testLocalJarExists() throws InterruptedException {
        assertTrue(JAR_PATH.toFile().exists());
        Thread.sleep(FAKE_NETWORK_THRESHOLD);
        Thread.sleep(FAKE_NETWORK_THRESHOLD);
    }


    @Test
    public void testClientJarPathReceived() throws IOException, InterruptedException {
        mMasterProxy.dispatchMessage(Messages.LOAD_JAR, JAR_PATH.toFile());
        mMasterProxy.sendFile(Messages.LOAD_JAR, JAR_PATH);
        waitForMessageReceive();

        assertEquals(JAR_PATH, mReceivedMessage.getData());

    }

    @Test
    public void testSendResult() throws IOException, InterruptedException {
        mNodeServer.broadcastMessage(new Message(Messages.SEND_RESULT,OUTPUT_PATH));
        waitForMessageReceive();

        assertTrue(Paths.get("client_res").resolve(OUTPUT_PATH).toAbsolutePath().toFile().exists());
    }



    private void waitForMessageReceive() throws InterruptedException {
        mLock.lock();
        try {
            mCondition.await(FAKE_NETWORK_THRESHOLD, TimeUnit.MILLISECONDS);
        } finally {
            mLock.unlock();
        }
    }

    private class MessageReceiveSignaler extends MasterMessageHandler {
        public MessageReceiveSignaler(PrintStream printStream) {
            super(printStream, ClientPrompt.DEFAULT_OUTPUT_NAME);
        }

        @Override
        protected void handleLoadJar(Message message, PeerCommunicator sender) {
            if(mReceivedMessage == null){
                mReceivedMessage = message;
                mReceivedMessage = new Message(mReceivedMessage.getCode(), ((File) mReceivedMessage.getData()).toPath());
            } else {
                Path jarRead = message.getData();
                try {
                    Files.move(jarRead, ((File) mReceivedMessage.getData()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    assertTrue(((File) mReceivedMessage.getData()).exists());
                }
            }
        }

        @Override
        protected void handleFailure(Message message, PeerCommunicator sender) {
            super.handleFailure(message, sender);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void handleMapping(Message message, PeerCommunicator sender) {
            super.handleMapping(message, sender);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void handleReducing(Message message, PeerCommunicator sender) {
            super.handleReducing(message, sender);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void handleCollecting(Message message, PeerCommunicator sender) {
            super.handleCollecting(message, sender);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void handleSendResult(Message message, PeerCommunicator sender) {
            Path receivedResult = message.getData();
            Path copiedPath;
            System.out.println(Paths.get(""));
            try {
                Files.createDirectory(Paths.get("client_res")).toFile().deleteOnExit();
                copiedPath = Files.copy(receivedResult, Paths.get("client_res").resolve(OUTPUT_PATH));
                copiedPath.toFile().deleteOnExit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
