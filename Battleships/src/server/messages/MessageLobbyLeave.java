package server.messages;
/**
 * @author Lennard Boetcher
 * Class which represents Lobby Leave Request / Response
 * Used Server -> Client and Client -> Server
 */
public class MessageLobbyLeave extends Message {

    private boolean initiatedByClient;

    public MessageLobbyLeave(boolean initiatedByClient) {
        super(MessageType.LOBBY_LEAVE);
        this.initiatedByClient = initiatedByClient;
    }

    public MessageLobbyLeave() {
        this(false);
    }

    @Override
    protected boolean parseFromNetworkMessage(String[] message) {
        if (message.length < 1)
            return false;
        initiatedByClient = message[0].equals("1");
        return true;
    }

    @Override
    protected String getSendMessage() {
        return initiatedByClient ? "1" : "0";
    }

    @Override
    protected String toPrintableString() {
        return "Initiated By Client ? " + initiatedByClient;
    }
}
