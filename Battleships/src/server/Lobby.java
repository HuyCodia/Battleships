package server;

import application.Game;
import application.GameField;
import server.messages.*;

import java.io.IOException;
import java.util.Random;

/**
 * @author Lennard Boetcher
 * Main Lobby Class, which handles all Client Network Messages for the Game Logic
 */
public class Lobby {

    private final String lobbyID;
    private final MainServer mainServer;

    private GameField field1, field2;
    private PlayerNetworkHandler player1, player2;
    private PlayerNetworkHandler turn = null;
    private LobbyState state;
    private boolean placedPlayer1 = false, placedPlayer2 = false;

    public Lobby(MainServer mainServer) {
        this.lobbyID = Utils.generateRandomString(16);
        this.mainServer = mainServer;
        this.state = LobbyState.CREATED;
    }

    /**
     * Close this lobby. Will send a MessageLobbyLeave Message to the connected Clients
     */
    public void closeLobby() {
        state = LobbyState.ENDED;
        System.out.println("Closing Lobby " + getLobbyID());
        if (player1 != null && player1.getConnection().isConnected() && !player1.getConnection().isBot()) {
            player1.getConnection().sendMessage(new MessageLobbyLeave(false));
        }
        if (player2 != null && player2.getConnection().isConnected() && !player2.getConnection().isBot()) {
            player2.getConnection().sendMessage(new MessageLobbyLeave(false));
        }
    }

    /**
     * Try to handle a Lobby Join Message
     * @param con Connection of the Player, that wants to join
     * @return true if the joined the lobby. If the Lobby is full or already started, this will be false
     */
    public boolean tryJoinLobby(PlayerNetworkHandler con) {
        System.out.println("Trying to join Lobby: " + con);
        if (state != LobbyState.CREATED)
            return false;
        if (player1 == null || !player1.getConnection().isConnected()) {
            player1 = con;
            field1 = new GameField(null);
            if (player2 != null)
                setState(LobbyState.PREPARING);
            return true;
        } else if (player2 == null || !player2.getConnection().isConnected()) {
            player2 = con;
            field2 = new GameField(null);
            if (player1 != null)
                setState(LobbyState.PREPARING);
            return true;
        }
        return false;
    }

    /**
     * Handle a Client disconnect Event. This will close the lobby, if the Game was already started or no Players are left in the Lobby
     * @param con The NetworkHandler of the Disconnecting Client
     */
    public void onDisconnectEvent(PlayerNetworkHandler con) {
        if (player1 == con) {
            player1 = null;
        }
        if (player2 == con) {
            player2 = null;
        }
        if (player1 == null && player2 == null || getState() == LobbyState.STARTED || getState() == LobbyState.PREPARING || getState() == LobbyState.ENDED)
            mainServer.closeLobby(this);
        else
            setState(LobbyState.CREATED);
    }

    /**
     * Handle all Network Messages sent from a Client. Either Player 1 or Player 2
     * @param con Network Handler of the Client, who sent the Message. This will be used to check which player sent the Message
     * @param message the Network Message received from the Client
     */
    public void onMessageReceived(PlayerNetworkHandler con, Message message) {
        switch (message.getType()) {
            case SHOOT:
                System.out.println("Received Shoot Message. Is Turn: " + isTurn(con));
                if (getState() != LobbyState.STARTED || !isTurn(con))
                    break;
                System.out.println("Processing Shoot Message");
                MessageShoot messageShoot = (MessageShoot) message;
                getEnemyGameField(con).onShootServer(con, getOtherPlayer(con), messageShoot.getCoord());
                if (checkWon())
                    break;
                nextTurn();
                break;
            case CHAT:
                MessageChat chatMessage = (MessageChat) message;
                System.out.println("Got Chat Message: " + chatMessage.getChatMessage());
                if (player1 != null)
                    player1.getConnection().sendMessage(new MessageChat(con.getName() + ": " + chatMessage.getChatMessage()));
                if (player2 != null)
                    player2.getConnection().sendMessage(new MessageChat(con.getName() + ": " + chatMessage.getChatMessage()));
                break;
            case PLACE_SHIPS:
                if (getState() == LobbyState.PREPARING || getState() == LobbyState.CREATED) {
                    MessagePlaceShips ships = (MessagePlaceShips) message;
                    if (con == player1 && !placedPlayer1) {
                        ships.placeOnGameField(field1);
                        placedPlayer1 = true;
                    } else if (!placedPlayer2) {
                        ships.placeOnGameField(field2);
                        placedPlayer2 = true;
                    }
                    System.out.println("Place Ship Received in Lobby. Placed: " + placedPlayer1 + " " + placedPlayer2);
                    if (placedPlayer1 && placedPlayer2)
                        setState(LobbyState.STARTED);
                    broadcastMessage(new MessageChat(con.getName() + " is ready"));
                }
                if (getState() == LobbyState.CREATED) {
                    try {
                        System.out.println("Adding Bot to game " + getLobbyID());
                        ConnectionThread botCon = ConnectionThread.newBotInstance(mainServer.getPort());
                        BotPlayer bot = (BotPlayer) botCon.getCallback();
                        bot.joinLobby(getLobbyID());
                    } catch (IOException e) {
                        System.err.println("Failed to create Bot instance for Lobby " + getLobbyID());
                    }
                    broadcastMessage(new MessageChat(con.getName() + " is ready"));
                }
        }
    }

