package server.messages;

import server.Lobby;
import server.LobbyState;
/**
 * @author Lennard Boetcher
 * Class which represents a change of the Lobby State
 * Used Server -> Client
 */
public class MessageLobbyState extends Message {

    private LobbyState state;
    private String extra;

    public MessageLobbyState(LobbyState state, String extra) {
        super(MessageType.LOBBY_STATE);
        this.extra = extra;
        this.state = state;
    }

    public MessageLobbyState(LobbyState state) {
        this(state, "");
    }

    public MessageLobbyState() {
        super(MessageType.LOBBY_STATE);
    }

    public LobbyState getState() {
        return state;
    }

    @Override
    protected boolean parseFromNetworkMessage(String[] message) {
        if (message.length < 2)
            return false;
        state = LobbyState.valueOf(message[0]);
        extra = message[1];
        return true;
    }

    @Override
    protected String getSendMessage() {
        return (state == null ? "null" : state.name()) + SEPARATOR_CHAR + extra;
    }

    @Override
    protected String toPrintableString() {
        return (state == null ? "null" : state.name()) + SEPARATOR_CHAR + extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getExtra() {
        return extra;
    }
}
