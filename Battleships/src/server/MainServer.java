package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Lennard Boetcher
 * Main Server Class. Handles incomming connections from Client and creates new PlayerNetworkHanlders for each Client
 * Keeps Track of all Lobbies and their Lobby Codes
 */
public class MainServer {

    public static final int PORT = 5544;
    private ServerSocket socket;
    private Thread serverThread;
    private ConnectionCallback callback;
    private List<Lobby> lobbies = new ArrayList<>();

    public static void main(String[] args) {
        MainServer server = new MainServer(null);
        server.start();
    }

    public MainServer(ConnectionCallback callback) {
        this.callback = callback;
    }

    /**
     * Find the Lobby which has this Lobby ID in the list of Lobbies
     * @param lobbyID the Lobby ID to look for
     * @return null if the Lobby was not found or the Lobby Object
     */
    public Lobby getLobbyByID(String lobbyID) {
        for (Lobby lobby : lobbies) {
            if (lobby.getLobbyID().equals(lobbyID))
                return lobby;
        }
        return null;
    }

    /**
     * Create a new Lobby
     * @return Instance of the newly created Lobby
     */
    public Lobby createLobby() {
        Lobby lobby = new Lobby(this);
        lobbies.add(lobby);
        return lobby;
    }

    /**
     * Close a lobby and remove it from the list of all lobbies
     * @param lobby the lobby to close
     */
    public void closeLobby(Lobby lobby) {
        System.out.println("Closing Lobby " + lobby.getLobbyID());
        lobby.closeLobby();
        lobbies.remove(lobby);
    }

    /**
     * Get the Port which is Socket is listening on
     * @return socket.getLocalPort();
     */
    public int getPort() {
        return socket.getLocalPort();
    }

    /**
     * Start main Server Thread, which accepts Client Connections and Creates the PlayerNetworkHandlers for each of the Clients
     */
    public void start() {
        System.out.println("Server Starting on Port " + PORT);
        try {
            socket = new ServerSocket(PORT);
        } catch (IOException e) {
            System.err.println("Could not Create Server ! " + e.getMessage());
            return;
        }
        serverThread = new Thread(() -> {
            try {
                while (true) {
                    Socket client = socket.accept();
                    PlayerNetworkHandler handler = new PlayerNetworkHandler(MainServer.this);
                    ConnectionThread con = ConnectionThread.newClientToServerConnection(client, handler);
                    handler.setConnectionThread(con);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Server Stopped");
        }, "Main Server Thread");
        serverThread.start();
    }
}
