import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Dungeon {

    public static void main(String[] args) {
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
        System.setErr(new java.io.PrintStream(System.err, true, java.nio.charset.StandardCharsets.UTF_8));

        
        boolean validChoice = false;
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);
        DungeonManager manager = new DungeonManager(in);

        while(!validChoice){

            System.out.println("=== Welome to the Dungeon Manager ===");
            System.out.println("1. Continue Dungeon");
            System.out.println("2. New Dungeon");
            System.out.print("Enter your choice: ");
            String choice = in.nextLine();
            switch (choice) {
                case "1":
                    System.out.println("Continuing existing dungeon...");
                    try {
                        manager.loadDungeon();
                        mainMenu(manager, in);
                        validChoice = true;
                    } catch (IOException e) {
                        System.err.println("Error loading dungeon: " + e.getMessage());
                        break;
                    }
                    break;
                case "2":
                    System.out.println("Starting a new dungeon...");
                    try {
                        manager.newDungeon();
                        mainMenu(manager, in);
                        validChoice = true;
                    } catch (IOException e) {
                        System.err.println("Error creating new dungeon: " + e.getMessage());
                        break;
                    }
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    validChoice = false;
                    break;
            }
        }     
    }

    private static void mainMenu(DungeonManager manager, Scanner in) {
        while(true){
            Room currentRoom = manager.getCurrentRoom();
            System.out.println("=== Main Menu ===");
            System.out.println("Current Room: " + currentRoom.getRoomNumber() + " (" + currentRoom.getName() + ")");
            System.out.println("Current Position: (" + manager.getCurrentPosition()[0] + ", " + manager.getCurrentPosition()[1] + ")");
            System.out.println(manager.describeDoors(currentRoom));
            System.out.println("1. Move to a new room");
            System.out.println("2. View room grid");
            System.out.println("3. Print room details");
            System.out.println("4. Save and Exit");
            System.out.println("9. Exit without Saving");
            System.out.print("Enter your choice: ");

            String choice = in.nextLine();

            switch (choice) {
                case "1":
                    manager.presentRoomOptions();
                    break;
                case "2":
                    printGrid(manager.getRoomGrid());
                    break;
                case "3":
                    System.out.println(manager.getCurrentRoom());
                    break;
                case "4":
                    System.out.println("Saving dungeon...");
                    try {
                        manager.saveDungeon();
                        System.out.println("Exiting the manager.");
                        System.exit(0);
                    } catch (IOException e) {
                        System.out.println("An error occurred while saving the dungeon: " + e.getMessage());
                    }
                    break;
                case "9":
                    System.out.println("Exiting without saving.");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    //Useful for later enhancements
    private static int[] findRoomPosition(int[][] grid, int roomNumber) {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (grid[row][col] == roomNumber) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    private static void printGrid(int[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;

        printDivider(cols);
        System.out.println("Room Grid:");
        printDivider(cols);
        for (int r = rows - 1; r >= 0; r--) {
            System.out.print("|");
            for (int c = 0; c < cols; c++) {
                System.out.print(" " + grid[r][c] + " |");
            }
            System.out.println();
            printDivider(cols);
        }
    }

    private static void printDivider(int cols) {
        for (int k = 0; k < cols; k++) {
            System.out.print("----");
        }
        System.out.println("-");
    }
}