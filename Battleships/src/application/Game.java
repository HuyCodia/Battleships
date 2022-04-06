package application;

import application.ships.ShipPart;
import server.ConnectionCallback;
import server.ConnectionThread;
import server.LobbyState;
import server.messages.*;

import java.io.IOException;

public class Game implements ConnectionCallback {

    /** initialise a gamefield for player */
    private GameField gameField;

    /** initialise a gamefield for opponent */
    private GameField enemyGameField;

    /** initialise a LobbySate for actual state */
    private LobbyState lobbyState;

    /** initialise a ConnectionThread for server */
    protected ConnectionThread toServerConnection;

    /** initialise a MainWindowController */
    private MainWindowController mainWindowController;

    /** constructor of game */
    public Game() {
        setGameField(new GameField(this));
        setEnemyGameField(new GameField(this));
    }

    /**
     * This method shows connection to server
     *
     * @param host
     * @param port
     * @throws IOException
     */
    public void connectToServer(String host, int port) throws IOException {
        toServerConnection = ConnectionThread.connectToServer(host, port, this);
    }

    /**
     * This method sets mainWindowController
     *
     * @param mainWindowController
     */
    public void setMainWindowController(MainWindowController mainWindowController) {
        this.mainWindowController = mainWindowController;
    }

    /**
     * This method returns the actual gameField
     *
     * @return gameField
     */
    public GameField getGameField() {
        return gameField;
    }

    /**
     * This method returns the actual gameField from the opponent
     *
     * @return enemyGameField
     */
    public GameField getEnemyGameField() {
        return enemyGameField;
    }

    /**
     * This method set a gameField
     *
     * @param gameField
     */
    public void setGameField(GameField gameField) {
        this.gameField = gameField;
        this.gameField.setEnemy(false);
    }

    /**
     * This method set a gameField for the opponent
     *
     * @param enemyGameField
     */
    public void setEnemyGameField(GameField enemyGameField) {
        this.enemyGameField = enemyGameField;
        this.enemyGameField.setEnemy(true);
    }

    /**
     * This method establish a connection to the server
     *
     * @return toServerConnection
     */
    public ConnectionThread getConnection() {
        return toServerConnection;
    }

    /**
     * This method reacts to mainWindowController if a connection is available
     *
     * @param con To Server Connection
     */
    @Override
    public void onDisconnectEvent(ConnectionThread con) {
        if(mainWindowController != null){
            mainWindowController.setViewToPregame();
        }
    }

    /**
     * Handle incoming Network Messages and react to them
     *
     * @param con To Server Connection
     * @param message Network Message received
     */
    @Override
    public void onMessageReceived(ConnectionThread con, Message message) {
        switch (message.getType()) {
            case ERROR:
                MessageError messageError = (MessageError) message;
                System.out.println("Error Message: " + messageError.toString());
                break;
            case HIT:
                MessageHit hitMessage = (MessageHit) message;
                System.out.println("Hit Message Received " + hitMessage.getCoord() + " " + hitMessage.isEnemy() + " " + hitMessage.isHitShip() + " " + hitMessage.isSunk());
                GameField field = gameField;
                if (hitMessage.isEnemy())
                    field = enemyGameField;
                field.hit(hitMessage.getCoord(), hitMessage.isHitShip());
                if (hitMessage.isSunk())
                    field.sunk(hitMessage.getCoord());
                if (mainWindowController != null)
                    mainWindowController.repaintGamefieldsSafe();
                break;
            case TURN:
                if (mainWindowController != null)
                    mainWindowController.repaintGamefieldsSafe();
                break;
            case LOBBY_STATE:
                MessageLobbyState stateMessage = (MessageLobbyState) message;
                lobbyState = stateMessage.getState();
                String extra = stateMessage.getExtra();
                if (lobbyState == LobbyState.STARTED && mainWindowController != null) {
                    mainWindowController.handleenemyname(extra);
                }
                break;
            case LOBBY_JOIN:
                MessageLobbyJoin joinMessage = (MessageLobbyJoin) message;
                System.out.println("Joined lobby: " + joinMessage.getLobbyID());
                if(mainWindowController != null){
                    mainWindowController.setLobbyCode(joinMessage.getLobbyID());
                    mainWindowController.setViewToIngame();
                }
                break;
            case CHAT:
                if (mainWindowController != null) {
                    MessageChat chatMessage = (MessageChat) message;
                    System.out.println("Chat Message Received: " + chatMessage.getChatMessage());
                    mainWindowController.addChatMessage(chatMessage.getChatMessage());
                }
                break;
            case LOBBY_LEAVE:
                if(mainWindowController != null){
                    mainWindowController.setViewToPregame();
                }
                break;
            case END:
                if (mainWindowController != null) {
                    MessageEndGame endMessage = (MessageEndGame) message;
                    System.out.println("Game Ended. Won ? " + endMessage.isWon() + " Winner Name: " + endMessage.getWinnerName());
                    mainWindowController.setWinnerImage(endMessage.isWon());
                }
                break;
            case PLACE_SHIPS:
                MessagePlaceShips messagePlaceShips = (MessagePlaceShips) message;
                messagePlaceShips.placeRestOnGameField(getEnemyGameField());
                if(mainWindowController != null){
                    mainWindowController.repaintGamefieldsSafe();
                }
                break;
        }
    }

    /**
     * This method checks if all the ships have been placed
     *
     * @return boolean if all ships are set
     */
    public boolean allShipsPlaced() {
        for (ShipPart remain : getGameField().getRemainingShipsToPlace()) {
            if (remain != null)
                return false;
        }
        return true;
    }

    /**
     * This method returns the state whether the lobby is ready.
     *
     * @return lobbyState
     */
    public boolean isReady() {
        return lobbyState == LobbyState.STARTED;
    }
}
