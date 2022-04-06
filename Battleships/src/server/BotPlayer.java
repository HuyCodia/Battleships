package server;

import application.Coordinate;
import application.Game;
import application.GameField;
import application.ShipDirection;
import application.ships.*;
import server.messages.*;

import java.security.SecureRandom;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * @author Lennard Boetcher
 * Main Bot Class. Uses Game as parent to handle the Gamefields
 */
public class BotPlayer extends Game {

    private static final String[] BOT_NAMES = {"Harald", "Peter", "Ernst", "Horst","Jürgen", "Ingelore", "Greta", "Petra"};

    public static String[] getBotNames() {
        return BOT_NAMES;
    }

    private String name;
    /**
     * Keep Track of the Last Hit Positions to keep track of hit ships, that the bot tries to sink
     */
    private List<Coordinate> lastHits = new ArrayList<>();

    public BotPlayer() {
        super();
        this.name = BOT_NAMES[new Random().nextInt(BOT_NAMES.length)];
    }

    /**
     * Handle incoming Network Messages and react to them
     * @param con To Server Connection
     * @param message Network Message received
     */
    @Override
    public void onMessageReceived(ConnectionThread con, Message message) {
        super.onMessageReceived(con, message);
        switch (message.getType()) {
            case HIT:
                MessageHit hitMessage = (MessageHit) message;
                if (!hitMessage.isEnemy() || !hitMessage.isHitShip())
                    break;
                // If the Position hit was a ship, we add it to the lastHits list.
                lastHits.add(hitMessage.getCoord());
                if (hitMessage.isSunk()) // If the ship was sunk, we no longer want to shoot at the ships position
                    lastHits.clear();
                break;
            case TURN:
                shootBack();
                break;
            case ERROR:
                System.out.println("Bot got an Error: " + ((MessageError)message).toString());
                break;
            case LOBBY_STATE:
                MessageLobbyState stateMessage = (MessageLobbyState) message;
                if (stateMessage.getState() == LobbyState.PREPARING)
                    placeShips();
                break;
        }
    }

    /**
     * Place all ships on the Gamefield and send them to the Server.
     * The Bot tries to Place 2 - 4 Ships at the Border and the Rest will be placed in the middle of the Gamefield
     * The Bot tries to avoid placement of ships, which connect at the edges, which reduces the chance of the Enemy to find ships.
     * Since the Placement is random, it can take a few attempts to actually place all ships
     */
    public void placeShips() {
        List<ShipPart> shipPartList;
        int placed = 0;
        do {
            getGameField().clearShips();
            Random r = new Random();
            shipPartList = ShipCollection.getAvailableShips();
            int borderShips = r.nextInt(shipPartList.size() / 5) + 2;
            placed = 0;
            for (int i = 0; i < borderShips; i++) {
                int randomShipIndex = r.nextInt(shipPartList.size());
                ShipPart ship = shipPartList.get(randomShipIndex);
                if (placeShipBorder(ship, r)) {
                    placed++;
                    shipPartList.remove(randomShipIndex);
                }
            }
            for (int i = 0; i < shipPartList.size(); i++) {
                ShipPart part = shipPartList.get(i);
                if (placeShip(part, r))
                    placed++;
            }
        } while (placed < ShipCollection.getAvailableShips().size());

        System.out.println("Bot placed Ships");
        getGameField().sendShipsToServer();
    }

    /**
     * Tries to place a ship on the Gamefield, which is at the border of the Gamefield
     * @param ship The type of the Ship, that should be placed
     * @param r The Random instance, that should be used (will generate the same placement if a static seed is used, which makes testing easier)
     * @return if the placement was successful. It will try to place ships 100 times before cancelling
     */
    private boolean placeShipBorder(ShipPart ship, Random r) {
        Coordinate c;
        ShipDirection dir;
        int tries = 0;
        do {
            boolean horizontal = r.nextBoolean();
            c = new Coordinate(!horizontal ? (r.nextBoolean() ? 0 : 9) : r.nextInt(10), horizontal ? (r.nextBoolean() ? 0 : 9) : r.nextInt(10));
            dir = horizontal ? ShipDirection.EAST : ShipDirection.SOUTH;
            tries++;
        } while (!getGameField().addShip(c, ship, dir) && tries <= 100);
       // System.out.println("Placed Ship at Border: " + c + " Dir: " + dir + " " + tries);
        return tries <= 100;
    }

