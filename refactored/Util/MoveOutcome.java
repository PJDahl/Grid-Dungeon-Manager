package refactored.util;

import refactored.model.Room;

public interface MoveOutcome {
    
    record Moved(Room newRoom,Position newPosition) implements MoveOutcome {}
    
    record Blocked(MoveBlockedReason reason) implements MoveOutcome {}

    record NeedsPlacement(Position newPosition) implements MoveOutcome {}
    
}
