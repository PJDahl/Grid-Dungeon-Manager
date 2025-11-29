package refactored.model;

import java.util.ArrayList;
import java.util.HashMap;

import refactored.util.Position;

public class DungeonRepository {

    public HashMap<Integer, Room> loadAllRooms(String slot) {
        // Implementation to load rooms from a data source
        return new HashMap<>();
    }

    public ArrayList<Room> loadUnusedRooms(String slot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadUnusedRooms'");
    }

    public int[][] loadGrid(String slot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadGrid'");
    }

    public Position loadPosition(String slot) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadPosition'");
    }

    public void loadRoomStates(String slot, HashMap<Integer,Room> allRooms) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadRoomStates'");
    }

    public void save(String slot, HashMap<Integer,Room> allRooms, ArrayList<Room> unusedRooms, int[][] houseGrid,
            Position currentPosition) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

}
