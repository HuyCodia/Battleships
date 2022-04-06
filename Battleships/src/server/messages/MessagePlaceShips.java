package server.messages;

import application.Coordinate;
import application.Game;
import application.GameField;
import application.ShipDirection;
import application.ships.ShipPart;

import java.util.ArrayList;
import java.util.List;
/**
 * @author Lennard Boetcher
 * Class which represents the placement of the Ships on the Gamefield.
 * Message will also tell the Server, that the client is ready to play.
 * If the Client received the message, it will be interpreted as the Enemy Gamefield to show all remaining ships at the end of the Game.
 * Used Server -> Client and Client -> Server
 */
public class MessagePlaceShips extends Message {

    List<PlaceShipEntry> entries = new ArrayList<>();

    public MessagePlaceShips() {
        super(MessageType.PLACE_SHIPS);
    }

    public void addShip(Coordinate c, ShipPart s, ShipDirection d) {
        if (entries.size() < 10)
            entries.add(new PlaceShipEntry(c,s,d));
    }

    public void placeOnGameField(GameField field) {
        for (PlaceShipEntry entry : entries)
            entry.placeOnGameField(field);
    }

    public void placeRestOnGameField(GameField field) {
        for (PlaceShipEntry entry : entries)
            entry.placeRestOnGameField(field);
    }

    @Override
    protected boolean parseFromNetworkMessage(String[] message) {
        if (message.length < 1)
            return false;
        int count = Integer.parseInt(message[0]);
        if (message.length < count * 4 + 1)
            return false;
        for (int i = 0; i < count; i++) {
            entries.add(new PlaceShipEntry(message, 1 + i * 4));
        }
        return true;
    }

    @Override
    protected String getSendMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(entries.size());
        sb.append(SEPARATOR_CHAR);
        for (PlaceShipEntry entry : entries) {
            sb.append(entry.toString());
        }
        return sb.toString();
    }

    @Override
    protected String toPrintableString() {
        return getSendMessage();
    }
}

class PlaceShipEntry {

    private Coordinate c;
    private ShipPart s;
    private ShipDirection d;

    public PlaceShipEntry(Coordinate c, ShipPart s, ShipDirection d) {
        this.c = c;
        this.s = s;
        this.d = d;
    }

    public PlaceShipEntry(String[] fromString, int startIndex) {
        int x = Integer.parseInt(fromString[0 + startIndex]);
        int y = Integer.parseInt(fromString[1 + startIndex]);
        int l = Integer.parseInt(fromString[2 + startIndex]);
        String dir = fromString[3 + startIndex];
        c = new Coordinate(x,y);
        s = ShipPart.partFromLength(l);
        d = ShipDirection.valueOf(dir);
    }

    protected void placeOnGameField(GameField field) {
        field.addShip(c,s,d);
    }
    protected void placeRestOnGameField(GameField field) {
        field.addShipIgnore(c,s,d);
    }

    @Override
    public String toString() {
        return c.getX() + Message.SEPARATOR_CHAR + c.getY() + Message.SEPARATOR_CHAR + s.getShipLength() + Message.SEPARATOR_CHAR + d.toString() + Message.SEPARATOR_CHAR;
    }
}
