package application;
import application.ships.*;
import server.PlayerNetworkHandler;
import server.messages.MessageHit;
import server.messages.MessagePlaceShips;
import server.messages.MessageShoot;

import java.util.List;

public class GameField {

    /** initialise number of row in gameField */
    private static final int _row = 10;

    /** initialise number of col in gameField */
    private static final int _col = 10;

    /** creates Entity Array for given objects */
    private final Entity[][] gameField = new Entity[_row][_col];

    /** declare new Game */
    private Game parent;

    /** declare enemy to know how things stand */
    private boolean enemy;

    /** create new MessagePlaceShips */
    private MessagePlaceShips messagePlaceShips = new MessagePlaceShips();

    /** create list for remaining ships  to place */
    private List<ShipPart> remainingShipsToPlace = ShipCollection.getAvailableShips();

    /**
     * Constructor of GameField
     *
     * @param parent
     */
    public GameField(Game parent) {
        this.parent = parent;
        clearShips();
    }

    /** This method clears all ships to reposition them. */
    public void clearShips() {
        for (int i = 0; i < _row; i++) {
            for (int i2 = 0; i2 < _col; i2++) {
                gameField[i][i2] = new Water();
            }
        }
        remainingShipsToPlace = ShipCollection.getAvailableShips();
        messagePlaceShips = new MessagePlaceShips();
    }

    /** This method sends the ships to thte server */
    public void sendShipsToServer() {
        if (parent == null || parent.getConnection() == null)
            return;
        System.out.println("Bot sending Ships to Server. Parent: " + parent + " Bot Connection: " + (parent != null ? parent.getConnection() : "null"));
        parent.getConnection().sendMessage(messagePlaceShips);
    }

    /**
     * This methode returns all the remaining ships for placing.
     *
     * @return remainingShipsToPlace
     */
    public List<ShipPart> getRemainingShipsToPlace() {
        return remainingShipsToPlace;
    }

    /**
     * This method checks if all ships are sunk.
     *
     * @return boolean
     */
    public boolean allShipsSunk() {
        for (int i = 0; i < gameField.length; i++) {
            for (int i2 = 0; i2 < gameField[i].length; i2++) {
                if (gameField[i][i2] != null && gameField[i][i2] instanceof ShipPart) {
                    ShipPart part = (ShipPart) gameField[i][i2];
                    if (part.getParent() != null && !part.getParent().isSunk())
                        return false;
                }
            }
        }
        return true;
    }

    public MessagePlaceShips getMessagePlaceShips() {
        return messagePlaceShips;
    }

    /**
     * This method checks if you can place a ship to the gameField.
     * There are options to place a ship like horizontal or vertical.
     * If you choose horizontal, this method will get the westernmost coordinate and the ship's length will construct from west to east.
     * If you choose vertical, this method will get the northernmost coordinate and the ship's length will construct from north to south.
     *
     * @param c westernmost or northernmost coordinate of ship. It depends of the ShipDirection
     * @param s type of ship
     * @param d direction for placing
     * @return boolean
     */
    //Schiff setzen: nördliche (vertikales Schiff) oder westliche (horizontales Schiff) Koordinate übergeben bekommen und Schifflänge nach Osten oder Süden anhängen
    public boolean addShip(Coordinate c, ShipPart s, ShipDirection d) {

        //Prüfen, ob Schiff gesetzt werden kann, indem jede Koordinate des zu setzenden Schiffes geprüft wird,
        // ob dort schon ein Schiff liegt
        for (int i = 0; i < s.getShipLength(); i++) {
            if (d.equals(ShipDirection.EAST)) {
                if (!isInBounds(new Coordinate(c.getX() + i, c.getY()))
                        || !isValidPosition(new Coordinate(c.getX() + i, c.getY()))) {
                    return false;
                }
            } else if (d.equals(ShipDirection.SOUTH)) {
                if (!isInBounds(new Coordinate(c.getX(), c.getY() + i))
                        || !isValidPosition(new Coordinate(c.getX(), c.getY() + i))) {
                    return false;
                }
            }
        }

        Ship ship = new Ship(s.getShipLength());
        for (int j = 0; j < s.getShipLength(); j++) {
            if (d.equals(ShipDirection.EAST)) {
                gameField[c.getX()+j][c.getY()] = s.copy().setParent(ship);
            } else if (d.equals(ShipDirection.SOUTH)) {
                gameField[c.getX()][c.getY()+j] = s.copy().setParent(ship);
            }
        }
        messagePlaceShips.addShip(c,s,d);
        return true;
    }

