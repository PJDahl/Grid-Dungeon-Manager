package userInterface.swing;

import util.DoorState;

public record RoomSnapshot(boolean hasRoom, int roomNumber, String roomName, boolean isGoalRoom, DoorState[] doors, int[] neighbours) {
    
    public static RoomSnapshot emptySnapshot() {
        return new RoomSnapshot(false, 0, "", false, new DoorState[] {DoorState.NONE, DoorState.NONE, DoorState.NONE, DoorState.NONE}, new int[] {0,0,0,0});
    }
}
