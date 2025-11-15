import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class DungeonManager {
    private int[][] houseGrid;
    private ArrayList<Room> unusedRooms;
    private HashMap<Integer, Room> allRooms = new HashMap<>();
    private int[] currentPosition;
    private int[] startPosition;
    private Room currentRoom;
    private Room startingRoom;
    private final Scanner in;
    private int blocked_door_chance = 40;
    private final String SAVE_DIRECTORY = "saves/";

     /*
      * Dungeon initialization and room management
      */

    public DungeonManager(Scanner in) {
        this.in = in;
    }

    public void increaseBlockedDoorChance() {
        this.blocked_door_chance -= 10;
    }

    public void decreaseBlockedDoorChance() {
        this.blocked_door_chance -= 10;
    }

    public int getBlockedDoorChance() {
        return blocked_door_chance;
    }

    public void initializeNewRooms() throws IOException {
        ArrayList<Room> loadedRooms = DungeonLoader.readRooms(".", "rooms.csv");
        for (Room room : loadedRooms) {
            allRooms.put(room.getRoomNumber(), room);
        }
        unusedRooms = new ArrayList<>(loadedRooms);
        Room goal = allRooms.get(1);
        unusedRooms.remove(goal); // Remove the goal room from unused rooms
        goal.setDoorExists(Direction.North.getIndex(), true); // Ensure goal room has a door to the north
        startingRoom = allRooms.get(2);
        unusedRooms.remove(startingRoom); // Remove the starting room from unused rooms and set as current room
        startingRoom.setDoorExists(Direction.North.getIndex(), true);
        startingRoom.setDoorExists(Direction.East.getIndex(), true);
        startingRoom.setDoorExists(Direction.West.getIndex(), true);
        startingRoom.setDoorExists(Direction.South.getIndex(), true);
        currentRoom = startingRoom;
        currentPosition = new int[]{startPosition[0], startPosition[1]}; // Starting position
    }

    public void initializeNewGrid(int i, int j, int k, int l) {
        houseGrid = new int[i][j];
        houseGrid[k][l] = 1;
        houseGrid[startPosition[0]][startPosition[1]] = 2;
    }

    private ArrayList<Room> getRandomRooms(int amount, int[] targetCoordinates) {
        ArrayList<Room> pool = new ArrayList<>(unusedRooms);
        ArrayList<Room> selectedRooms = new ArrayList<>();

        Collections.shuffle(pool);

        for (Room room : pool) {
            if (checkPrerequisite(room, targetCoordinates)) {
                selectedRooms.add(room);
                if (selectedRooms.size() >= amount) {
                    break;
                }
            }
        }
        return selectedRooms;
    }

    private boolean checkPrerequisite(Room room, int[] targetCoordinates) {
            int row = targetCoordinates[0];
            int col = targetCoordinates[1];
            int numRows = houseGrid.length-1;
            int numCols = houseGrid[0].length-1;
            boolean atEdge = row == 0 || row == numRows || col == 0 || col == numCols;
            boolean atCorner = (row == 0 && col == 0) || (row == 0 && col == numCols) ||
                               (row == numRows && col == 0) || (row == numRows && col == numCols);
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

    private Room chooseRoom(ArrayList<Room> selectedRooms) {
        System.out.print("Choose a room by entering its number: ");
        int choice = -1;
        while (choice < 1 || choice > selectedRooms.size()) {
            try {
                choice = Integer.parseInt(in.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and " + selectedRooms.size() + ": ");
            }
        }
        Room chosenRoom = selectedRooms.get(choice - 1);
        return chosenRoom;
    }

    public void presentRoomOptions() {
        System.out.print("Enter direction to move (N/S/E/W): ");
        Direction direction = getDirectionFromUser();
        if (direction == null) {
            System.out.println("Movement cancelled.");
            return;
        }
        int[] newPosition = peek(direction, currentPosition[0], currentPosition[1]);

        if (!isInBounds(newPosition[0], newPosition[1])) {
            System.out.println("Cannot move " + direction +". You're at the edge of the grid.");
            return;
        }

        if(!currentRoom.doesDoorExist(direction.getIndex())) {
            System.out.println("No door to the " + direction + ". Please choose another direction.");
            return;
        }

        int nextRoomNumber = houseGrid[newPosition[0]][newPosition[1]];
        if (nextRoomNumber != 0) {
            if(currentRoom.getBlockedDoors()[direction.getIndex()]) {
                System.out.println("The door to the " + direction + " is blocked. You cannot pass through.");
                return;
            }
            currentPosition = newPosition;
            currentRoom = getRoom(nextRoomNumber);
            System.out.println("Moved to room " + currentRoom.getRoomNumber() + " (" + currentRoom.getName() + ").");
            return;
        }

        boolean[] locked = currentRoom.getLockedDoors();
        if (locked[direction.getIndex()]) {
            System.out.println("The door to the " + direction + " is locked.");
            return;
        }

        ArrayList<Room> roomOptions = getRandomRooms(3, newPosition);
        if (roomOptions.isEmpty()) {
            System.out.println("No available rooms meet the prerequisites going " + direction + ". Try another direction.");
            return;
        }

        for (int i = 0; i < roomOptions.size(); i++) {
            System.out.println((i + 1) + ": " + roomOptions.get(i).getName() + " ("+ roomOptions.get(i).getDoorCount()+" doors)");
        }

        Room chosenRoom = chooseRoom(roomOptions);
        unusedRooms.remove(chosenRoom);
        houseGrid[newPosition[0]][newPosition[1]] = chosenRoom.getRoomNumber();
        setDoorsInNewRoom(chosenRoom, direction, newPosition[0], newPosition[1]);
        
        currentPosition = newPosition;
        currentRoom = chosenRoom;

        System.out.println("You moved " + direction +" into: " + currentRoom.getRoomNumber() + " (" + currentRoom.getName() + ").");
        return;
    }

    private Direction getDirectionFromUser() {
        char direction = ' ';
        while (direction != 'N' && direction != 'S' && direction != 'E' && direction != 'W' && direction != 'C') {
            String input = in.nextLine().toUpperCase();
            if (input.length() == 1) {
                direction = input.charAt(0);
            }
            if (direction != 'N' && direction != 'S' && direction != 'E' && direction != 'W') {
                System.out.println("Invalid input. Please enter N, S, E, or W, to cancel enter C:");
            }
        }
        if(direction == 'C'){
            return null;
        }
        return Direction.fromChar(direction);
    }

    public int[][] getHouseGrid() {
        return houseGrid;
    }

    public Room getRoom(int roomNumber){
        return allRooms.get(roomNumber);
    }

    public int[] getCurrentPosition() {
        return currentPosition;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public HashMap<Integer, Room> getAllRooms() {
        return allRooms;
    }

    /*
     * Directional logic
     */

    private int[] peek(Direction dir, int row, int col) {
        int newRow = row + dir.getDeltaRow();
        int newCol = col + dir.getDeltaCol();
        return new int[]{newRow, newCol};
    }

    private boolean isInBounds(int row, int col) {
        return row >= 0 && row < houseGrid.length && col >= 0 && col < houseGrid[0].length;
    }

    /*
     * Methods to handle doors
     */

    public void unlockDoor() {
        System.out.print("Enter door to unlock (N/S/E/W): ");
        Direction direction = getDirectionFromUser();    
        if (!currentRoom.doesDoorExist(direction.getIndex())) {
            System.out.println("There is no door to the " + direction + ".");
            return;
        }

        if (currentRoom.getBlockedDoors()[direction.getIndex()]) {
            System.out.println("The doorway to the " + direction + " is blocked and cannot be unlocked.");
            return;
        }

        if (!currentRoom.getLockedDoors()[direction.getIndex()]) {
            System.out.println("The door to the " + direction + " is already unlocked.");
            return;
        }

        currentRoom.setLockStatus(direction.getIndex(), false);
        System.out.println("Unlocked the door to the " + direction + ".");
    }

    private void setDoorsInNewRoom(Room newRoom, Direction from, int row, int col) {
        newRoom.setDoorExists(from.opposite().getIndex(), true);

        List<Direction> options = new ArrayList<>();
        
        for (Direction dir : Direction.values()) {
            if (dir != from.opposite()) {
                int[] adjacentPos = peek(dir, row, col);
                if (isInBounds(adjacentPos[0], adjacentPos[1])) {
                    options.add(dir);
                }
            }
        }
        Collections.shuffle(options);

        int doorsToSet = newRoom.getDoorCount() - 1; // One door is already set
        List<Direction> blockedCandidates = new ArrayList<>();

        if (blocked_door_chance <= 20 && doorsToSet > 0) {
            for (Direction direction : options) {

                int[] adjacentPos = peek(direction, row, col);
                int neighborRoomNum = houseGrid[adjacentPos[0]][adjacentPos[1]];
                if (neighborRoomNum == 1 && getRoom(neighborRoomNum).doesDoorExist(direction.opposite().getIndex())) {
                    newRoom.setDoorExists(direction.getIndex(), true);
                    doorsToSet--;
                    break;
                }
                    
            }
        }

        for (Direction direction : options) {
            if (doorsToSet <= 0) break;

            int[] adjacentPos = peek(direction, row, col);
            int neighborRoomNum = houseGrid[adjacentPos[0]][adjacentPos[1]];
            if (neighborRoomNum == 0) {
                newRoom.setDoorExists(direction.getIndex(), true);
                if(newRoom.locked()) {
                    newRoom.setLockStatus(direction.getIndex(), true);
                }
                doorsToSet--;
            } else {
                Room neighborRoom = getRoom(neighborRoomNum);
                if (neighborRoom.doesDoorExist(direction.opposite().getIndex())) {
                    newRoom.setDoorExists(direction.getIndex(), true);
                    if (neighborRoom.getLockedDoors()[direction.opposite().getIndex()]){
                        neighborRoom.setLockStatus(direction.opposite().getIndex(), false);
                    }
                    doorsToSet--;
                } else {
                    int roll = (int)(Math.random() * 100) + 1;
                    if (roll <= blocked_door_chance) {
                        newRoom.setDoorExists(direction.getIndex(), true);
                        newRoom.setBlockedDoor(direction.getIndex(), true);
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
                newRoom.setDoorExists(dir.getIndex(), true);
                newRoom.setBlockedDoor(dir.getIndex(), true);
                doorsToSet--;
            }
        }

        for (Direction direction : Direction.values()) {
            int[] adjacentPos = peek(direction, row, col);
            if (isInBounds(adjacentPos[0], adjacentPos[1])){
                int neighborNum = houseGrid[adjacentPos[0]][adjacentPos[1]];
                if (neighborNum != 0) {
                    Room neighbor = getRoom(neighborNum);
                    int neighborDoorIndex = direction.opposite().getIndex();
                    if (neighbor.doesDoorExist(neighborDoorIndex) && !newRoom.doesDoorExist(direction.getIndex())) {
                        neighbor.setBlockedDoor(neighborDoorIndex, true);
                    }
                }
            }
        }

        newRoom.updateConnections();
    }

    public String describeDoors(Room room) {
        StringBuilder sb = new StringBuilder();
        sb.append("Doors: ");
        String[] dirLabels = {"North", "East", "South", "West"};

        boolean[] doors = room.getDoors();
        boolean[] blocked = room.getBlockedDoors();
        boolean[] locked = room.getLockedDoors();

        boolean first = true;

        for (Direction dir : Direction.values()) {
            if (!doors[dir.getIndex()]) continue;
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(dirLabels[dir.getIndex()]);
            
            sb.append("(");
            if(room.equals(startingRoom) && dir == Direction.South) {
                sb.append("Outside (Entrance)");
                sb.append(")");
                continue;
            }
            if (blocked[dir.getIndex()]) {
                sb.append("Blocked");
            } else if (locked[dir.getIndex()]) {
                sb.append("Locked");
            } else {
                sb.append("Open");
                int[] neighborRoomPos = peek(dir, currentPosition[0], currentPosition[1]);
                if (isInBounds(neighborRoomPos[0], neighborRoomPos[1])) {
                    int neighborRoomNum = houseGrid[neighborRoomPos[0]][neighborRoomPos[1]];
                    if (neighborRoomNum != 0) {
                        Room neighborRoom = getRoom(neighborRoomNum);
                        sb.append(" - " + neighborRoomNum + "(" + neighborRoom.getName() + ")");
                    }                    
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }

    /*
     * Room navigation by room number
     * returns:
     * 1 if successful 
     * 0 if room number not found
     * -1 if invalid room number
     */

    public int goToRoomByNumber(int roomNumber) {
        if (roomNumber < 1 || roomNumber > allRooms.size()) {
            return -1;
        }
        int[] position = getRoomPosition(roomNumber);
        if(position != null){
            currentPosition[0] = position[0];
            currentPosition[1] = position[1];
            currentRoom = getRoom(roomNumber);
            return 1;
        }
        /* for (int r = 0; r < houseGrid.length; r++) {
            for (int c = 0; c < houseGrid[0].length; c++) {
                if (houseGrid[r][c] == roomNumber) {
                    currentPosition[0] = r;
                    currentPosition[1] = c;
                    currentRoom = getRoom(roomNumber);
                    return 1;
                }
            }
        } */
        return 0;
    }

    /*
     * Room navigation by coordinates
     * returns:
     * 1 if successful
     * 0 if no room at coordinates
     * -1 if out of bounds
     */

    public int goToRoomByCoordinates(int row, int col) {
        if (isInBounds(row, col)) {
            int roomNumber = houseGrid[row][col];
            if (roomNumber != 0) {
                currentPosition[0] = row;
                currentPosition[1] = col;
                currentRoom = getRoom(roomNumber);
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public int placeRoom(int roomNbr, int targetRow, int targetCol) {
        if (!isInBounds(targetRow, targetCol)) {
            return -2;
        }
        int currentRoomAtTarget = houseGrid[targetRow][targetCol];
        if (currentRoomAtTarget != 0){
            return -1;
        }

        int[] roomsCurrentPlace = getRoomPosition(roomNbr);
        
        if (roomsCurrentPlace != null){
            return 0;
        }

        Direction direction = Direction.West;
        for (int i = 3; i > 0; i--) {
            direction = Direction.values()[i];
            int[] newPos = peek(direction, targetRow, targetCol);
            if(isInBounds(newPos[0], newPos[1])){
                break;
            }
        }
        Room room = getRoom(roomNbr);
        houseGrid[targetRow][targetCol] = room.getRoomNumber();
        setDoorsInNewRoom(room, direction, targetRow, targetCol);
        return 1;        
    }

    public boolean removeRoomFromHouse(int roomNumber){
        int[] position = getRoomPosition(roomNumber);
        if (position != null){
            houseGrid[position[0]][position[1]] = 0;
            return true;
        }
        return false;
    }

    private int[] getRoomPosition(int roomNumber) { 
        for (int row = 0; row < houseGrid.length; row++) {
            for (int col = 0; col < houseGrid[0].length; col++) {
                if (houseGrid[row][col] == roomNumber) {
                    int[] result = new int[]{row, col};
                    return result;
                }
            }
        }
        return null;
    }


    /*
     * Dungeon loading and saving
     */

    public void loadDungeon(String slot) throws IOException {  
        DungeonSaver.ensureSaveDir(SAVE_DIRECTORY);      
        ArrayList<Room> loadedRooms = DungeonLoader.readRooms(SAVE_DIRECTORY, "allRooms" + slot + ".csv");
        for (Room room : loadedRooms) {
            allRooms.put(room.getRoomNumber(), room);
        }
        unusedRooms = DungeonLoader.readRooms(SAVE_DIRECTORY, "unusedRooms" + slot + ".csv");
        DungeonLoader.GridData data = DungeonLoader.readGrid(SAVE_DIRECTORY, "roomGrid" + slot + ".csv");
        DungeonLoader.loadRoomState(SAVE_DIRECTORY, "roomState" + slot + ".csv", allRooms);
        houseGrid = data.grid;
        currentPosition = data.position;
        startingRoom = getRoom(data.startingRoom);
        startPosition = getRoomPosition(startingRoom.getRoomNumber());
        currentRoom = getRoom(houseGrid[currentPosition[0]][currentPosition[1]]);
    }

    public void newDungeon() throws IOException {
        startPosition = new int[]{0,2};
        initializeNewRooms();
        initializeNewGrid(7, 5, 4, 2);
    }

    public void saveDungeon(String slot) throws IOException {
        DungeonSaver.ensureSaveDir(SAVE_DIRECTORY);
        DungeonSaver.saveRooms(SAVE_DIRECTORY, "allRooms" + slot + ".csv", new ArrayList<>(allRooms.values()));
        DungeonSaver.saveRooms(SAVE_DIRECTORY, "unusedRooms" + slot + ".csv", unusedRooms);
        DungeonSaver.saveGrid(SAVE_DIRECTORY, "roomGrid" + slot + ".csv", houseGrid, currentPosition, startingRoom.getRoomNumber());
        DungeonSaver.saveRoomState(SAVE_DIRECTORY, "roomState" + slot + ".csv", new ArrayList<>(allRooms.values()));
    }

    public boolean emptySlot(String slot) {
        DungeonSaver.ensureSaveDir(SAVE_DIRECTORY);
        boolean all = DungeonSaver.emptySlot(SAVE_DIRECTORY + "allRooms" + slot + ".csv");
        boolean unused = DungeonSaver.emptySlot(SAVE_DIRECTORY + "unusedRooms" + slot + ".csv");
        boolean grid = DungeonSaver.emptySlot(SAVE_DIRECTORY + "roomGrid" + slot + ".csv");
        boolean state = DungeonSaver.emptySlot(SAVE_DIRECTORY + "roomState" + slot + ".csv");
        return all && unused && grid && state;
    }

    public void clearDungeon() {

        int startRoomNum = startingRoom.getRoomNumber();
        int goalRoomNum = 1;

        for (int row = 0; row < houseGrid.length; row++) {
            for (int col = 0; col < houseGrid[0].length; col++) {
                int gridPosition = houseGrid[row][col];
                if (gridPosition != 0 && gridPosition != startRoomNum && gridPosition != goalRoomNum){
                    Room room = getRoom(gridPosition);
                    if (room != null) {
                        if (!unusedRooms.contains(room)) {
                            unusedRooms.add(room);
                        }
                    }
                    houseGrid[row][col] = 0;
                }
            }
        }
        unusedRooms.removeIf(room -> room.getRoomNumber() == startRoomNum || room.getRoomNumber() == goalRoomNum);

        for (Room room : allRooms.values()) {
            if (room != null) {
                if (room.getRoomNumber() != startRoomNum && room.getRoomNumber() != goalRoomNum) {  
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

        currentPosition = getRoomPosition(startRoomNum);
        currentRoom = startingRoom;
    }

    public int clearDungeonButOneRoom(int roomToSave) {
        Room savedRoom = getRoom(roomToSave);
        if (savedRoom == null){
            return -1;
        }
        if (getRoomPosition(roomToSave) == null){
            return 0;
        }

        int startRoomNum = startingRoom.getRoomNumber();
        int goalRoomNum = 1;

        for (int row = 0; row < houseGrid.length; row++) {
            for (int col = 0; col < houseGrid[0].length; col++) {
                int gridPosition = houseGrid[row][col];
                if (gridPosition != 0 && gridPosition != startRoomNum && gridPosition != goalRoomNum && gridPosition != roomToSave){
                    Room room = getRoom(gridPosition);
                    if (room != null) {
                        if (!unusedRooms.contains(room)) {
                            unusedRooms.add(room);
                        }
                    }
                    houseGrid[row][col] = 0;
                }
            }
        }

        unusedRooms.removeIf(room -> room.getRoomNumber() == startRoomNum || room.getRoomNumber() == goalRoomNum || room.getRoomNumber() == roomToSave);

        for (Room room : allRooms.values()) {
            if (room != null) {
                if (room.getRoomNumber() != startRoomNum && room.getRoomNumber() != goalRoomNum && room.getRoomNumber() != roomToSave) {  
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

        currentPosition = getRoomPosition(roomToSave);
        currentRoom = savedRoom;
        return 1;
    }
}