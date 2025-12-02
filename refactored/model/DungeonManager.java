package refactored.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    private Room currentRoom;
    private Room startingRoom;
    private int blocked_door_chance = 40;
    private int roomAmount = 3;

    /* 
     * Dungeon Life Cycle Methods
     * Start a new dungeon, load a dungeon, save a dungeon, clear dungeon
     */

    public void newDungeon(Position startPos, Position goalPos) {
        initializeNewRooms();
        houseGrid = new int[7][5];
        initializeStartAndGoalRooms(startPos, goalPos);
    }

    private void initializeNewRooms() {
        allRooms = repo.loadAllRooms("0");
        unusedRooms = new ArrayList<>(allRooms.values());
        placedRooms = new ArrayList<>();
    }

    private void initializeStartAndGoalRooms(Position startPosition, Position goalPosition) { 
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

    public int clearDungeon(Integer roomToSave) {
        return clearDungeonInternal(roomToSave);
    }

    public int clearDungeon() {
        return clearDungeonInternal(null);
    }

    // Returns: -1 roomToSave invalid, 0 roomToSave not placed, 1 success
    private int clearDungeonInternal(Integer roomToSave) {
        int startRoomNum = startingRoom.getRoomNumber();
        int goalRoomNum  = 1;
        HashSet<Integer> roomsToKeep = new HashSet<>();
        roomsToKeep.add(startRoomNum);
        roomsToKeep.add(goalRoomNum);

        if (roomToSave != null) {
            if (!allRooms.containsKey(roomToSave)) {
                return -1;
            }
            if (!placedRooms.contains(allRooms.get(roomToSave))) {
                return 0;
            }
            
            roomsToKeep.add(roomToSave);
        }

        unusedRooms.addAll(placedRooms);
        unusedRooms.removeIf(room -> roomsToKeep.contains(room.getRoomNumber()));

        placedRooms.clear();
        placedRooms.add(startingRoom);
        placedRooms.add(allRooms.get(goalRoomNum));
        if(roomToSave != null){
            placedRooms.add(allRooms.get(roomToSave));
        }

        for (int row = 0; row < houseGrid.length; row++) {
            for (int col = 0; col < houseGrid[0].length; col++) {
                int roomNum = houseGrid[row][col];
                if (!roomsToKeep.contains(roomNum)) {
                    houseGrid[row][col] = 0;
                }
            }
        }

        for (Room room : allRooms.values()) {
            if (room != null) {
                if (!roomsToKeep.contains(room.getRoomNumber())) {  
                    for (int i = 0; i < 4; i++) {
                        room.setDoorExists(i, false);
                        room.setLockStatus(i, false);
                        room.setBlockedDoor(i, false);
                    }
                    room.updateConnections();
                } else {
                    for (int i = 0; i < 4; i++){
                        room.setBlockedDoor(i, false);
                    }
                }      
            }
        }

        if(roomToSave != null){
            Position pos = getRoomPosition(roomToSave);
            currentPosition = pos;
            currentRoom = allRooms.get(roomToSave);
        } else {
            currentPosition = getRoomPosition(startRoomNum);
            currentRoom = startingRoom;
        }
        return 1;
    }

    /*
     * Helper Methods
     */
    private Position getRoomPosition(int roomNumber) { 
        for (int row = 0; row < houseGrid.length; row++) {
            for (int col = 0; col < houseGrid[0].length; col++) {
                if (houseGrid[row][col] == roomNumber) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }

    public boolean roomIsPlaced(int roomNumber){
        return placedRooms.contains(allRooms.get(roomNumber));
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

    /*
     * Room Placement Methods
     */
    public void placeRoom(Room room, Position position, Direction fromDirection) {
        placedRooms.add(room);
        unusedRooms.remove(room);
        houseGrid[position.row()][position.col()] = room.getRoomNumber();

        Direction toDirection = fromDirection.opposite();
        room.setDoorExists(toDirection.getIndex(), true);
        setDoorsInNewRoom(room, position, fromDirection);
    }
    
}