    /**
     * This method ignore the added ships
     *
     * @param c  given coordinate
     * @param s  given part of ship
     * @param d  given direction of ship
     * @return boolean
     */
    //Schiff setzen: nördliche (vertikales Schiff) oder westliche (horizontales Schiff) Koordinate übergeben bekommen und Schifflänge nach Osten oder Süden anhängen
    public boolean addShipIgnore(Coordinate c, ShipPart s, ShipDirection d) {

        Ship ship = new Ship(s.getShipLength());
        for (int j = 0; j < s.getShipLength(); j++) {
            if (d.equals(ShipDirection.EAST)) {
                if (gameField[c.getX()+j][c.getY()] instanceof ShipPart)
                    ((ShipPart) gameField[c.getX()+j][c.getY()]).setParent(ship);
                else
                    gameField[c.getX() + j][c.getY()] = s.copy().setParent(ship);
            } else if (d.equals(ShipDirection.SOUTH)) {
                if (gameField[c.getX()][c.getY()+j] instanceof ShipPart)
                    ((ShipPart) gameField[c.getX()][c.getY()+j]).setParent(ship);
                else
                    gameField[c.getX()][c.getY()+j] = s.copy().setParent(ship);
            }
        }
        messagePlaceShips.addShip(c,s,d);
        return true;
    }

