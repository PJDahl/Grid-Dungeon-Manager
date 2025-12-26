package model;

import util.Direction;
import util.DoorState;

public record DoorInfo(Direction direction, DoorState state, int leadsToRoomNumber, String neighbourName) {}
