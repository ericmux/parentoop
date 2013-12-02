import com.parentoop.network.api.messaging.MessageReader;
import com.parentoop.network.api.messaging.MessageType;
import com.parentoop.network.api.NodeServer;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;

import static org.junit.Assert.assertEquals;

public class MessageReaderTest {

    @Test
    public void testMessageReceived() throws IOException{

        final String sent = "test message";

        MessageReader reader = new MessageReader() {
            @Override
            public void read(MessageType type, ObjectInputStream inputStream, InetAddress senderAddress) throws IOException, ClassNotFoundException{
                assertEquals(inputStream.readObject(), sent);
            }
        };

        NodeServer listeningNode = new NodeServer(NodeServer.DEFAULT_PORT, reader);
        NodeServer dispatchingNode = new NodeServer(NodeServer.DEFAULT_PORT - 1, reader);

        listeningNode.startServer();

        dispatchingNode.dispatchMessage(MessageType.NOP, listeningNode.getServerAddress(), sent);
    }

    @Test
    public void testNullMessage() throws IOException{
        MessageReader reader = new MessageReader() {
            @Override
            public void read(MessageType type, ObjectInputStream inputStream) throws IOException, ClassNotFoundException{
                assertEquals(inputStream.readObject(), null);
            }
        };

        NodeServer listeningNode = new NodeServer(NodeServer.DEFAULT_PORT - 2, reader);
        NodeServer dispatchingNode = new NodeServer(NodeServer.DEFAULT_PORT - 3, reader);

        listeningNode.startServer();

        dispatchingNode.dispatchMessage(MessageType.NOP, listeningNode.getServerAddress(), null);
    }


}
