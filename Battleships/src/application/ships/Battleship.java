package application.ships;

public class Battleship extends ShipPart {

    public Battleship() {
        super(5, "Battleship");
    }

    @Override
    public ShipPart copy() {
        return new Battleship();
    }
}


