package application.ships;

public abstract class ShipPart extends Entity {

    private final int _shipLength;
    private final String _shipName;
    private Ship parent;

    public static ShipPart partFromLength(int length) {
        if (length == 2)
            return new Submarine();
        if (length == 3)
            return new Destroyer();
        if (length == 4)
            return new Cruiser();
        if (length == 5)
            return new Battleship();
        return null;
    }

    public ShipPart(int shipLength, String shipName) {
        this._shipLength = shipLength;
        this._shipName = shipName;
    }

    public int getShipLength() {
        return _shipLength;
    }

    public ShipPart setParent(Ship parent) {
        this.parent = parent;
        parent.addPart(this);
        return this;
    }

    public Ship getParent() {
        return parent;
    }

    public abstract ShipPart copy();

}
