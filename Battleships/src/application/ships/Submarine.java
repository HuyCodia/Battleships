package application.ships;

public class Submarine extends ShipPart {

    public Submarine() {
        super(2, "Submarine");
    }

    @Override
    public ShipPart copy() {
        return new Submarine();
    }
}
