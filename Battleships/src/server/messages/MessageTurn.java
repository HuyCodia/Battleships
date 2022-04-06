package server.messages;
/**
 * @author Lennard Boetcher
 * Class which represents a Message to tell the client it's his turn
 * Used Server -> Client
 */
public class MessageTurn extends Message {

    public MessageTurn() {
        super(MessageType.TURN);
    }

    @Override
    protected boolean parseFromNetworkMessage(String[] message) {
        return true;
    }

    @Override
    protected String getSendMessage() {
        return "";
    }

    @Override
    protected String toPrintableString() {
        return "";
    }
}
