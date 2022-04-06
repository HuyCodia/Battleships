package server;

import server.messages.*;

/**
 * @author Lennard Boetcher
 * Main Player Network Handler, which is the root of the Network Messages for each Client. This is the Server Part of a Client Connection
 * Depending on the Message sent, it will be passed to the Lobby the Client is connected to.
 * Messages like LOBBY_CREATE, LOBBY_LEAVE and LOBBY_JOIN will be handled here.
 */
public class PlayerNetworkHandler implements ConnectionCallback {

    private Lobby currentLobby = null;
    private MainServer mainServer;
    private ConnectionThread connectionThread;
    private String name;

    public PlayerNetworkHandler(MainServer server) {
        this.mainServer = server;
    }

    public void setConnectionThread(ConnectionThread connectionThread) {
        this.connectionThread = connectionThread;
    }

    /**
     * Handle disconnect Event of the Client. Will pass a Disconnect event to the Lobby, if the Client was in a Lobby
     * @param con Connection of the Client
     */
    @Override
    public void onDisconnectEvent(ConnectionThread con) {
        if (currentLobby != null)
            currentLobby.onDisconnectEvent(this);
        currentLobby = null;
    }

    /**
     * Handles all Client Network Messages received. It will handle LOBBY_CREATE, LOBBY_LEAVE and LOBBY_JOIN.
     * Others will be forwarded to the Lobby, if there is a Lobby. Otherwise they will be ignored
     * @param con Connection of the Client
     * @param message The Network Message received
     */
    @Override
    public void onMessageReceived(ConnectionThread con, Message message) {
        System.out.println("[" + Thread.currentThread().getName() + "] Network Message Received: " + message.getType());
        if (processLobbyCreate(con, message) || processLobbyJoin(con, message) || processLobbyLeave(con, message))
            return;
        if (currentLobby != null)
            currentLobby.onMessageReceived(this, message);
    }

    /**
     * Will handles a Lobby Leave Message if the given Network Message is of type LOBBY_LEAVE
     * @param con Connection of the Client
     * @param message The Network Message received
     * @return if the Message was handled or if it was not handled (not of type LOBBY_LEAVE)
     */
    private boolean processLobbyLeave(ConnectionThread con, Message message) {
        if (message.getType() != MessageType.LOBBY_LEAVE)
            return false;
        if (currentLobby == null) {
            con.sendMessage(new MessageError(MessageType.LOBBY_LEAVE, "You are in no Lobby !"));
        } else {
            currentLobby.onDisconnectEvent(this);
            currentLobby = null;
            con.sendMessage(new MessageLobbyLeave(true));
        }
        return true;
    }

    /**
     * Will handles a Lobby Join Message if the given Network Message is of type LOBBY_JOIN
     * @param con Connection of the Client
     * @param message The Network Message received
     * @return if the Message was handled or if it was not handled (not of type LOBBY_JOIN)
     */
    protected boolean processLobbyJoin(ConnectionThread con, Message message) {
        if (message.getType() != MessageType.LOBBY_JOIN)
            return false;
        MessageLobbyJoin join = (MessageLobbyJoin) message;
        name = join.getName();
        if (currentLobby != null) {
            con.sendMessage(new MessageError(MessageType.LOBBY_JOIN, "You are already in a Lobby !"));
        } else {
            String lobbyID = join.getLobbyID();
            Lobby lobby = mainServer.getLobbyByID(lobbyID);
            if (lobby == null) {
                con.sendMessage(new MessageError(MessageType.LOBBY_JOIN, "This Lobby does not exist !"));
            } else {
                boolean ok = lobby.tryJoinLobby(this);
                if (!ok) {
                    con.sendMessage(new MessageError(MessageType.LOBBY_JOIN, "The Lobby is full or already started !"));
                } else {
                    currentLobby = lobby;
                    con.sendMessage(new MessageLobbyJoin(lobbyID, join.getName()));
                    return true;
                }
            }
        }
        return true;
    }

    /**
     * Will handles a Lobby Create Message if the given Network Message is of type LOBBY_CREATE
     * @param con Connection of the Client
     * @param message The Network Message received
     * @return if the Message was handled or if it was not handled (not of type LOBBY_CREATE)
     */
    private boolean processLobbyCreate(ConnectionThread con, Message message) {
        if (message.getType() != MessageType.LOBBY_CREATE)
            return false;
        MessageLobbyCreate lobbyCreate = (MessageLobbyCreate) message;
        name = lobbyCreate.getName();
        if (currentLobby != null) {
            con.sendMessage(new MessageError(MessageType.LOBBY_CREATE, "You are already in a Lobby !"));
        } else {
            Lobby lobby = mainServer.createLobby();
            lobby.tryJoinLobby(this);
            con.sendMessage(new MessageLobbyJoin(lobby.getLobbyID(), lobbyCreate.getName()));
            currentLobby = lobby;
        }
        return true;
    }

    /**
     * Get the Connection to send Network Messages
     * @return current ConnectionThread
     */
    public ConnectionThread getConnection() {
        return connectionThread;
    }

    public String getName() {
        return name;
    }

}
