package application.ships;

public class Ship {
    private ShipPart[] parts;
    private int currentPartIndex = 0;

    public Ship(int partCount) {
        parts = new ShipPart[partCount];
    }

    public void addPart(ShipPart part) {
        parts[currentPartIndex] = part;
        currentPartIndex++;
    }

    public boolean isSunk() {
        for (ShipPart part : parts) {
            if (part != null && !part.isHit()) {
                return false;
            }
        }
        return true;
    }

    public boolean isHit() {
        for (ShipPart part : parts) {
            if (part != null && part.isHit())
                return true;
        }
        return false;
    }

}
