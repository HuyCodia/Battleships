package server.messages;

import application.Coordinate;
/**
 * @author Lennard Boetcher
 * Class which represents a Hit response to a Shoot Message
 * Used Server -> Client
 */
public class MessageHit extends Message {

    private Coordinate coord;
    private boolean hitShip;
    private boolean enemy;
    private boolean sunk;

    public MessageHit() {
        super(MessageType.HIT);
    }

    public MessageHit(Coordinate coord, boolean hitShip, boolean enemy, boolean sunk) {
        super(MessageType.HIT);
        this.coord = coord;
        this.hitShip = hitShip;
        this.enemy = enemy;
        this.sunk = sunk;
    }

    @Override
    protected boolean parseFromNetworkMessage(String[] message) {
        if (message.length < 5)
            return false;
        int x = Integer.parseInt(message[0]);
        int y = Integer.parseInt(message[1]);
        coord = new Coordinate(x,y);
        hitShip = message[2].equals("1");
        enemy = message[3].equals("1");
        sunk = message[4].equals("1");
        return true;
    }

    public Coordinate getCoord() {
        return coord;
    }

    public boolean isEnemy() {
        return enemy;
    }

    public boolean isHitShip() {
        return hitShip;
    }

    public boolean isSunk() {
        return sunk;
    }

    @Override
    protected String getSendMessage() {
        return coord.getX() + SEPARATOR_CHAR + coord.getY() + SEPARATOR_CHAR + (hitShip ? "1" : "0") + SEPARATOR_CHAR + (enemy ? "1" : "0") + SEPARATOR_CHAR + (sunk ? "1" : "0");
    }

    @Override
    protected String toPrintableString() {
        return coord.toString();
    }
}