    /**
     * Tries to place a ship on the Gamefield, which is at in the middle of the Gamefield (2-9 and B-I)
     * @param ship The type of the Ship, that should be placed
     * @param r The Random instance, that should be used (will generate the same placement if a static seed is used, which makes testing easier)
     * @return if the placement was successful. It will try to place ships 100 times before cancelling
     */
    private boolean placeShip(ShipPart ship, Random r) {
        Coordinate c;
        ShipDirection dir;
        int tries = 0;
        boolean corners;
        do {
            dir = r.nextBoolean() ? ShipDirection.EAST : ShipDirection.SOUTH;
            c = new Coordinate(r.nextInt(8)+1, r.nextInt(8)+1);
           // System.out.println("Trying to place at " + c + " Dir: " + dir);
            Coordinate cornerTopLeft, cornerTopRight, cornerBottomLeft, cornerBottomRight;
            cornerTopLeft = new Coordinate(c.getX() - 1, c.getY() - 1);
            if (dir == ShipDirection.SOUTH) {
                cornerTopRight = new Coordinate(c.getX() + 1, c.getY() - 1);
                cornerBottomLeft = new Coordinate(c.getX() - 1, c.getY() + ship.getShipLength());
                cornerBottomRight = new Coordinate(c.getX() + 1, c.getY() + ship.getShipLength());
            } else {
                cornerTopRight = new Coordinate(c.getX() + ship.getShipLength(), c.getY() - 1);
                cornerBottomLeft = new Coordinate(c.getX() - 1, c.getY() + 1);
                cornerBottomRight = new Coordinate(c.getX() + ship.getShipLength(), c.getY() + 1);
            }
            tries++;
           // System.out.println("Corners: " + cornerTopLeft + " " + cornerTopRight + " " + cornerBottomRight + " " + cornerBottomLeft);
            corners = true;
            if (getGameField().getEntityAt(cornerTopLeft) == null || getGameField().getEntityAt(cornerTopLeft) instanceof ShipPart ||
                    getGameField().getEntityAt(cornerTopRight) == null || getGameField().getEntityAt(cornerTopRight) instanceof ShipPart ||
                    getGameField().getEntityAt(cornerBottomLeft) == null || getGameField().getEntityAt(cornerBottomLeft) instanceof ShipPart ||
                    getGameField().getEntityAt(cornerBottomRight) == null || getGameField().getEntityAt(cornerBottomRight) instanceof ShipPart) {
                //System.out.println("Repeated because of Corners");
                corners = false;
            }
        } while ((!getGameField().addShip(c, ship, dir) || !corners) && tries <= 100);
       // System.out.println(getGameField().getGameField());
        //System.out.println("Placed Ship: " + c + " Dir: " + dir + " tries: " + tries);
        return tries <= 100;
    }

