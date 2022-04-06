package application.ships;

import java.util.ArrayList;
import java.util.List;

public class ShipCollection {

    public static List<ShipPart> getAvailableShips() {
        List<ShipPart> lst = new ArrayList<>(10);
        lst.add(new Battleship());
        lst.add(new Cruiser());
        lst.add(new Cruiser());
        lst.add(new Destroyer());
        lst.add(new Destroyer());
        lst.add(new Destroyer());
        lst.add(new Submarine());
        lst.add(new Submarine());
        lst.add(new Submarine());
        lst.add(new Submarine());
        return lst;
    }

}
