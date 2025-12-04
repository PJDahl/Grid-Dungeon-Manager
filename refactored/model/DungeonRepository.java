package refactored.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import refactored.util.Position;

public class DungeonRepository {

    public HashMap<Integer, Room> loadAllRooms(String slot) {
        // Implementation to load rooms from a data source
        return new HashMap<>();
    }

    public DungeonSaveData loadSaveData(String slot) {
        // Implementation to load saved rooms from a data source
        return new DungeonSaveData();
    }

    public void save(String slot, Map<Integer, Room> allRooms, List<Room> unusedRooms, int[][] houseGrid,
                     Position currentPosition, int startingRoomId, int blockedDoorChance, int roomAmount) {
        // Implementation to save rooms to a data source
    }

}
