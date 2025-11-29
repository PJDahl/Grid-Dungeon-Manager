package refactored.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import refactored.util.Direction;
import refactored.util.Position;

public class DungeonManager {
    private DungeonRepository repo = new DungeonRepository();
    private int[][] houseGrid;
    private ArrayList<Room> unusedRooms;
    private ArrayList<Room> placedRooms;
    private HashMap<Integer, Room> allRooms = new HashMap<>();
    private Position currentPosition;
    private Position startPosition;
    private Room currentRoom;
    private Room startingRoom;
    private int blocked_door_chance = 40;
    private int roomAmount = 3;

    /* 
     * Dungeon Initialization
     */

    public void newDungeon(Position startPos, Position goalPos) {
        startPosition = startPos;
        initializeNewRooms();
        houseGrid = new int[7][5];
        initializeStartAndGoalRooms(goalPos);
    }

    private void initializeNewRooms() {
        allRooms = repo.loadAllRooms("0");
        unusedRooms = new ArrayList<>(allRooms.values());
        placedRooms = new ArrayList<>();
    }

    private void initializeStartAndGoalRooms(Position goalPosition) { 
        Room goalRoom = allRooms.get(1);
        unusedRooms.remove(goalRoom);
        placedRooms.add(goalRoom);
        houseGrid[goalPosition.row()][goalPosition.col()] = goalRoom.getRoomNumber();
        goalRoom.setDoorExists(Direction.North.getIndex(), true);

        startingRoom = allRooms.get(2);
        unusedRooms.remove(startingRoom);
        placedRooms.add(startingRoom);
        houseGrid[startPosition.row()][startPosition.col()] = startingRoom.getRoomNumber();
        startingRoom.setDoorExists(Direction.North.getIndex(), true);
        startingRoom.setDoorExists(Direction.East.getIndex(), true);
        startingRoom.setDoorExists(Direction.West.getIndex(), true);
        startingRoom.setDoorExists(Direction.South.getIndex(), true);
        currentRoom = startingRoom;
        currentPosition = startPosition;
    }

    public void loadDungeon(String slot) throws IOException {
        allRooms = repo.loadAllRooms(slot);
        unusedRooms = repo.loadUnusedRooms(slot);
        houseGrid = repo.loadGrid(slot);
        currentPosition = repo.loadPosition(slot);
        repo.loadRoomStates(slot, allRooms);
        currentRoom = allRooms.get(houseGrid[currentPosition.row()][currentPosition.col()]);
    }

    public void saveDungeon(String slot) throws IOException {
        repo.save(slot, allRooms, unusedRooms, houseGrid, currentPosition);
    }

    /* 
     * Getters
     */

    public int[][] getHouseGrid() { return houseGrid;}

    public Room getRoom(int roomNumber){ return allRooms.get(roomNumber);}

    public Position getCurrentPosition() { return currentPosition;}

    public Room getCurrentRoom() { return currentRoom;}


    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < houseGrid.length && col >= 0 && col < houseGrid[0].length;
    }

    
}
