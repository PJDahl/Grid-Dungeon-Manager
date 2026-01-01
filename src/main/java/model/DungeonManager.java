package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import userInterface.swing.RoomSnapshot;
import util.BlockedReason;
import util.Direction;
import util.DoorState;
import util.MoveOutcome;
import util.Position;
import util.RoomOutcome;
import util.UnlockOutcome;

public class DungeonManager {
    private DungeonRepository repo = new DungeonRepository();
    private int[][] houseGrid;
    private ArrayList<Room> unusedRooms;
    private ArrayList<Room> placedRooms;
    private Map<Integer, Room> allRooms;
    private Position currentPosition;
    private Room currentRoom;
    private Room startingRoom;
    private int blockedDoorChance = 40;
    private int roomAmount = 3;
    private static final int GOAL_ROOM_NUMBER = 1;
    private Integer currentSaveSlot = null;



    /* 
     * Dungeon Life Cycle Methods
     * Start a new dungeon, load a dungeon, save a dungeon, clear dungeon
     */

    public void newDungeon(Position startPos, Position goalPos) throws IOException {
        initializeNewRooms();
        houseGrid = new int[7][5];
        initializeStartAndGoalRooms(startPos, goalPos);
    }

    private void initializeNewRooms() throws IOException {
        allRooms = repo.loadAllRooms();
        unusedRooms = new ArrayList<>(allRooms.values());
        placedRooms = new ArrayList<>();
    }

    private void initializeStartAndGoalRooms(Position startPosition, Position goalPosition) { 
        Room goalRoom = getRoom(GOAL_ROOM_NUMBER);
        unusedRooms.remove(goalRoom);
        placedRooms.add(goalRoom);
        houseGrid[goalPosition.row()][goalPosition.col()] = goalRoom.getRoomNumber();
        goalRoom.setDoorState(Direction.North, DoorState.OPEN);

        startingRoom = getRoom(2);
        unusedRooms.remove(startingRoom);
        placedRooms.add(startingRoom);
        houseGrid[startPosition.row()][startPosition.col()] = startingRoom.getRoomNumber();
        startingRoom.setDoorState(Direction.North, DoorState.OPEN);
        startingRoom.setDoorState(Direction.East, DoorState.LOCKED);
        startingRoom.setDoorState(Direction.West, DoorState.BLOCKED);
        startingRoom.setDoorState(Direction.South, DoorState.OPEN);

        Room addedRoom = getRoom(28);
        unusedRooms.remove(addedRoom);
        placedRooms.add(addedRoom);
        houseGrid[startPosition.row()+1][startPosition.col()] = addedRoom.getRoomNumber();
        addedRoom.setDoorState(Direction.North, DoorState.BLOCKED);
        addedRoom.setDoorState(Direction.East, DoorState.LOCKED);
        addedRoom.setDoorState(Direction.South, DoorState.OPEN);
        addedRoom.setDoorState(Direction.West, DoorState.OPEN);

        currentRoom = startingRoom;
        currentPosition = startPosition;
    }

    public void loadDungeon(int slot) throws IOException {
        allRooms = repo.loadAllRooms();
        DungeonSaveData saveData = repo.loadSaveData(slot);
        this.houseGrid = saveData.houseGrid;
        this.currentPosition = saveData.currentPosition;
        this.startingRoom = getRoom(saveData.startingRoomId);
        this.blockedDoorChance = saveData.blockedDoorChance;
        this.roomAmount = saveData.roomAmount;
        this.unusedRooms = new ArrayList<>();
        for (Integer roomNumber : saveData.unusedRoomIds) {
            Room room = getRoom(roomNumber);
            if(room != null) {
                unusedRooms.add(room);
            }
        }
        this.placedRooms = new ArrayList<>();
        for (int i = 0; i < houseGrid.length; i++) {
            for (int j = 0; j < houseGrid[0].length; j++) {
                int roomNum = houseGrid[i][j];
                if (roomNum != 0) {
                    Room room = getRoom(roomNum);
                    if (room != null && !placedRooms.contains(room)) {
                        placedRooms.add(room);
                    }
                }
            }
        }
        for (RoomStateData stateData : saveData.roomStates) {
            Room room = getRoom(stateData.roomNumber);
            if (room != null) {
                DoorState[] doors = stateData.doors;
                for (int k = 0; k < 4; k++) {
                    room.setDoorState(Direction.values()[k], doors[k]);
                }
            }
        }

        this.currentRoom = getRoom(houseGrid[currentPosition.row()][currentPosition.col()]);
        currentSaveSlot = slot;
    }