    /**
     * This method checks whether a ship can be added
     *
     * @param c  given coordinate
     * @param s  given part of ship
     * @param d  given direction of ship
     * @return boolean
     */
    public boolean canAddShip(Coordinate c, ShipPart s, ShipDirection d) {

        //Prüfen, ob Schiff gesetzt werden kann, indem jede Koordinate des zu setzenden Schiffes geprüft wird,
        // ob dort schon ein Schiff liegt
        for (int i = 0; i < s.getShipLength(); i++) {
            if (d.equals(ShipDirection.EAST)) {
                if (!isInBounds(new Coordinate(c.getX() + i, c.getY()))
                        || !isValidPosition(new Coordinate(c.getX() + i, c.getY()))) {
                    return false;
                }
            } else if (d.equals(ShipDirection.SOUTH)) {
                if (!isInBounds(new Coordinate(c.getX(), c.getY() + i))
                        || !isValidPosition(new Coordinate(c.getX(), c.getY() + i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method checks the stand of enemy
     *
     * @return enemy
     */
    public boolean isEnemy() {
        return enemy;
    }

    /** This method  sets the stand of enemy */
    protected void setEnemy(boolean enemy) {
        this.enemy = enemy;
    }

    /**
     * This method checks the valid position of ships
     *
     * @param c given coordinate
     * @return boolean
     */
    //Schiff nicht auf anderem Schiff, bzw. nicht direkt neben anderem schiff
    public boolean isValidPosition(Coordinate c) {
        boolean b = true;
        if ((this.gameField[c.getX()][c.getY()] instanceof ShipPart)
         || (c.getX() < 9 && this.gameField[c.getX()+1][c.getY()] instanceof ShipPart)
         || (c.getY() < 9 && this.gameField[c.getX()][c.getY()+1] instanceof ShipPart)
         || (c.getX() > 0 && this.gameField[c.getX()-1][c.getY()] instanceof ShipPart)
         || (c.getY() > 0 && this.gameField[c.getX()][c.getY()-1] instanceof ShipPart)) {
            b = false;
        }
        return b;
    }

    /**
     * This method checks is the given coordinate is in bounds of the gameField
     *
     * @param c given coordinate
     * @return boolean
     */
    public boolean isInBounds(Coordinate c) {
        return (c.getX() < 10 && c.getY() < 10 && c.getX() >= 0 && c.getY() >= 0);
    }

    /**
     * This method returns the necessary coordinates to Entity
     *
     * @param c given coordinate
     * @return gameField[c.getX()][c.getY()]
     */
    public Entity getEntityAt(Coordinate c) {
        if (!isInBounds(c))
            return null;
        return gameField[c.getX()][c.getY()];
    }

    protected void setEntity(Coordinate c, Entity entity) {
        if (!isInBounds(c))
            return;
        gameField[c.getX()][c.getY()] = entity;
    }

    /**
     * This method send the given coordinate to the server.
     *
     * @param c given coordinate
     */
    //koordinaten bekommen und an server schicken
    public void shoot(Coordinate c) {
        if (parent == null)
            return;
        parent.getConnection().sendMessage(new MessageShoot(c));
    }

    /**
     * This method
     *
     * @param shooter
     * @param other
     * @param c         given coordinate
     */
    public void onShootServer(PlayerNetworkHandler shooter, PlayerNetworkHandler other, Coordinate c) {
        Entity entity = getEntityAt(c);
        boolean ship = entity instanceof ShipPart;
        boolean sunk = false;
        entity.setHit();
        if (ship)
            sunk = ((ShipPart) entity).getParent().isSunk();
        System.out.println("Sending Message Hit to Clients");
        shooter.getConnection().sendMessage(new MessageHit(c, ship, true, sunk));
        other.getConnection().sendMessage(new MessageHit(c, ship, false, sunk));
    }

    /**
     * This method checks if a ship is hit
     *
     * @param c         given coordinate
     * @param hitShip   state of hitting
     */
    public void hit(Coordinate c, boolean hitShip) {
        Entity entity = getEntityAt(c);
        if (entity == null)
            return;
        if (hitShip && isEnemy()) {
            entity = new EnemyShip();
            setEntity(c, entity);
        }
        entity.setHit();
    }

    /**
     * This method checks if a ship is sunk.
     *
     * @param c given coordinate
     */
    public void sunk(Coordinate c) {
        // Find start of the Ship which is on the given Coordinate
        int xStartOffset = 0,yStartOffset = 0, xEndOffset = 0, yEndOffset = 0;
        while (getEntityAt(new Coordinate(c.getX() + xStartOffset + 1, c.getY() + yStartOffset)) instanceof ShipPart) {
            xStartOffset++;
        }
        while (getEntityAt(new Coordinate(c.getX() + xStartOffset, c.getY() + yStartOffset + 1)) instanceof ShipPart) {
            yStartOffset++;
        }
        while (getEntityAt(new Coordinate(c.getX() + xEndOffset - 1, c.getY() + yEndOffset)) instanceof ShipPart) {
            xEndOffset--;
        }
        while (getEntityAt(new Coordinate(c.getX() + xEndOffset, c.getY() + yEndOffset - 1)) instanceof ShipPart) {
            yEndOffset--;
        }

        /** Check if the ship is vertical, by comparing if the X value for start and end is the same */
        boolean vertical = xStartOffset == xEndOffset;
        int shipPartCount = 1 + (vertical ? yStartOffset - yEndOffset : xStartOffset - xEndOffset);
        ShipPart[] parts = new ShipPart[shipPartCount];
        Coordinate currentCoord = new Coordinate(c.getX() + xStartOffset, c.getY() + yStartOffset);
        Ship parent = new Ship(shipPartCount);
        System.out.println(shipPartCount);
        for (int i = 0; i < shipPartCount; i++) {
            System.out.println(currentCoord);
            parts[i] = (ShipPart) getEntityAt(currentCoord);
            parts[i].setParent(parent);
            currentCoord.setCoordinate(vertical ? currentCoord.getX() : currentCoord.getX() - 1, vertical ? currentCoord.getY() - 1 : currentCoord.getY());
        }
    }

}
