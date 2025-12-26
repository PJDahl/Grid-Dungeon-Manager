package model;

import util.DoorState;

public class RoomStateData {
    public int roomNumber;
    public DoorState[] doors;

    public RoomStateData() {}

    public RoomStateData(int roomNumber, DoorState[] doors) {
        this.roomNumber = roomNumber;
        this.doors = doors;
    }
}
