package model;

import java.util.List;
import util.Position;

public class DungeonSaveData {
    public int[][] houseGrid;
    public Position currentPosition;
    public Position startingPosition;
    public int startingRoomId;
    public int blockedDoorChance;
    public int roomAmount;
    public List<Integer> unusedRoomIds;
    public List<RoomStateData> roomStates;
}
