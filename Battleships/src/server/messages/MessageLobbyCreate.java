package server.messages;
/**
 * @author Lennard Boetcher
 * Class which represents a Request to create a Lobby
 * Used Client -> Server
 */
public class MessageLobbyCreate extends Message {

    private String name;

    public MessageLobbyCreate() {
        super(MessageType.LOBBY_CREATE);
    }

    public MessageLobbyCreate(String name) {
        super(MessageType.LOBBY_CREATE);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    protected boolean parseFromNetworkMessage(String[] message) {
        if (message.length < 1)
            return false;
        this.name = message[0];
        return true;
    }

    @Override
    protected String getSendMessage() {
        return getName();
    }

    @Override
    protected String toPrintableString() {
        return getName();
    }
}
