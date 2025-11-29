package refactored.util;

public record Position(int row, int col) {
    
    public Position move(Direction direction) {
        return new Position(row + direction.getDeltaRow(), col + direction.getDeltaCol());
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }
}
