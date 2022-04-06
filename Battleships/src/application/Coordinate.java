package application;

public class Coordinate {

    /** declare for horizontal indices in gameField */
    private int _x;

    /** declare for vertical indices in gameField */
    private int _y;

    /**
     * This is the constructor of the coordinate class.
     *
     * @param x as an integer
     * @param y as an integer
     */
    // x & y als Index (von 0 bis 9)
    public Coordinate(int x, int y) {
        setCoordinate(x, y);
    }

    /**
     * This is the constructor of the coordinate class.
     *
     * @param x as an integer
     * @param y as ab character
     */
    // Spielfeldkoordinaten: x = 1-10,  y = A-J
    public Coordinate(int x, char y) {
        setCoordinate(x, y);
    }

    /**
     * This method sets the coordinate
     *
     * @param x actual x-position as an int
     * @param y actual y-position as an int
     */
    public void setCoordinate(int x, int y) {
        this._x = x;
        this._y = y;
    }

    /**
     * This method sets the coordinate
     *
     * @param x actual x-position as an int
     * @param y actual y-position as an char
     */
    public void setCoordinate(int x, char y) {
        this._x = x - 1;
        this._y = ((int)(y) - 65);
    }

    /**
     * This method gets the indices 0 to 9 from the gameField.
     *
     * @return this._x index of horizontal-axis from gameField.
     */
    // Spielfeldindex von 0 - 9
    public int getX() {
        return this._x;
    }

    /**
     * This method gets the indices A to J from the gameField.
     * The indices are represented by 0 to 9.
     *
     * @return this._y index of vertical-axis from gameField
     */
    //Spielfeldindex wird von A bis J als 0 bis 9 ausgegeben
    public int getY() {
        return this._y;
    }

    /**
     * This method shows the selected coordinate as a string.
     *
     * @return s coordinate from gameField
     */
    @Override
    public String toString() {
        int x = this._x + 1;
        char yChar = (char) (this._y + 65); // A wird als 0 eingegeben
        String s = String.format("[%c,%d]", yChar, x);
        return s;
    }



}
