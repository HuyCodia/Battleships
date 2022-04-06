package server.messages;

import application.Coordinate;
/**
 * @author Lennard Boetcher
 * Class which represents a Shoot request on a given Coordinate
 * Used Client -> Server
 */
public class MessageShoot extends Message {

    private Coordinate coord;

    public MessageShoot() {
        super(MessageType.SHOOT);
    }

    public MessageShoot(Coordinate coord) {
        super(MessageType.SHOOT);
        this.coord = coord;
    }

    @Override
    protected boolean parseFromNetworkMessage(String[] message) {
        if (message.length < 2)
            return false;
        int x = Integer.parseInt(message[0]);
        int y = Integer.parseInt(message[1]);
        coord = new Coordinate(x,y);
        return true;
    }

    public Coordinate getCoord() {
        return coord;
    }

    @Override
    protected String getSendMessage() {
        return coord.getX() + SEPARATOR_CHAR + coord.getY();
    }

    @Override
    protected String toPrintableString() {
        return coord.toString();
    }
}
