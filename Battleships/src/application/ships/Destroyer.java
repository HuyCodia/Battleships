package application.ships;

public class Destroyer extends ShipPart {

    public Destroyer() {
        super(3, "Destroyer");
    }

    @Override
    public ShipPart copy() {
        return new Destroyer();
    }
}
