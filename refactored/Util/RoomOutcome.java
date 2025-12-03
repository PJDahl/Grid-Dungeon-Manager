package refactored.util;

import refactored.model.Room;

public interface RoomOutcome {
    
    record Placed(Room newRoom, Position newPosition) implements RoomOutcome {}

    record Removed(Room removedRoom, Position oldPosition) implements RoomOutcome {}
    
    record Failed(BlockedReason reason) implements RoomOutcome {}

    record Cleared() implements RoomOutcome {}
    
}