package refactored.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import refactored.util.BlockedReason;
import refactored.util.Direction;
import refactored.util.MoveOutcome;
import refactored.util.Position;
import refactored.util.RoomOutcome;
import refactored.util.UnlockOutcome;

public class DungeonManager {
    private DungeonRepository repo = new DungeonRepository();
    private int[][] houseGrid;
    private ArrayList<Room> unusedRooms;
    private ArrayList<Room> placedRooms;
    private HashMap<Integer, Room> allRooms = new HashMap<>();
    private Position currentPosition;
    private Room currentRoom;
    private Room startingRoom;
    private int blockedDoorChance = 40;
    private int roomAmount = 3;
    private static final int GOAL_ROOM_NUMBER = 1;



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
        Room goalRoom = allRooms.get(GOAL_ROOM_NUMBER);
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
        DungeonSaveData saveData = repo.loadSaveData(slot);
    }

    public void saveDungeon(String slot) throws IOException {
        repo.save(slot, allRooms, unusedRooms, houseGrid, currentPosition, startingRoom.getRoomNumber(), blockedDoorChance, roomAmount);
    }

    public RoomOutcome clearDungeon(Integer roomToSave) {
        return clearDungeonInternal(roomToSave);
    }

    public RoomOutcome clearDungeon() {
        return clearDungeonInternal(null);
    }

    private RoomOutcome clearDungeonInternal(Integer roomToSave) {
        int startRoomNum = startingRoom.getRoomNumber();
        HashSet<Integer> roomsToKeep = new HashSet<>();
        roomsToKeep.add(startRoomNum);
        roomsToKeep.add(GOAL_ROOM_NUMBER);

        if (roomToSave != null) {
            if (!allRooms.containsKey(roomToSave)) {
                return new RoomOutcome.Failed(BlockedReason.INVALID_ROOM_NUMBER);
            }
            if (!placedRooms.contains(allRooms.get(roomToSave))) {
                return new RoomOutcome.Failed(BlockedReason.ROOM_NOT_PLACED);
            }
            
            roomsToKeep.add(roomToSave);
        }

        unusedRooms.addAll(placedRooms);
        unusedRooms.removeIf(room -> roomsToKeep.contains(room.getRoomNumber()));

        placedRooms.clear();
        placedRooms.add(startingRoom);
        placedRooms.add(allRooms.get(GOAL_ROOM_NUMBER));
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
        return new RoomOutcome.Cleared();
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
    
    public int getBlockedDoorChance() { return blockedDoorChance;}


    private boolean isInBounds(Position pos) {
        int row = pos.row();
        int col = pos.col();
        return row >= 0 && row < houseGrid.length && col >= 0 && col < houseGrid[0].length;
    }



    /*
     * Dungeon Configuration Methods
     */
    private void changeBlockedDoorChance(int change) {
        blockedDoorChance += change;
        if (blockedDoorChance < 0) {
            blockedDoorChance = 0;
        } else if (blockedDoorChance > 100) {
            blockedDoorChance = 100;
        }
    }

    public int increaseBlockedDoorChance() {
        changeBlockedDoorChance(10);
        return blockedDoorChance;
    }

    public int decreaseBlockedDoorChance() {
        changeBlockedDoorChance(-10);
        return blockedDoorChance;
    }

    private void setRoomAmount(int amount) {
        if (amount < 1) {
            roomAmount = 1;
        } else if (amount > 10) {
            roomAmount = 10;
        } else {
            roomAmount = amount;
        }
    }

    public int setRoomAmountToFive() {
        setRoomAmount(5);
        return roomAmount;
    }

    public int setRoomAmountToThree() {
        setRoomAmount(3);
        return roomAmount;
    }



    /*
     * Movement and Room handling Methods
     */
    public MoveOutcome goToRoomByRoomNumber(int roomNumber) {
        if (!allRooms.containsKey(roomNumber)) {
            return new MoveOutcome.Blocked(BlockedReason.INVALID_ROOM_NUMBER);
        }
        Position position = getRoomPosition(roomNumber);
        if(position != null){
            currentPosition = position;
            currentRoom = getRoom(roomNumber);
            return new MoveOutcome.Moved(currentRoom, currentPosition);
        }
        return new MoveOutcome.Blocked(BlockedReason.ROOM_NOT_PLACED);
    }

    public MoveOutcome tryToMove(Direction direction) {
        Position newPosition = currentPosition.move(direction);
        if (!isInBounds(newPosition)) {
            return new MoveOutcome.Blocked(BlockedReason.OUT_OF_BOUNDS);
        }

        int dirIndex = direction.getIndex();
        if (!currentRoom.doesDoorExist(dirIndex)) {
            return new MoveOutcome.Blocked(BlockedReason.NO_DOOR);
        }
        if (currentRoom.isDoorBlocked(dirIndex)) {
            return new MoveOutcome.Blocked(BlockedReason.DOOR_BLOCKED);
        }
        if (currentRoom.isDoorLocked(dirIndex)) {
            return new MoveOutcome.Blocked(BlockedReason.DOOR_LOCKED);
        }
        
        int nextRoomNumber = houseGrid[newPosition.row()][newPosition.col()];
        if (nextRoomNumber == 0) {
            List<Room> options = getRandomRooms(newPosition);
            if (options.isEmpty()) {
                return new MoveOutcome.Blocked(BlockedReason.NO_ROOM_OPTIONS);
            }
            return new MoveOutcome.NeedsPlacement(options, newPosition);
        }

        Room nextRoom = allRooms.get(nextRoomNumber);
        currentPosition = newPosition;
        currentRoom = nextRoom;
        return new MoveOutcome.Moved(nextRoom, newPosition);
    }

    private List<Room> getRandomRooms(Position targetPosition) {
        ArrayList<Room> pool = new ArrayList<>(unusedRooms);
        ArrayList<Room> selectedRooms = new ArrayList<>();

        Collections.shuffle(pool);

        for (Room room : pool) {
            if (checkRoomPrerequisites(room, targetPosition)) {
                selectedRooms.add(room);
                if (selectedRooms.size() >= roomAmount) {
                    break;
                }
            }
        }
        return selectedRooms;
    }

    private boolean checkRoomPrerequisites(Room room, Position targetPosition) {
        int row = targetPosition.row();
        int col = targetPosition.col();
        int numRows = houseGrid.length-1;
        int numCols = houseGrid[0].length-1;
        boolean atEdge = row == 0 || row == numRows || col == 0 || col == numCols;
        boolean atCorner = (row == 0 && col == 0) || (row == 0 && col == numCols) || (row == numRows && col == 0) || (row == numRows && col == numCols);

        String prereq = room.getPrerequisite();
        if (prereq == null) {
            return true;
        } else if (prereq.equalsIgnoreCase("edge") && atEdge) {
            return true;
        } else if (prereq.equalsIgnoreCase("center") && !atEdge) {
            return true;
        } else if (prereq.equalsIgnoreCase("NonCornerEdge") && !atCorner && atEdge) {
            return true;
        } else {
            return false;
        }
    }

    public void placeRoom(Room roomToPlace, Position targetPosition, Direction fromDirection, boolean moveInto) {
        houseGrid[targetPosition.row()][targetPosition.col()] = roomToPlace.getRoomNumber();
        unusedRooms.remove(roomToPlace);
        placedRooms.add(roomToPlace);
        setDoorsInRoom(roomToPlace, targetPosition, fromDirection);
        if (moveInto) {
            currentPosition = targetPosition;
            currentRoom = roomToPlace;
        }
    }

    public RoomOutcome forcePlaceRoom(int roomNumber, Position targetPosition, Direction fromDirection) {
        if(!isInBounds(targetPosition)) {
            return new RoomOutcome.Failed(BlockedReason.OUT_OF_BOUNDS);
        }
        if (houseGrid[targetPosition.row()][targetPosition.col()] != 0) {
            return new RoomOutcome.Failed(BlockedReason.TARGET_OCCUPIED);
        }
        if(!allRooms.containsKey(roomNumber)){
            return new RoomOutcome.Failed(BlockedReason.INVALID_ROOM_NUMBER);
        }
        Room roomToPlace = allRooms.get(roomNumber);
        if(!unusedRooms.contains(roomToPlace)){
            return new RoomOutcome.Failed(BlockedReason.ROOM_NOT_IN_POOL);
        }
        if(placedRooms.contains(roomToPlace)){
            return new RoomOutcome.Failed(BlockedReason.ROOM_ALREADY_PLACED);
        }
        if(!checkRoomPrerequisites(roomToPlace, targetPosition)){
            return new RoomOutcome.Failed(BlockedReason.PREREQUISITES_NOT_MET);
        }
        Position doorLeadsTo = targetPosition.move(fromDirection.opposite());
        if(!isInBounds(doorLeadsTo)){
            return new RoomOutcome.Failed(BlockedReason.DOOR_LEADS_OUT_OF_BOUNDS);
        }
        placeRoom(roomToPlace, targetPosition, fromDirection, false);
        return new RoomOutcome.Placed(roomToPlace, targetPosition);
    }

    public RoomOutcome removeRoomFromHouse(int roomNumber) {
        if(!allRooms.containsKey(roomNumber)){
            return new RoomOutcome.Failed(BlockedReason.INVALID_ROOM_NUMBER);
        }
        Room roomToRemove = allRooms.get(roomNumber);
        if(!placedRooms.contains(roomToRemove)){
            return new RoomOutcome.Failed(BlockedReason.ROOM_NOT_PLACED);
        }
        Position roomPosition = getRoomPosition(roomNumber);
        houseGrid[roomPosition.row()][roomPosition.col()] = 0;
        placedRooms.remove(roomToRemove);
        unusedRooms.add(roomToRemove);

        for (int i = 0; i < 4; i++) {
            roomToRemove.setDoorExists(i, false);
            roomToRemove.setLockStatus(i, false);
            roomToRemove.setBlockedDoor(i, false);
        }
        roomToRemove.updateConnections();

        return new RoomOutcome.Removed(roomToRemove, roomPosition);
    }

    public RoomOutcome removeRoomFromPool(int roomNumber) {
        if(!allRooms.containsKey(roomNumber)){
            return new RoomOutcome.Failed(BlockedReason.INVALID_ROOM_NUMBER);
        }
        Room roomToRemove = allRooms.get(roomNumber);
        if(!unusedRooms.contains(roomToRemove)){
            return new RoomOutcome.Failed(BlockedReason.ROOM_NOT_IN_POOL);
        }
        unusedRooms.remove(roomToRemove);
        return new RoomOutcome.Removed(roomToRemove, null);
    }



    /*
     * Door Configuration Methods
     */
    private void setDoorsInRoom(Room roomToPlace, Position targetPosition, Direction fromDirection) {
         roomToPlace.setDoorExists(fromDirection.opposite().getIndex(), true);

        List<Direction> options = new ArrayList<>();
        
        for (Direction dir : Direction.values()) {
            if (dir != fromDirection.opposite()) {
                Position adjacentPosition = targetPosition.move(dir);
                if (isInBounds(adjacentPosition)) {
                    options.add(dir);
                }
            }
        }
        Collections.shuffle(options);

        int doorsToSet = roomToPlace.getDoorCount() - 1; // One door is already set
        List<Direction> blockedCandidates = new ArrayList<>();

        Direction goalDirection = null;
        if (blockedDoorChance <= 20 && doorsToSet > 0) {
            for (Direction direction : options) {

                Position adjacentPosition = targetPosition.move(direction);
                int neighborRoomNum = houseGrid[adjacentPosition.row()][adjacentPosition.col()];
                if (neighborRoomNum == GOAL_ROOM_NUMBER && getRoom(neighborRoomNum).doesDoorExist(direction.opposite().getIndex())) {
                    roomToPlace.setDoorExists(direction.getIndex(), true);
                    doorsToSet--;
                    goalDirection = direction;
                    break;
                }
                    
            }
        }
        if(goalDirection != null){
            options.remove(goalDirection);
        }

        for (Direction direction : options) {
            if (doorsToSet <= 0) break;

            Position adjacentPosition = targetPosition.move(direction);
            int neighborRoomNum = houseGrid[adjacentPosition.row()][adjacentPosition.col()];
            if (neighborRoomNum == 0) {
                roomToPlace.setDoorExists(direction.getIndex(), true);
                if(roomToPlace.locked()) {
                    roomToPlace.setLockStatus(direction.getIndex(), true);
                }
                doorsToSet--;
            } else {
                Room neighborRoom = getRoom(neighborRoomNum);
                if (neighborRoom.doesDoorExist(direction.opposite().getIndex())) {
                    roomToPlace.setDoorExists(direction.getIndex(), true);
                    if (neighborRoom.getLockedDoors()[direction.opposite().getIndex()]){
                        neighborRoom.setLockStatus(direction.opposite().getIndex(), false);
                    }
                    doorsToSet--;
                } else {
                    int roll = (int)(Math.random() * 100) + 1;
                    if (roll <= blockedDoorChance) {
                        roomToPlace.setDoorExists(direction.getIndex(), true);
                        roomToPlace.setBlockedDoor(direction.getIndex(), true);
                        doorsToSet--;
                    } else {
                        blockedCandidates.add(direction);
                    }
                }       
            }
        }

        if (doorsToSet > 0 && !blockedCandidates.isEmpty()) {
            for (Direction dir : blockedCandidates) {
                if (doorsToSet <= 0) break;
                roomToPlace.setDoorExists(dir.getIndex(), true);
                roomToPlace.setBlockedDoor(dir.getIndex(), true);
                doorsToSet--;
            }
        }

        for (Direction direction : Direction.values()) {
            Position adjacentPosition = targetPosition.move(direction);
            if (isInBounds(adjacentPosition)){
                int neighborNum = houseGrid[adjacentPosition.row()][adjacentPosition.col()];
                if (neighborNum != 0) {
                    Room neighbor = getRoom(neighborNum);
                    int neighborDoorIndex = direction.opposite().getIndex();
                    if (neighbor.doesDoorExist(neighborDoorIndex) && !roomToPlace.doesDoorExist(direction.getIndex())) {
                        neighbor.setBlockedDoor(neighborDoorIndex, true);
                    }
                }
            }
        }

        roomToPlace.updateConnections();
    }

    public UnlockOutcome unlockDoor(Direction direction) {
        int dirIndex = direction.getIndex();
        if (!currentRoom.doesDoorExist(dirIndex)) {
            return UnlockOutcome.NO_DOOR;
        }
        if(currentRoom.isDoorBlocked(dirIndex)) {
            return UnlockOutcome.DOOR_BLOCKED;
        }
        if (!currentRoom.isDoorLocked(dirIndex)) {
            return UnlockOutcome.ALREADY_UNLOCKED;
        }
        currentRoom.setLockStatus(dirIndex, false);
        return UnlockOutcome.UNLOCKED;
    }
    
}