    public void saveDungeon(int slot) throws IOException {
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
            if (!placedRooms.contains(getRoom(roomToSave))) {
                return new RoomOutcome.Failed(BlockedReason.ROOM_NOT_PLACED);
            }
            
            roomsToKeep.add(roomToSave);
        }

        unusedRooms.addAll(placedRooms);
        unusedRooms.removeIf(room -> roomsToKeep.contains(room.getRoomNumber()));

        placedRooms.clear();
        placedRooms.add(startingRoom);
        placedRooms.add(getRoom(GOAL_ROOM_NUMBER));
        if(roomToSave != null){
            placedRooms.add(getRoom(roomToSave));
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
                    for (Direction dir : Direction.values()) {
                        room.setDoorState(dir, DoorState.NONE);
                    }
                } else {
                    for (Direction dir : Direction.values()){
                        if (room.getDoorState(dir) == DoorState.BLOCKED) {
                            room.setDoorState(dir, DoorState.OPEN);
                        }
                    }
                }      
            }
        }

        if(roomToSave != null){
            Position pos = getRoomPosition(roomToSave);
            currentPosition = pos;
            currentRoom = getRoom(roomToSave);
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

    private boolean isRoomPlaced(int roomNumber){
        return placedRooms.contains(getRoom(roomNumber));
    }

    private boolean isInBounds(Position pos) {
        int row = pos.row();
        int col = pos.col();
        return row >= 0 && row < houseGrid.length && col >= 0 && col < houseGrid[0].length;
    }



    /* 
     * Getters
     */

    public int[][] getHouseGrid() { return houseGrid;}

    public Room getRoom(int roomNumber){ return allRooms.get(roomNumber);}

    public Position getCurrentPosition() { return currentPosition;}

    public Room getCurrentRoom() { return currentRoom;}
    
    public int getBlockedDoorChance() { return blockedDoorChance;}

    public Integer getCurrentSaveSlot() { return currentSaveSlot; }

    public Room getRoomAtPosition(Position pos) {
        if (!isInBounds(pos)) {
            return null;
        }
        int roomNumber = houseGrid[pos.row()][pos.col()];
        return getRoom(roomNumber);
    }



    /*
     * Information Retrieval Methods
     */
    public List<DoorInfo> getDoorInfo(int roomNumber) {
        Room room = getRoom(roomNumber);
        List<DoorInfo> doorInfos = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            if(room.doesDoorExist(dir)){
                DoorState state = room.getDoorState(dir);
                
                Position adjacentPosition = getRoomPosition(roomNumber).move(dir);
                int neighborRoomNum = houseGrid[adjacentPosition.row()][adjacentPosition.col()];
                if(neighborRoomNum == 0){
                    doorInfos.add(new DoorInfo(dir, state, 0, null));
                    continue;
                }
                Room neighborRoom = getRoom(neighborRoomNum);
                doorInfos.add(new DoorInfo(dir, state, neighborRoomNum, neighborRoom.getName()));
            }
        }
        return doorInfos;
    }

    public ArrayList<String> getAllMiniaturesInHouse() {
        ArrayList<String> miniatures = new ArrayList<>();
        for (int row = 0; row < houseGrid.length; row++) {
            for (int col = 0; col < houseGrid[0].length; col++) {
                int roomNumber = houseGrid[row][col];
                if (roomNumber != 0) {
                    Room room = getRoom(roomNumber);
                    String miniature = room.getMiniature();
                    if (miniature != null && !miniature.equals("-") && !miniatures.contains(miniature) && !miniature.equalsIgnoreCase("ingen")) {
                        miniatures.add(miniature);
                    }
                }
            }
        }
        return miniatures;
    }

    public boolean isSlotOccupied(int slot) {
        return repo.saveExists(slot);
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
    public RoomSnapshot getRoomSnapshot(Position position) {
        if (!isInBounds(position)) {
            return RoomSnapshot.emptySnapshot();
        }

        int roomNum = houseGrid[position.row()][position.col()];
        if (roomNum == 0) {
            return RoomSnapshot.emptySnapshot();
        }

        Room room = allRooms.get(roomNum);
        if (room == null) {
            return RoomSnapshot.emptySnapshot();
        }

        int[] neighbours = new int[4];
        neighbours[Direction.North.getIndex()] = roomNumberOrZero(getRoomAtPosition(position.move(Direction.North)));
        neighbours[Direction.East.getIndex()]  = roomNumberOrZero(getRoomAtPosition(position.move(Direction.East)));
        neighbours[Direction.South.getIndex()] = roomNumberOrZero(getRoomAtPosition(position.move(Direction.South)));
        neighbours[Direction.West.getIndex()]  = roomNumberOrZero(getRoomAtPosition(position.move(Direction.West)));

        DoorState[] doors = new DoorState[4];
        doors[Direction.North.getIndex()] = room.getDoorState(Direction.North);
        doors[Direction.East.getIndex()]  = room.getDoorState(Direction.East);
        doors[Direction.South.getIndex()] = room.getDoorState(Direction.South);
        doors[Direction.West.getIndex()]  = room.getDoorState(Direction.West);

        return new RoomSnapshot(
                true,
                room.getRoomNumber(),
                room.getName(),
                room.getRoomNumber() == GOAL_ROOM_NUMBER,
                doors,
                neighbours
        );
    }

    private static int roomNumberOrZero(Room r) {
        return (r == null) ? 0 : r.getRoomNumber();
    }


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

        if (!currentRoom.doesDoorExist(direction)) {
            return new MoveOutcome.Blocked(BlockedReason.NO_DOOR);
        }
        if (currentRoom.isDoorBlocked(direction)) {
            return new MoveOutcome.Blocked(BlockedReason.DOOR_BLOCKED);
        }
        if (currentRoom.isDoorLocked(direction)) {
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

        Room nextRoom = getRoom(nextRoomNumber);
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
        Room roomToPlace = getRoom(roomNumber);
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
        Room roomToRemove = getRoom(roomNumber);
        if(!placedRooms.contains(roomToRemove)){
            return new RoomOutcome.Failed(BlockedReason.ROOM_NOT_PLACED);
        }
        Position roomPosition = getRoomPosition(roomNumber);
        houseGrid[roomPosition.row()][roomPosition.col()] = 0;
        placedRooms.remove(roomToRemove);
        unusedRooms.add(roomToRemove);

        for (Direction dir : Direction.values()) {
            roomToRemove.setDoorState(dir, DoorState.NONE);
        }
        return new RoomOutcome.Removed(roomToRemove, roomPosition);
    }

    public RoomOutcome removeRoomFromPool(int roomNumber) {
        if(!allRooms.containsKey(roomNumber)){
            return new RoomOutcome.Failed(BlockedReason.INVALID_ROOM_NUMBER);
        }
        Room roomToRemove = getRoom(roomNumber);
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
        roomToPlace.setDoorState(fromDirection.opposite(), DoorState.OPEN);

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
                if (neighborRoomNum == GOAL_ROOM_NUMBER && getRoom(neighborRoomNum).doesDoorExist(direction.opposite())) {
                    roomToPlace.setDoorState(direction, DoorState.OPEN);
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
                if(roomToPlace.shouldBeLocked()) {
                    roomToPlace.setDoorState(direction, DoorState.LOCKED);
                } else {
                    roomToPlace.setDoorState(direction, DoorState.OPEN);
                }
                doorsToSet--;
            } else {
                Room neighborRoom = getRoom(neighborRoomNum);
                if (neighborRoom.doesDoorExist(direction.opposite())) {
                    roomToPlace.setDoorState(direction, DoorState.OPEN);
                    if (neighborRoom.isDoorLocked(direction.opposite())){
                        neighborRoom.setDoorState(direction.opposite(), DoorState.OPEN);
                    }
                    doorsToSet--;
                } else {
                    int roll = (int)(Math.random() * 100) + 1;
                    if (roll <= blockedDoorChance) {
                        roomToPlace.setDoorState(direction, DoorState.BLOCKED);
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
                roomToPlace.setDoorState(dir, DoorState.BLOCKED);
                doorsToSet--;
            }
        }

        for (Direction direction : Direction.values()) {
            Position adjacentPosition = targetPosition.move(direction);
            if (isInBounds(adjacentPosition)){
                int neighborNum = houseGrid[adjacentPosition.row()][adjacentPosition.col()];
                if (neighborNum != 0) {
                    Room neighbor = getRoom(neighborNum);
                    if (neighbor.doesDoorExist(direction.opposite()) && !roomToPlace.doesDoorExist(direction)) {
                        neighbor.setDoorState(direction.opposite(), DoorState.BLOCKED);
                    }
                }
            }
        }
    }

    public UnlockOutcome unlockDoor(Direction direction) {
        if (!currentRoom.doesDoorExist(direction)) {
            return UnlockOutcome.NO_DOOR;
        }
        if(currentRoom.isDoorBlocked(direction)) {
            return UnlockOutcome.DOOR_BLOCKED;
        }
        if (!currentRoom.isDoorLocked(direction)) {
            return UnlockOutcome.ALREADY_UNLOCKED;
        }
        currentRoom.setDoorState(direction, DoorState.OPEN);
        return UnlockOutcome.UNLOCKED;
    }
    
}