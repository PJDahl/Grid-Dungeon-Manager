package model;

import util.Direction;

public record DoorInfo(Direction direction, boolean exists, boolean isLocked, boolean isBlocked, int leadsToRoomNumber, String neighbourName) {

}
