public enum Direction {
    North(0, +1, 0),
    East(1, 0, +1),
    South(2, -1, 0),
    West(3, 0, -1);

    private final int index;
    private final int deltaRow;
    private final int deltaCol;

    Direction(int index, int deltaRow, int deltaCol) {
        this.index = index;
        this.deltaRow = deltaRow;
        this.deltaCol = deltaCol;
    }

    public Direction opposite() {
        switch (this) {
            case North:
                return South;
            case East:
                return West;
            case South:
                return North;
            case West:
                return East;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public static Direction fromChar(char dirChar) {
        switch (Character.toUpperCase(dirChar)) {
            case 'N':
                return North;
            case 'E':
                return East;
            case 'S':
                return South;
            case 'W':
                return West;
            default:
                throw new IllegalArgumentException("Invalid direction: " + dirChar);
        }
    }

    public int getIndex() {
        return index;
    }

    public int getDeltaRow() {
        return deltaRow;
    }

    public int getDeltaCol() {
        return deltaCol;
    }
    
}
