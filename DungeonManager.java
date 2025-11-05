import java.io.IOException;
import java.util.ArrayList;

public class DungeonManager {
    private int[][] roomGrid;
    private ArrayList<Room> unusedRooms;
    private ArrayList<Room> allRooms;
    private int[] currentPosition;
    private Room currentRoom;

    public void intializeNewRooms() throws IOException {
        allRooms = DungeonLoader.readRooms("rooms.csv");
        unusedRooms = new ArrayList<>(allRooms);
        unusedRooms.remove(0); // Remove the goal room from unused rooms
        currentRoom = unusedRooms.remove(0); // Remove the starting room from unused rooms and set as current room
        currentPosition = new int[]{0, 2}; // Starting position
    }

    public void intializeNewGrid(int i, int j, int k, int l) {
        roomGrid = new int[i][j];
        roomGrid[k][l] = 1; // Goal at position (k, l)
        roomGrid[0][2] = 2; // Start at position (0, 2)
    }

    private ArrayList<Room> getRandomRooms(int amount) {
        ArrayList<Room> selectedRooms = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            int index = (int) (Math.random() * unusedRooms.size());
            selectedRooms.add(unusedRooms.remove(index));
        }
        return selectedRooms;
    }

    private Room chooseRoom(ArrayList<Room> selectedRooms) {
        System.out.println("Choose a room by entering its number:");
        int choice = -1;
        while (choice < 1 || choice > selectedRooms.size()) {
            try {
                choice = Integer.parseInt(System.console().readLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 1 and " + selectedRooms.size() + ":");
            }
        }
        Room chosenRoom = selectedRooms.get(choice - 1);
        return chosenRoom;
    }

    public void presentRoomOptions() {
        boolean unableToMove = true;
        char direction = ' ';
        while (unableToMove) {
            direction = getDirectionFromUser();
            unableToMove = updateCurrentPosition(direction);
        }
        ArrayList<Room> selectedRooms = getRandomRooms(3);
        for (int i = 0; i < selectedRooms.size(); i++) {
            System.out.println((i + 1) + ": " + selectedRooms.get(i).getName());
        }
        Room chosenRoom = chooseRoom(selectedRooms);
        unusedRooms.remove(chosenRoom);
        System.out.println("You have chosen: " + chosenRoom.getName());
        roomGrid[currentPosition[0]][currentPosition[1]] = chosenRoom.getRoomNumber();
    }

    private char getDirectionFromUser() {
        System.out.println("Enter direction to move (N/S/E/W):");
        char direction = ' ';
        while (direction != 'N' && direction != 'S' && direction != 'E' && direction != 'W') {
            String input = System.console().readLine().toUpperCase();
            if (input.length() == 1) {
                direction = input.charAt(0);
            }
            if (direction != 'N' && direction != 'S' && direction != 'E' && direction != 'W') {
                System.out.println("Invalid input. Please enter N, S, E, or W:");
            }
        }
        return direction;
    }

    private boolean updateCurrentPosition(char direction) {
        boolean unableToMove = false;
        switch (direction) {
            case 'N':
                if (currentPosition[0] >= roomGrid.length - 1) {
                    System.out.println("Cannot move North. You're at the edge of the grid.");
                    unableToMove = true;
                } else {
                    currentPosition[0]++;
                }
                break;
            case 'S':
                if (currentPosition[0] <= 0) {
                    System.out.println("Cannot move South. You're at the edge of the grid.");
                    unableToMove = true;
                } else {
                    currentPosition[0]--;
                }
                break;
            case 'E':
                if (currentPosition[1] >= roomGrid[0].length - 1) {
                    System.out.println("Cannot move East. You're at the edge of the grid.");
                    unableToMove = true;
                } else {
                    currentPosition[1]++;
                }
                break;
            case 'W':
                if (currentPosition[1] <= 0) {
                    System.out.println("Cannot move West. You're at the edge of the grid.");
                    unableToMove = true;
                } else {
                    currentPosition[1]--;
                }
                break;
        }
        return unableToMove;
    }

    public int[][] getRoomGrid() {
        return roomGrid;
    }

    public int[] getCurrentPosition() {
        return currentPosition;
    }

    public void loadDungeon() throws IOException {
        allRooms = DungeonLoader.readRooms("allRooms.csv");
        unusedRooms = DungeonLoader.readRooms("unusedRooms.csv");
        DungeonLoader.GridData data = DungeonLoader.readGrid("roomGrid.csv");
        roomGrid = data.grid;
        currentPosition = data.position;
        currentRoom = allRooms.get(roomGrid[currentPosition[0]][currentPosition[1]]-1);
    }

    public void newDungeon() throws IOException {
        intializeNewRooms();
        intializeNewGrid(7, 5, 4, 2);
        
    }

    public void saveDungeon() throws IOException {
        DungeonSaver.saveRooms("allRooms.csv", allRooms);
        DungeonSaver.saveRooms("unusedRooms.csv", unusedRooms);
        DungeonSaver.saveGrid("roomGrid.csv", roomGrid, currentPosition);
    }
    
}
