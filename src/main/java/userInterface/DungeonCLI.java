package userInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import model.DungeonManager;
import model.Room;
import util.Direction;
import util.MoveOutcome;

public class DungeonCLI {
    
    public static void main(String[] args) {

        boolean validChoice = false;
        Scanner in = new Scanner(System.in);
        DungeonManager manager = new DungeonManager();

        while(!validChoice){

            System.out.println("\n=== Welome to the Dungeon Manager ===");
            System.out.println("1. Continue Dungeon");
            System.out.println("2. New Dungeon");
            System.out.println("3. Delete a save");
            System.out.print("Enter your choice: ");
            String choice = in.nextLine();
            switch (choice) {
                case "1":
                    
                case "2":

                    break;
                case "3":
           
                    break;
                case "4":

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
            System.out.println("Current Position: " + manager.getCurrentPosition());
            System.out.println(manager.describeDoors(currentRoom));
            System.out.println("1. Move to a new room");
            System.out.println("2. View house grid");
            System.out.println("3. Print room details");
            System.out.println("4. Unlock door");
            System.out.println("5. Manage rooms");
            System.out.println("6. Manage drafting");
            System.out.println("7. Show all miniatures in the house");
            System.out.println("8. Save and Exit");
            System.out.println("0. Exit without Saving");
            System.out.print("\nEnter your choice: ");

            String choice = in.nextLine();

            switch (choice) {
                case "1":
                    break;
                case "2":
                    printGrid(manager.getHouseGrid());
                    break;
                case "3":
                    printWithSeparator(manager.getCurrentRoom().toString());
                    break;
                case "4":
                    break;
                case "5":
                    manageRoomsMenu(manager, in);
                    break;
                case "6":
                    manageDraftingMenu(manager, in);
                    break;
                case "7":
                    ArrayList<String> miniatures = manager.getAllMiniaturesInHouse();
                    if(miniatures.isEmpty()){
                        printWithSeparator("No miniatures placed in the house.");
                    } else {
                        StringBuilder sb = new StringBuilder("Miniatures in the house:\n");
                        for(String mini : miniatures){
                            sb.append("- ").append(mini).append("\n");
                        }
                        printWithSeparator(sb.toString());
                    }
                    break;
                case "8":
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

    private static void manageDraftingMenu(DungeonManager manager, Scanner in) {
        System.out.println("\n=== Manage Drafting ===");
        System.out.println("1. Increase chance of blocked door");
        System.out.println("2. Decrease chance of blocked door");
        System.out.println("3. Set drafting to 3 rooms");
        System.out.println("4. Set drafting to 5 rooms");
        System.out.println("5. Back to main menu");
        System.out.print("\nEnter your choice: ");

        String choice = in.nextLine();

        switch (choice) {

            case "1":
                manager.increaseBlockedDoorChance();
                printWithSeparator("Chance of blocked door increased to " + manager.getBlockedDoorChance() + "%");
                break;
            case "2":
                manager.decreaseBlockedDoorChance();
                printWithSeparator("Chance of blocked door decreased to " + manager.getBlockedDoorChance() + "% ");
                break;
            case "3":
                manager.setRoomAmountToThree();
                printWithSeparator("Drafting set to 3 rooms.");
                break;
            case "4":
                manager.setRoomAmountToFive();
                printWithSeparator("Drafting set to 5 rooms.");
                break;
            case "5":
                return; 
            case "6":
                return;
            case "7":
                return;
            case "8":
                return;
            case "9":
                return;    
            default:
                printWithSeparator("Invalid choice. Please try again.");
                break;
        }
    }

    private static void manageRoomsMenu(DungeonManager manager, Scanner in) {
        System.out.println("\n=== Manage Rooms ===");
        System.out.println("1. Go to room by number");
        System.out.println("2. Set room at coordinates");
        System.out.println("3. Remove placed room");
        System.out.println("4. Clear dungeon");
        System.out.println("5. Clear dungeon except for specific room");
        System.out.println("6. Update room details");
        System.out.println("7. Back to main menu");
        System.out.print("\nEnter your choice: ");

        String choice = in.nextLine();

        int roomNumber;
        boolean validInput = false;

        switch (choice) {
            case "1":
                System.out.print("Enter room number: ");
                String input = in.nextLine();
                while(!validInput){
                    if(input.equalsIgnoreCase("c")){
                        printWithSeparator("Canceled");
                        break;
                    }
                    try {
                        roomNumber = Integer.parseInt(input);
                        validInput = true;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid room number. Please enter a valid integer, or c to cancel: ");
                    }
                }
                MoveOutcome outcome = manager.goToRoomByRoomNumber(roomNumber);
                if(outcome instanceof MoveOutcome.Blocked){
                    printWithSeparator("Invalid room number: " + roomNumber);
                } else if(outcome instanceof MoveOutcome.Moved){
                    printWithSeparator("Moved to room number: " + roomNumber);
                } else {
                    printWithSeparator("Room number " + roomNumber + " is not placed in the dungeon yet.");
                }
                break;
            case "2":

                System.out.print("Enter room number to place, or c to cancel: ");
                validInput = false;
                input = in.nextLine();
                while(!validInput){
                    if(input.equalsIgnoreCase("c")){
                        printWithSeparator("Canceled");
                        break;
                    }
                    try {
                        roomNumber = Integer.parseInt(input);
                        validInput = true;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid room number. Please enter a valid integer, or c to cancel: ");
                    }
                }
                Room roomToPlace;
                try {
                    roomToPlace = manager.getRoom(roomNumber);
                } catch (NullPointerException e){
                    printWithSeparator("No such room");
                    break;
                }
                System.out.print("Enter target row: ");
                int targetRow;
                try {
                    targetRow = Integer.parseInt(in.nextLine());
                } catch (NumberFormatException e) {
                    printWithSeparator("Invalid row number.");
                    break;
                }
                System.out.print("Enter target column: ");
                int targetCol;
                try {
                    targetCol = Integer.parseInt(in.nextLine());
                } catch (NumberFormatException e) {
                    printWithSeparator("Invalid column number.");
                    break;
                }
                System.out.print("Enter direction for door (N/S/E/W) or leave blank for random: ");
                String direction = in.nextLine().trim();
                Direction dir = null;
                if(!direction.isEmpty()){
                    try {
                        dir = Direction.fromChar(direction.charAt(0));
                    } catch (IllegalArgumentException e){
                        printWithSeparator("Invalid direction: " + direction);
                        break;
                    }
                }
                int placeResult = manager.placeRoom(roomNbr, targetRow, targetCol, dir);
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
                else if (placeResult == -3){
                    printWithSeparator("Error: door would lead out of bounds");
                }
                else {
                    printWithSeparator("Error: Unknown error");
                }
                break;
            case "3":
                
                System.out.print("Enter room number to remove: ");
                int roomNumbr;
                try {
                    roomNumbr = Integer.parseInt(in.nextLine());
                } catch (NumberFormatException e) {
                    printWithSeparator("Invalid room number.");
                    break;
                }
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
            case "4":
                printWithSeparator("Are you sure you want to clear the entire dungeon? Yes to confirm");
                String confirmClearAll = in.nextLine().trim();
                if(!confirmClearAll.equalsIgnoreCase("yes")){
                    printWithSeparator("Canceled");
                    break;
                }
                manager.clearDungeon();
                printWithSeparator("Dungeon has been cleared. Current position set to starting position.");
                break;
            case "5":
                System.out.print("Which room do you not want to remove?");
                int roomToSave;
                try {
                    roomToSave = Integer.parseInt(in.nextLine());
                } catch (NumberFormatException e) {
                    printWithSeparator("Invalid room number.");
                    break;
                }
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
            case "6":
                System.out.print("Are you sure you want to update the details of every room in the dungeon? Yes to confirm: ");
                String confirmUpdate = in.nextLine().trim();
                if(!confirmUpdate.equalsIgnoreCase("yes")){
                    printWithSeparator("Canceled");
                    break;
                }
                try {
                    manager.updateAllRoomDetails();
                } catch (IOException e) {
                    printWithSeparator("An error occurred while updating room details: " + e.getMessage());
                    break;
                }
                printWithSeparator("All room details have been updated.");
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

        System.out.print("Are you sure you want to delete save " + slot + "? Type YES to confirm: ");
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
