import java.io.IOException;
import java.util.Scanner;

public class Dungeon {

    public static void main(String[] args) {

        boolean validChoice = false;
        Scanner in = new Scanner(System.in);
        DungeonManager manager = new DungeonManager(in);

        while(!validChoice){

            System.out.println("=== Welome to the Dungeon Manager ===");
            System.out.println("1. Continue Dungeon");
            System.out.println("2. New Dungeon");
            System.out.println("3. Delete a save");
            System.out.print("Enter your choice: ");
            String choice = in.nextLine();
            switch (choice) {
                case "1":
                    printWithSeparator("Which slot do you want to load (1 to 5): ");
                    String slot = in.nextLine();
                    if(!checkSlot(slot)){
                        printWithSeparator(slot + " is not a valid slot.");
                        break;
                    }
                    if(manager.emptySlot(slot)){
                        printWithSeparator(slot + " is empty.");
                        break;
                    }
                    System.out.println("Continuing existing dungeon...");
                    try {
                        manager.loadDungeon(slot);
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
                case "3":
                    handleDeleteSave(manager, in);               
                    break;
                case "4":
                    manager.getRoom(-34).getName();
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
            System.out.println("\n=== Main Menu ===");
            System.out.println("Current Room: " + currentRoom.getRoomNumber() + " (" + currentRoom.getName() + ")");
            System.out.println("Current Position: (" + manager.getCurrentPosition()[0] + ", " + manager.getCurrentPosition()[1] + ")");
            System.out.println(manager.describeDoors(currentRoom));
            System.out.println("1. Move to a new room");
            System.out.println("2. View house grid");
            System.out.println("3. Print room details");
            System.out.println("4. Unlock door");
            System.out.println("5. Manage rooms");
            System.out.println("6. Increase chance of blocked door");
            System.out.println("7. Increase chance of blocked door");
            System.out.println("8. Save and Exit");
            System.out.println("0. Exit without Saving");
            System.out.print("\nEnter your choice: ");

            String choice = in.nextLine();

            switch (choice) {
                case "1":
                    manager.presentRoomOptions();
                    break;
                case "2":
                    printGrid(manager.getHouseGrid());
                    break;
                case "3":
                    printWithSeparator(manager.getCurrentRoom().toString());
                    break;
                case "4":
                    manager.unlockDoor();
                    break;
                case "5":
                    manageRoomsMenu(manager, in);
                    break;
                case "6":
                    manager.increaseBlockedDoorChance();
                    printWithSeparator("Chance of blocked door increased to " + manager.getBlockedDoorChance() + "%");
                    break;
                case "7":
                    manager.decreaseBlockedDoorChance();
                    printWithSeparator("Chance of blocked door decreased to " + manager.getBlockedDoorChance() + "%");
                    break;
                case "8":
                    printWithSeparator("Which slot do you want to save on (1 to 5): ");
                    String slot = in.nextLine();
                    if(!checkSlot(slot)){
                        printWithSeparator("Not a valid slot");
                        break;
                    }
                    if(!manager.emptySlot(slot)){
                        printWithSeparator("Slot is already taken. Do you want to overwrite it? YES to confirm");
                        String opt = in.nextLine();
                        if(!opt.equalsIgnoreCase("yes")){
                            System.out.println("Cancelling...");
                            break;
                        }
                    }
                    System.out.println("Saving dungeon...");
                    try {
                        manager.saveDungeon(slot);
                        System.out.println("Exiting the manager.");
                        System.exit(0);
                    } catch (IOException e) {
                        printWithSeparator("An error occurred while saving the dungeon: " + e.getMessage());
                    }
                    break;
                case "0":
                    printWithSeparator("Exiting without saving.");
                    System.exit(0);
                    break;
                default:
                    printWithSeparator("Invalid choice. Please try again.");
            }
        }
    }

    private static void manageRoomsMenu(DungeonManager manager, Scanner in) {
        while (true){
            System.out.println("\n=== Manage Rooms ===");
            System.out.println("1. Go to room by number");
            System.out.println("2. Go to room by coordinates");
            System.out.println("3. Set room at coordinates");
            System.out.println("4. Remove placed room");
            System.out.println("5. Clear dungeon");
            System.out.println("6. Clear dungeon except for specific room");
            System.out.println("7. Back");
            System.out.print("\nEnter your choice: ");

            String choice = in.nextLine();

            switch (choice) {
                case "1":

                    System.out.print("Enter room number: ");
                    int roomNumber = Integer.parseInt(in.nextLine());
                    int result = manager.goToRoomByNumber(roomNumber);
                    if(result == -1){
                        printWithSeparator("Invalid room number: " + roomNumber);
                    } else if(result == 1){
                        printWithSeparator("Moved to room number: " + roomNumber);
                    } else {
                        printWithSeparator("Room number " + roomNumber + " is not placed in the dungeon yet.");
                    }
                    break;
                case "2":

                    System.out.print("Enter row: ");
                    int row = Integer.parseInt(in.nextLine());
                    System.out.print("Enter column: ");
                    int col = Integer.parseInt(in.nextLine());
                    int moveResult = manager.goToRoomByCoordinates(row, col);
                    if(moveResult == 1){
                        printWithSeparator("Moved to room at (" + row + ", " + col + ").");
                    } else if(moveResult == 0){
                        printWithSeparator("No room exists at (" + row + ", " + col + ").");
                    } else {
                        printWithSeparator("Invalid coordinates: (" + row + ", " + col + ").");
                    }
                    break;
                case "3":

                    System.out.print("Enter room number to place: ");
                    int roomNbr = Integer.valueOf(in.nextLine());
                    Room roomToPlace;
                    try {
                        roomToPlace = manager.getRoom(roomNbr);
                    } catch (NullPointerException e){
                        printWithSeparator("No such room");
                        break;
                    }
                    System.out.print("Enter target row: ");
                    int targetRow = Integer.valueOf(in.nextLine());
                    System.out.print("Enter target column: ");
                    int targetCol = Integer.valueOf(in.nextLine());
                    int placeResult = manager.placeRoom(roomNbr, targetRow, targetCol);
                    if(placeResult == 1){
                        printWithSeparator("Room " + roomNbr + " (" + roomToPlace.getName() + ") has been placed at (" + targetRow + "," + targetCol + ")");
                    }
                    else if (placeResult == 0){
                        printWithSeparator("Error: " + roomNbr + " (" + roomToPlace.getName() + ") is already placed in the house");
                    }
                    else if (placeResult == -1) {
                        printWithSeparator("Error: room already exists at (" + targetRow + "," + targetCol + ")");
                    } 
                    else if (placeResult == -2){
                        printWithSeparator("Error: coordinates (" + targetRow + "," + targetCol + ") are outside of house bounds");
                    }
                    else {
                        printWithSeparator("Error: Unknown error");
                    }
                    break;
                case "4":
                    
                    System.out.print("Enter room number to remove: ");
                    int roomNumbr = Integer.valueOf(in.nextLine());
                    Room room;
                    try {
                        room = manager.getRoom(roomNumbr);
                    } catch (NullPointerException e){
                        printWithSeparator("No such room");
                        break;
                    }
                    printWithSeparator("You want to remove room " + roomNumbr + " (" + room.getName() + "). Are you sure? Yes to confirm");
                    String confirm = in.nextLine().trim();
                    if(!confirm.equalsIgnoreCase("yes")){
                        printWithSeparator("Canceled");
                        break;
                    }
                    System.out.println("Removing room " + roomNumbr + " (" + room.getName() + ")....");
                    boolean removeResult = manager.removeRoomFromHouse(roomNumbr);
                    if(removeResult){
                        printWithSeparator("Room "+ roomNumbr + " (" + room.getName() + ") has been removed from the house");;
                    } else {
                        printWithSeparator("Error: Room is not placed in the house");
                    }
                    break;
                case "5":
                    printWithSeparator("Are you sure you want to clear the entire dungeon? Yes to confirm");
                    String confirmClearAll = in.nextLine().trim();
                    if(!confirmClearAll.equalsIgnoreCase("yes")){
                        printWithSeparator("Canceled");
                        break;
                    }
                    manager.clearDungeon();
                    printWithSeparator("Dungeon has been cleared. Current position set to starting position.");
                    break;
                case "6":
                    System.out.print("Which room do you not want to remove?");
                    int roomToSave = Integer.parseInt(in.nextLine());
                    try {
                        printWithSeparator("Are you sure you want to clear the dungeon, except for room " + roomToSave + " (" + manager.getRoom(roomToSave).getName() +")? Yes to confirm");
                    } catch (NullPointerException e){
                        printWithSeparator("No such room");
                        break;
                    }
                    String confirmClear = in.nextLine().trim();
                    if(!confirmClear.equalsIgnoreCase("yes")){
                        printWithSeparator("Canceled");
                        break;
                    }
                    int clearResult = manager.clearDungeonButOneRoom(roomToSave);
                    if (clearResult == 1){
                        printWithSeparator("Dungeon cleard, except for room " + roomToSave + " (" + manager.getRoom(roomToSave).getName() +")");
                    } else{
                        printWithSeparator("Error: Room " + roomToSave + " (" + manager.getRoom(roomToSave).getName() +") is not placed.");
                    }
                    break;
                case "7":
                    return;  
                case "8":
                    return;     
                case "9":
                    return;
                case "0":
                    return;
                default:
                    printWithSeparator("Invalid choice. Please try again.");
                    break;
            }
        }
    }

    private static void handleDeleteSave(DungeonManager manager, Scanner in) {
        printWithSeparator("Which save slot do you want to delete? (1..5)");
        String slot = in.nextLine().trim();
        if(!checkSlot(slot)){
            printWithSeparator(slot + " is not a valid slot.");
            return;
        }
        if(manager.emptySlot(slot)){
            printWithSeparator(slot + " is already empty.");
            return;
        }

        System.out.print("Are you sure you want to delete " + slot + "? Type YES to confirm: ");
        String confirm = in.nextLine().trim();
        if (!"YES".equalsIgnoreCase(confirm)) {
            System.out.println("Cancelled.");
            return;
        }
        try {
            boolean any = DungeonSaver.deleteSaves("saves", slot);
            if (any) {
                printWithSeparator("Save files deleted.");
            } else {
                printWithSeparator("Deletion failed.");
            }
        } catch (IOException e) {
            printWithSeparator("Error while deleting: " + e.getMessage());
        }
    }

    private static boolean checkSlot(String slot) {
        try{   
            int s = Integer.parseInt(slot);
            if ( s < 1 || s > 5){
                return false;
            }
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }

    private static void printGrid(int[][] grid) {
        int rows = grid.length;
        int cols = grid[0].length;

        System.out.println("\n=====================================");
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
        System.out.println("=====================================\n");
    }

    private static void printDivider(int cols) {
        for (int k = 0; k < cols; k++) {
            System.out.print("----");
        }
        System.out.println("-");
    }

    private static void printWithSeparator(String message) {
        System.out.println("\n=====================================");
        System.out.println(message);
        System.out.println("=====================================\n");
    }
}