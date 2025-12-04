package refactored.model;

public class RoomStateData {
    public int roomId;
    public boolean[] doors;
    public boolean[] blockedDoors;
    public boolean[] lockedDoors;

    public RoomStateData() {}

    public RoomStateData(int roomId, boolean[] doors, boolean[] blockedDoors, boolean[] lockedDoors) {
        this.roomId = roomId;
        this.doors = doors;
        this.blockedDoors = blockedDoors;
        this.lockedDoors = lockedDoors;
    }
}
