import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class DungeonManager {
    private int[][] roomGrid;
    private ArrayList<Room> unusedRooms;
    private ArrayList<Room> allRooms;
    private int[] currentPosition;
    private Room currentRoom;
    private Room startingRoom;
    private final Scanner in;
    private final int BLOCKED_DOOR_CHANCE = 33; // Percentage chance to block a door between rooms

     /*
      * Dungeon initialization and room management
      */

    public DungeonManager(Scanner in) {
        this.in = in;
    }

    public void initializeNewRooms() throws IOException {
        allRooms = DungeonLoader.readRooms("rooms.csv");
        unusedRooms = new ArrayList<>(allRooms);
        Room goal = unusedRooms.remove(0); // Remove the goal room from unused rooms
        goal.setDoorExists(Direction.N.getIndex(), true); // Ensure goal room has a door to the north
        startingRoom = unusedRooms.remove(0); // Remove the starting room from unused rooms and set as current room
        startingRoom.setDoorExists(Direction.N.getIndex(), true);
        startingRoom.setDoorExists(Direction.E.getIndex(), true);
        startingRoom.setDoorExists(Direction.W.getIndex(), true);
        startingRoom.setDoorExists(Direction.S.getIndex(), true);
        currentRoom = startingRoom;
        currentPosition = new int[]{0, 2}; // Starting position
    }

    public void initializeNewGrid(int i, int j, int k, int l) {
        roomGrid = new int[i][j];
        roomGrid[k][l] = 1; // Goal at position (k, l)
        roomGrid[0][2] = 2; // Start at position (0, 2)
    }

    private ArrayList<Room> getRandomRooms(int amount) {
        ArrayList<Room> pool = new ArrayList<>(unusedRooms);
        ArrayList<Room> selectedRooms = new ArrayList<>();

        Collections.shuffle(pool);

        for (Room room : pool) {
            if (checkPrerequisite(room)) {
                selectedRooms.add(room);
                if (selectedRooms.size() >= amount) {
                    break;
                }
            }
        }
        return selectedRooms;
    }

    private boolean checkPrerequisite(Room room) {
            int row = currentPosition[0];
            int col = currentPosition[1];
            int numRows = roomGrid.length-1;
            int numCols = roomGrid[0].length-1;
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
        System.out.println("Choose a room by entering its number:");
        int choice = -1;
        while (choice < 1 || choice > selectedRooms.size()) {
            try {
                choice = Integer.parseInt(in.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and " + selectedRooms.size() + ":");
            }
        }
        Room chosenRoom = selectedRooms.get(choice - 1);
        return chosenRoom;
    }

    public void presentRoomOptions() {
        while (true) {
            Direction direction = getDirectionFromUser();
            int[] newPosition = peek(direction, currentPosition[0], currentPosition[1]);

            if (!isInBounds(newPosition[0], newPosition[1])) {
                System.out.println("Cannot move" + direction +". You're at the edge of the grid.");
                continue;
            }

            if(!currentRoom.doesDoorExist(direction.getIndex())) {
                System.out.println("No door to the " + direction + ". Please choose another direction.");
                continue;
            }

            int nextRoomNumber = roomGrid[newPosition[0]][newPosition[1]];
            if (nextRoomNumber != 0) {
                if(currentRoom.getBlockedDoors()[direction.getIndex()]) {
                    System.out.println("The door to the " + direction + " is blocked. You cannot pass through.");
                    continue;
                }
                currentPosition = newPosition;
                currentRoom = allRooms.get(nextRoomNumber - 1);
                System.out.println("Moved to room " + currentRoom.getRoomNumber() + " (" + currentRoom.getName() + ").");
                break;
            }

            ArrayList<Room> roomOptions = getRandomRooms(3);
            if (roomOptions.isEmpty()) {
                System.out.println("No available rooms meet the prerequisites going " + direction + ". Try another direction.");
                continue;
            }

            for (int i = 0; i < roomOptions.size(); i++) {
                System.out.println((i + 1) + ": " + roomOptions.get(i).getName());
            }

            Room chosenRoom = chooseRoom(roomOptions);
            unusedRooms.remove(chosenRoom);
            roomGrid[newPosition[0]][newPosition[1]] = chosenRoom.getRoomNumber();
            setDoorsInNewRoom(chosenRoom, direction, newPosition[0], newPosition[1]);
            
            currentPosition = newPosition;
            currentRoom = chosenRoom;

            System.out.println("You moved " + direction +" into: " + currentRoom.getRoomNumber() + " (" + currentRoom.getName() + ").");
            break;
        }
    }

    private Direction getDirectionFromUser() {
        System.out.println("Enter direction to move (N/S/E/W):");
        char direction = ' ';
        while (direction != 'N' && direction != 'S' && direction != 'E' && direction != 'W') {
            String input = in.nextLine().toUpperCase();
            if (input.length() == 1) {
                direction = input.charAt(0);
            }
            if (direction != 'N' && direction != 'S' && direction != 'E' && direction != 'W') {
                System.out.println("Invalid input. Please enter N, S, E, or W:");
            }
        }
        return Direction.fromChar(direction);
    }

    public int[][] getRoomGrid() {
        return roomGrid;
    }

    public int[] getCurrentPosition() {
        return currentPosition;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public ArrayList<Room> getAllRooms() {
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
        return row >= 0 && row < roomGrid.length && col >= 0 && col < roomGrid[0].length;
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

        for (Direction dir : options) {
            if (doorsToSet <= 0) break;

            int[] adjacentPos = peek(dir, row, col);
            int neighborRoomNum = roomGrid[adjacentPos[0]][adjacentPos[1]];
            if (neighborRoomNum == 0) {
                newRoom.setDoorExists(dir.getIndex(), true);
                if(newRoom.locked()) {
                    newRoom.setLockStatus(dir.getIndex(), true);
                }
                doorsToSet--;
            } else {
                Room neighborRoom = allRooms.get(neighborRoomNum - 1);
                if (neighborRoom.doesDoorExist(dir.opposite().getIndex())) {
                    newRoom.setDoorExists(dir.getIndex(), true);
                    doorsToSet--;
                } else {
                    int roll = (int)(Math.random() * 100) + 1;
                    if (roll <= BLOCKED_DOOR_CHANCE) {
                        newRoom.setDoorExists(dir.getIndex(), true);
                        newRoom.setBlockedDoor(dir.getIndex(), true);
                        doorsToSet--;
                    } else {
                        blockedCandidates.add(dir);
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

        newRoom.updateConnections();
    }

    public String describeDoors(Room room) {
        StringBuilder sb = new StringBuilder();
        sb.append("Doors: ");
        String[] dirLabels = {"North", "East", "South", "West"};

        boolean[] doors = room.getDoors();
        boolean[] blocked = room.getBlockedDoors();   // your new field
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
            if(room.equals(startingRoom) && dir == Direction.S) {
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
                    int neighborRoomNum = roomGrid[neighborRoomPos[0]][neighborRoomPos[1]];
                    if (neighborRoomNum != 0) {
                        Room neighborRoom = allRooms.get(neighborRoomNum - 1);
                        sb.append(" - " + neighborRoomNum + "(" + neighborRoom.getName() + ")");
                    }                    
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }


    /*
     * Dungeon loading and saving
     */

    public void loadDungeon() throws IOException {
        allRooms = DungeonLoader.readRooms("allRooms.csv");
        unusedRooms = DungeonLoader.readRooms("unusedRooms.csv");
        DungeonLoader.GridData data = DungeonLoader.readGrid("roomGrid.csv");
        roomGrid = data.grid;
        currentPosition = data.position;
        currentRoom = allRooms.get(roomGrid[currentPosition[0]][currentPosition[1]]-1);
    }

    public void newDungeon() throws IOException {
        initializeNewRooms();
        initializeNewGrid(7, 5, 4, 2);
        
    }

    public void saveDungeon() throws IOException {
        DungeonSaver.saveRooms("allRooms.csv", allRooms);
        DungeonSaver.saveRooms("unusedRooms.csv", unusedRooms);
        DungeonSaver.saveGrid("roomGrid.csv", roomGrid, currentPosition);
    }
    
}
