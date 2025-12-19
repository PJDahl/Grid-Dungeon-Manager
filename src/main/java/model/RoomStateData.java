package model;

public class RoomStateData {
    public int roomNumber;
    public boolean[] doors;
    public boolean[] blockedDoors;
    public boolean[] lockedDoors;

    public RoomStateData() {}

    public RoomStateData(int roomNumber, boolean[] doors, boolean[] blockedDoors, boolean[] lockedDoors) {
        this.roomNumber = roomNumber;
        this.doors = doors;
        this.blockedDoors = blockedDoors;
        this.lockedDoors = lockedDoors;
    }
}