    /**
     * Shoot on the Enemy Gamefield. It will try to target already found ships first
     * If no ship was found, it will shoot random in a checkerboard pattern
     * The Method does not take into account, that some positions are already invalid ship positions for the remaining ships length
     */
    private void shootBack() {
        Random r = new SecureRandom();
        System.out.println("Last Hits: " + lastHits.size());
        if (lastHits.size() == 1) { // If a ship was found, but the Direction is not clear
            Coordinate last = lastHits.get(0);
            List<Coordinate> possiblePositions = new ArrayList<>();
            addIfPossible(1,0, possiblePositions, last);
            addIfPossible(0,1, possiblePositions, last);
            addIfPossible(-1,0, possiblePositions, last);
            addIfPossible(0,-1, possiblePositions, last);
            if (possiblePositions.size() >= 1) {
                Coordinate randomPos = possiblePositions.get(r.nextInt(possiblePositions.size()));
                getConnection().sendMessage(new MessageShoot(randomPos));
                return;
            }
        }
        if (lastHits.size() > 1) { // The same ship was hit twice, which gives the Direction of the Ship
            ShipDirection dir = lastHits.get(0).getX() == lastHits.get(1).getX() ? ShipDirection.SOUTH : ShipDirection.EAST;
            System.out.println("Ship Dir: " + dir);
            Coordinate start = null, end = null;
            for (Coordinate coord : lastHits) {
                if (start == null || start.getX() - coord.getX() < 0 || start.getY() - coord.getY() < 0)
                    start = coord;
                if (end == null || end.getX() - coord.getX() > 0 || end.getY() - coord.getY() > 0)
                    end = coord;
            }
            Coordinate shoot = new Coordinate(start.getX() + (dir == ShipDirection.EAST ? 1 : 0), start.getY() + (dir == ShipDirection.EAST ? 0 : 1) );
            if (getEnemyGameField().getEntityAt(shoot) == null || getEnemyGameField().getEntityAt(shoot).isHit())
                shoot = new Coordinate(end.getX() + (dir == ShipDirection.EAST ? -1 : 0), end.getY() + (dir == ShipDirection.EAST ? 0 : -1) );
            System.out.println("Start: " + start + " End: " + end + " Shoot: " + shoot);
            getConnection().sendMessage(new MessageShoot(shoot));
        } else {
            // Fill an Array with all possible positions in a checkerboard pattern
            List<Coordinate> possiblePositions = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                for (int i2 = 0; i2 < 10; i2++) {
                    if ((i + i2) % 2 == 1)
                        continue;
                    Coordinate c = new Coordinate(i , i2);
                    if (!getEnemyGameField().getEntityAt(c).isHit())
                        possiblePositions.add(c);
                }
            }
            if (possiblePositions.size() > 0) {
                Coordinate coord = null;
                while (possiblePositions.size() > 0) {
                    int index = r.nextInt(possiblePositions.size()); // Choose a random position and check for ships.
                    coord = possiblePositions.get(index);
                    // Check for Ship, that have already been hit next to this position
                    Entity ent = getEnemyGameField().getEntityAt(new Coordinate(coord.getX() + 1, coord.getY()));
                    if (ent != null && ent.isHit()) {
                        possiblePositions.remove(index);
                        continue;
                    }
                    ent = getEnemyGameField().getEntityAt(new Coordinate(coord.getX() - 1, coord.getY()));
                    if (ent != null && ent.isHit()) {
                        possiblePositions.remove(index);
                        continue;
                    }
                    ent = getEnemyGameField().getEntityAt(new Coordinate(coord.getX(), coord.getY()+1));
                    if (ent != null && ent.isHit()) {
                        possiblePositions.remove(index);
                        continue;
                    }
                    ent = getEnemyGameField().getEntityAt(new Coordinate(coord.getX(), coord.getY()-1));
                    if (ent != null && ent.isHit()) {
                        possiblePositions.remove(index);
                        continue;
                    }
                    break;
                }
                getConnection().sendMessage(new MessageShoot(coord));
            } else {
                System.out.println("No possible Positions to shoot found ! Shooting on random Tile");
                for (int i = 0; i < 100; i++) {
                    Coordinate coord = new Coordinate(i % 10, i / 10);
                    if (getEnemyGameField().getEntityAt(coord).isHit())
                        continue;
                    getConnection().sendMessage(new MessageShoot(coord));
                    break;
                }
            }
        }
    }

    /**
     * small helper function to check if the position can be shot at
     * @param x offset of the last Position
     * @param y offset of the last Position
     * @param list which the new Position would be added if possible
     * @param last Position of the Last Ship hit
     */
    private void addIfPossible(int x, int y, List<Coordinate> list, Coordinate last) {
        Coordinate hit = new Coordinate(last.getX() + x, last.getY() + y);
        Entity hitEnt = getEnemyGameField().getEntityAt(hit);
        if (hitEnt != null && !hitEnt.isHit())
            list.add(hit);
    }

    /**
     * Try to join the lobby with the given Lobby ID
     * @param lobbyID Lobby ID of the target Lobby
     */
    public void joinLobby(String lobbyID) {
        getConnection().sendMessage(new MessageLobbyJoin(lobbyID, getName()));
    }

    public String getName() {
        return name;
    }

    protected void setConnectionThread(ConnectionThread con) {
        super.toServerConnection = con;
    }
}
