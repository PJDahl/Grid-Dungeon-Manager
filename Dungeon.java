import java.io.IOException;

public class Dungeon {

    public static void main(String[] args) {
        
        boolean validChoice = false;
        DungeonManager org = new DungeonManager();

        while(!validChoice){

            System.out.println("=== Welome to the Dungeon Manager ===");
            System.out.println("1. Continue Dungeon");
            System.out.println("2. New Dungeon");
            System.out.print("Enter your choice: ");
            String choice = System.console().readLine();
            switch (choice) {
                case "1":
                    System.out.println("Continuing existing dungeon...");
                    try {
                        org.loadDungeon();
                        mainMenu(org);
                        validChoice = true;
                    } catch (IOException e) {
                        System.err.println("Error loading dungeon: " + e.getMessage());
                        break;
                    }
                    break;
                case "2":
                    System.out.println("Starting a new dungeon...");
                    try {
                        org.newDungeon();
                        mainMenu(org);
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

    private static void mainMenu(DungeonManager org) {
        while(true){
            System.out.println("=== Main Menu ===");
            System.out.println("Current Position: (" + org.getCurrentPosition()[0] + ", " + org.getCurrentPosition()[1] + ")");
            System.out.println("1. Move to a new room");
            System.out.println("2. View room grid");
            System.out.println("3. Save and Exit");
            System.out.println("9. Exit without Saving");
            System.out.print("Enter your choice: ");

            String choice = System.console().readLine();

            switch (choice) {
                case "1":
                    org.presentRoomOptions();
                    break;
                case "2":
                    printGrid(org.getRoomGrid());
                    break;
                case "3":
                    System.out.println("Saving dungeon...");
                    try {
                        org.saveDungeon();
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


    private static void printGrid(int[][] grid) {
        int cols = grid[0].length;
        printDivider(cols);
        System.out.println("Room Grid:");
        printDivider(cols);

        for (int[] row : grid) {
            System.out.print("|");
            for (int cell : row) {
                System.out.print(" " + cell + " |");
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