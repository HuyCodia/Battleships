package application.ships;

public class Cruiser extends ShipPart {

    public Cruiser() {
        super(4, "Cruiser");
    }

    @Override
    public ShipPart copy() {
        return new Cruiser();
    }
}