    /**
     * Check if a Player has sunk all enemy Ships
     * @return true if all ships have been sunk and the game if over. False if the game is not over yet
     */
    private boolean checkWon() {
        if (field2.allShipsSunk()) {
            player1.getConnection().sendMessage(new MessageEndGame(true, player1.getName()));
            player2.getConnection().sendMessage(new MessageEndGame(false, player1.getName()));
            player1.getConnection().sendMessage(field2.getMessagePlaceShips());
            player2.getConnection().sendMessage(field1.getMessagePlaceShips());
            setState(LobbyState.ENDED);
            broadcastMessage(new MessageChat(player1.getName() + " has won the Game"));
            return true;
        } else if (field1.allShipsSunk()) {
            player1.getConnection().sendMessage(new MessageEndGame(false, player2.getName()));
            player2.getConnection().sendMessage(new MessageEndGame(true, player2.getName()));
            player1.getConnection().sendMessage(field2.getMessagePlaceShips());
            player2.getConnection().sendMessage(field1.getMessagePlaceShips());
            setState(LobbyState.ENDED);
            broadcastMessage(new MessageChat(player2.getName() + " has won the Game"));
            return true;
        }
        return false;
    }

    /**
     * Send a network Message to both clients
     * @param message that should be send
     */
    private void broadcastMessage(Message message) {
        if (player1 != null && player1.getConnection() != null)
            player1.getConnection().sendMessage(message);
        if (player2 != null && player2.getConnection() != null)
            player2.getConnection().sendMessage(message);
    }

    /**
     * Change the State of the Lobby. Will also send a Network Message to the clients to notify them of the State Change.
     * @param state new State of the Lobby
     */
    private void setState(LobbyState state) {
        this.state = state;
        System.out.println("New State of Lobby " + getLobbyID() + " " + getState());
        MessageLobbyState mess = new MessageLobbyState(state);
        if (state == LobbyState.STARTED) {
            mess.setExtra(player1.getName());
            player2.getConnection().sendMessage(mess);
            mess.setExtra(player2.getName());
            player1.getConnection().sendMessage(mess);
        } else {
            broadcastMessage(mess);
        }
        if (state == LobbyState.STARTED) {
            turn = (new Random().nextBoolean()) ? player1 : player2;
            turn.getConnection().sendMessage(new MessageTurn());
            broadcastMessage(new MessageChat(turn.getName() + " makes the first turn"));
        }
    }

    /**
     * Get the Network Handler of the other Player
     * @param in Network Handler of the Player, which we want to have the opponent of
     * @return the opponent of the in Player
     */
    private PlayerNetworkHandler getOtherPlayer(PlayerNetworkHandler in) {
        if (in == player1)
            return player2;
        return player1;
    }

    /**
     * Get the Gamefield of  the specified Players Enemy
     * @param handler Network Handler to identify the Player
     * @return Gamefield of the Enemy
     */
    private GameField getEnemyGameField(PlayerNetworkHandler handler) {
        if (player1 == handler)
            return field2;
        return field1;
    }

    /**
     * Check if the Player is currently going on
     * @param handler Player Network Instance
     * @return true if the player is going on. False if not
     */
    private boolean isTurn(PlayerNetworkHandler handler) {
        if (turn == null)
            return true;
        return turn == handler;
    }

    /**
     * Pass the current turn to the next player. Will also inform the Player with a network Message, that it's not his turn
     */
    private void nextTurn() {
        if (isTurn(player1))
            turn = player2;
        else
            turn = player1;
        turn.getConnection().sendMessage(new MessageTurn());
        broadcastMessage(new MessageChat(turn.getName() + " makes the next turn"));
    }

    public LobbyState getState() {
        return state;
    }

    public String getLobbyID() {
        return lobbyID;
    }

}
