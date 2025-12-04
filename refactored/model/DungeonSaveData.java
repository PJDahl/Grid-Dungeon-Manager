package refactored.model;

import java.util.List;
import refactored.util.Position;

public class DungeonSaveData {
    public int[][] houseGrid;
    public Position currentPosition;
    public int startingRoomId;
    public int blockedDoorChance;
    public int roomAmount;
    public List<Integer> unusedRoomIds;
    public List<RoomStateData> roomStates;
}
