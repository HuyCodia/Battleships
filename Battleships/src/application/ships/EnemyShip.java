package application.ships;

public class EnemyShip extends ShipPart {

    public EnemyShip() {
        super(5, "Enemy");
    }

    @Override
    public ShipPart copy() {
        return new EnemyShip();
    }
}
