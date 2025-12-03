package refactored.util;

import java.util.List;

import refactored.model.Room;

public interface MoveOutcome {
    
    record Moved(Room newRoom,Position newPosition) implements MoveOutcome {}
    
    record Blocked(BlockedReason reason) implements MoveOutcome {}

    record NeedsPlacement(List<Room> options, Position newPosition) implements MoveOutcome {}
    
}
