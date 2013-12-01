import com.parentoop.network.api.MessageReader;
import com.parentoop.network.api.MessageType;
import com.parentoop.network.api.NodeServer;
import org.junit.Test;

import java.io.IOException;
import java.io.ObjectInputStream;

import static org.junit.Assert.assertEquals;

public class MessageReaderTest {

    @Test
    public void testMessageReceived() throws IOException{

        final String sent = "test message";

        MessageReader reader = new MessageReader() {
            @Override
            public void read(MessageType type, ObjectInputStream inputStream) throws IOException, ClassNotFoundException{
                assertEquals(inputStream.readObject(), sent);
            }
        };

        NodeServer listeningNode = new NodeServer(NodeServer.DEFAULT_PORT, reader);
        NodeServer dispatchingNode = new NodeServer(NodeServer.DEFAULT_PORT - 1, reader);

        listeningNode.startServer();

        dispatchingNode.dispatchMessage(MessageType.NOP, listeningNode.getServerAddress(), sent);
    }


}
