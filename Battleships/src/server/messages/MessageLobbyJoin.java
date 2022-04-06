package server.messages;
/**
 * @author Lennard Boetcher
 * Class which represents a Lobby Join Request / response
 * Used Server -> Client and Client -> Server
 */
public class MessageLobbyJoin extends Message {

    private String lobbyID;
    private String name;

    public MessageLobbyJoin() {
        super(MessageType.LOBBY_JOIN);
    }

    public MessageLobbyJoin(String lobbyID, String name) {
        super(MessageType.LOBBY_JOIN);
        this.lobbyID = lobbyID;
        this.name = name;
    }

    public String getLobbyID() {
        return lobbyID;
    }

    public String getName() {
        return name;
    }

    @Override
    protected boolean parseFromNetworkMessage(String[] message) {
        if (message.length < 2)
            return false;
        this.lobbyID = message[0];
        this.name = message[1];
        return true;
    }

    @Override
    protected String getSendMessage() {
        return lobbyID + SEPARATOR_CHAR + name;
    }

    @Override
    protected String toPrintableString() {
        return lobbyID + SEPARATOR_CHAR + name;
    }
}
