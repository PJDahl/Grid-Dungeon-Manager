public enum Direction {
    N(0, +1, 0),
    E(1, 0, +1),
    S(2, -1, 0),
    W(3, 0, -1);

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
            case N:
                return S;
            case E:
                return W;
            case S:
                return N;
            case W:
                return E;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public static Direction fromChar(char dirChar) {
        switch (Character.toUpperCase(dirChar)) {
            case 'N':
                return N;
            case 'E':
                return E;
            case 'S':
                return S;
            case 'W':
                return W;
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
