package userInterface.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import model.DungeonManager;
import model.Room;
import util.Direction;
import util.DoorState;
import util.MoveOutcome;
import util.Position;
import util.RoomOutcome;

public class SwingController {
    private DungeonManager manager;
    private RoomPanel[][] dungeonGrid;
    private ControlPanel controlPanel;
    private Room currentRoom;
    private RoomPanel currentRoomPanel;
    private RoomPanel selectedRoomPanel;
    
    public SwingController(DungeonManager manager, RoomPanel[][] dungeonGrid, ControlPanel controlPanel) {
        this.manager = manager;
        this.dungeonGrid = dungeonGrid;
        this.controlPanel = controlPanel;
        this.currentRoom = manager.getCurrentRoom();
        this.currentRoomPanel = dungeonGrid[manager.getCurrentPosition().row()][manager.getCurrentPosition().col()];
        currentRoomPanel.setCurrent(true);
        this.selectedRoomPanel = currentRoomPanel;
    }

    public void initialize() {
        addDungeonMapActionListener();
        addMainMenuActionListener();
        addMovementActionListeners();
        addRoomOptionsActionListener();
    }

    private void addDungeonMapActionListener() {
        for (int row = 0; row < dungeonGrid.length; row++) {
            for (int col = 0; col < dungeonGrid[0].length; col++) {
                Position pos = new Position(row, col);
                Room room = manager.getRoomAtPosition(pos);
                RoomPanel roomPanel = dungeonGrid[row][col];
                roomPanel.setClickListener((position, listener) -> {
                    RoomPanel clickedPanel = dungeonGrid[position.row()][position.col()];
                    if (selectedRoomPanel != null) {
                        selectedRoomPanel.setSelected(false);
                    }
                    selectedRoomPanel = clickedPanel;
                    selectedRoomPanel.setSelected(true);
                    RoomSnapshot snapshot = clickedPanel.getSnapshot();
                    controlPanel.setRoomPreviewPanel(snapshot);
                    controlPanel.setRoomOptionsPanel(position.equals(manager.getCurrentPosition()));
                    boolean current = roomPanel.equals(currentRoomPanel);
                    for (int i = 0; i < 4; i++) {
                        controlPanel.setDoorButtonPanel(i, snapshot.doors()[i], current);
                    }
                    controlPanel.setRoomOptionsPanel(current);
                    controlPanel.setInfoPanelText(buildRoomInfoText(room));
                });
            }
        }
    }

    private String buildRoomInfoText(Room room) {
        if (room == null) {
            return "No room at this position.";
        }
        StringBuilder info = new StringBuilder();
        info.append("Description: ").append(room.getDescription()).append("\n\n");
        if(room.getDanger() != null && !room.getDanger().isEmpty()) {
            info.append("Danger: ").append(room.getDanger()).append("\n\n");
        }
        if (room.getContent() != null && !room.getContent().isEmpty()) {
            info.append("Contents: ").append(room.getContent()).append("\n\n");
        }
        if (room.getEffect() != null && !room.getEffect().isEmpty()) {
            info.append("Special Effect: ").append(room.getEffect()).append("\n\n");
        }
        return info.toString();
    }

    private void addMainMenuActionListener() {
        controlPanel.onExit(listener -> {
            System.exit(0);
        });

        controlPanel.onLoad(listener -> {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
            int choice = JOptionPane.showConfirmDialog(controlPanel, spinner, "Load slot", JOptionPane.OK_CANCEL_OPTION);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
            choice = (Integer) spinner.getValue();
            if (!manager.isSlotOccupied(choice)){
                JOptionPane.showMessageDialog(controlPanel, "Slot " + choice + " is empty.", "Load Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                manager.loadDungeon(choice);
                redrawMap();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(controlPanel, "Error loading dungeon: " + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        controlPanel.onSave(listener -> {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
            int choice = JOptionPane.showConfirmDialog(controlPanel, spinner, "Save slot", JOptionPane.OK_CANCEL_OPTION);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
            choice = (Integer) spinner.getValue();
            if (manager.isSlotOccupied(choice) && choice != manager.getCurrentSaveSlot()) {
                int overwriteChoice = JOptionPane.showConfirmDialog(controlPanel, "Slot " + choice + " is already occupied. Overwrite?", "Confirm Overwrite", JOptionPane.YES_NO_OPTION);
                if (overwriteChoice != JOptionPane.YES_OPTION) {
                    return;
                }
            } 
            try {
                manager.saveDungeon(choice);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(controlPanel, "Error saving dungeon: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        controlPanel.onClear(listener -> {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 0, manager.getTotalNumberOfRooms(), 1));
            int choice = JOptionPane.showConfirmDialog(controlPanel, spinner, "Select room to clear. 0 to clear all", JOptionPane.OK_CANCEL_OPTION);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
            choice = (Integer) spinner.getValue();
            if (choice == 0) {
                manager.clearDungeon();
            } else {
                manager.clearDungeon(choice);
            }
            redrawMap();
        });
    }

    private void addMovementActionListeners() {
        for (Direction direction : Direction.values()) {
            controlPanel.onMove(direction, listener -> {
                MoveOutcome move = manager.tryToMove(direction);
                if (move instanceof MoveOutcome.Moved) {  
                    currentRoomPanel.setCurrent(false);
                    Position newPosition = manager.getCurrentPosition();
                    currentRoomPanel = dungeonGrid[newPosition.row()][newPosition.col()];
                    currentRoomPanel.setCurrent(true);
                    Room room = manager.getCurrentRoom();
                    controlPanel.setInfoPanelText(buildRoomInfoText(room));
                    redrawMap();
                }
                else if (move instanceof MoveOutcome.Blocked blocked) {
                    JOptionPane.showMessageDialog(controlPanel, "Could not move: " + blocked.reason(), "Movement Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } else if (move instanceof MoveOutcome.NeedsPlacement needsPlacement) {
                    List<Room> options = needsPlacement.options();
                    List<JPanel> optionPanels = new ArrayList<>();
                    AtomicInteger chosenRoom = new AtomicInteger(-1);
                    for (int i = 0; i < options.size(); i++) {
                        Room option = options.get(i);
                        RoomSnapshot snapshot = new RoomSnapshot(true, option.getRoomNumber(), option.getDescription(), false, new DoorState[] {DoorState.NONE, DoorState.NONE, DoorState.NONE, DoorState.NONE}, new int[] {0,0,0,0});
                        RoomPanel optionPanel = new RoomPanel(new Position(-1, -1));
                        optionPanel.setPreferredSize(new Dimension(200, 160));
                        optionPanel.setSnapshot(snapshot);
                        optionPanel.setClickListener((position, e) -> {
                            chosenRoom.set(option.getRoomNumber());
                        });
                        optionPanels.add(optionPanel);
                    }
                    JPanel optionsContainer = new JPanel();
                    for (JPanel panel : optionPanels) {
                        optionsContainer.add(panel);
                    }
                    int choice = JOptionPane.showConfirmDialog(controlPanel, optionsContainer, "Select a room to place", JOptionPane.OK_CANCEL_OPTION);
                    if (choice != JOptionPane.OK_OPTION) {
                        return;
                    }
                    if (chosenRoom.get() == -1) {
                        JOptionPane.showMessageDialog(controlPanel, "No room selected.", "Movement Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Room newRoom = manager.getRoom(chosenRoom.get());
                    Position position = manager.getCurrentPosition().move(direction);
                    manager.placeRoom(newRoom, position, direction, true);
                    dungeonGrid[position.row()][position.col()].setSnapshot(manager.getRoomSnapshot(position));
                    currentRoomPanel.setCurrent(false);
                    currentRoomPanel = dungeonGrid[position.row()][position.col()];
                    currentRoomPanel.setCurrent(true);
                    controlPanel.setInfoPanelText(buildRoomInfoText(newRoom));
                    redrawMap();
                }
            });
        }
    }

    private void addRoomOptionsActionListener() {
        controlPanel.onPlaceRoom(listener -> {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, manager.getTotalNumberOfRooms()+1, 1));
            int choice = JOptionPane.showConfirmDialog(controlPanel, spinner, "Select roomnumber of room to place", JOptionPane.OK_CANCEL_OPTION);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
            JPanel panel = new JPanel();
            JSpinner directionSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 3, 1));
            JTextArea directionLabel = new JTextArea("North = 0\nEast = 1\nSouth = 2\nWest = 3");
            directionLabel.setEditable(false);
            directionLabel.setOpaque(false);
            panel.add(directionLabel);
            panel.add(directionSpinner);
            int directionValue = JOptionPane.showConfirmDialog(controlPanel, panel, "Select direction the first door should face", JOptionPane.OK_CANCEL_OPTION);
            if (directionValue != JOptionPane.OK_OPTION) {
                return;
            }
            choice = (Integer) spinner.getValue();
            Position selectedPosition = selectedRoomPanel.getPosition();
            directionValue = (Integer) directionSpinner.getValue();
            Direction firstDoorDirection = Direction.values()[directionValue].opposite();
            try {
                RoomOutcome outcome = manager.forcePlaceRoom(choice, selectedPosition, firstDoorDirection);
                if (outcome instanceof RoomOutcome.Placed) {
                    RoomSnapshot snapshot = manager.getRoomSnapshot(selectedPosition);
                    selectedRoomPanel.setSnapshot(snapshot);
                    redrawMap();
                    JOptionPane.showMessageDialog(controlPanel, "Room placed successfully.", "Place Room", JOptionPane.INFORMATION_MESSAGE);
                } else if (outcome instanceof RoomOutcome.Failed failed) {
                    JOptionPane.showMessageDialog(controlPanel, "Room could not be placed: " + failed.reason(), "Place Room Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    JOptionPane.showMessageDialog(controlPanel, "Room could not be placed: Unknown reason.", "Place Room Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(controlPanel, "Error placing room: " + e.getMessage(), "Place Room Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        controlPanel.onRemoveRoom(listener -> {
            Position selectedPosition = selectedRoomPanel.getPosition();
            try {
                Room room = manager.getRoomAtPosition(selectedPosition);
                if (room == null) {
                    JOptionPane.showMessageDialog(controlPanel, "No room to remove at this position.", "Remove Room Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int roomNumber = room.getRoomNumber();
                if (roomNumber == 1) {
                    JOptionPane.showMessageDialog(controlPanel, "Cannot remove the starting room.", "Remove Room Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (roomNumber == manager.getCurrentRoom().getRoomNumber()) {
                    JOptionPane.showMessageDialog(controlPanel, "Cannot remove the current room.", "Remove Room Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (roomNumber == 0) {
                    JOptionPane.showMessageDialog(controlPanel, "No room to remove at this position.", "Remove Room Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                RoomOutcome outcome = manager.removeRoomFromHouse(roomNumber);
                if (outcome instanceof RoomOutcome.Removed) {
                    selectedRoomPanel.setSnapshot(RoomSnapshot.emptySnapshot());
                    redrawMap();
                    JOptionPane.showMessageDialog(controlPanel, "Room removed successfully.", "Remove Room", JOptionPane.INFORMATION_MESSAGE);
                } else if (outcome instanceof RoomOutcome.Failed failed) {
                    JOptionPane.showMessageDialog(controlPanel, "Room could not be removed: " + failed.reason(), "Remove Room Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    JOptionPane.showMessageDialog(controlPanel, "Room could not be removed: Unknown reason.", "Remove Room Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(controlPanel, "Error removing room: " + e.getMessage(), "Remove Room Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        controlPanel.onGoToRoom(listener -> {
            Position selectedPosition = selectedRoomPanel.getPosition();
            try {
                Room room = manager.getRoomAtPosition(selectedPosition);
                if (room == null) {
                    JOptionPane.showMessageDialog(controlPanel, "No room at this position to go to.", "Go To Room Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int roomNumber = room.getRoomNumber();
                MoveOutcome move = manager.goToRoomByRoomNumber(roomNumber);
                if (move instanceof MoveOutcome.Moved) {  
                    currentRoomPanel.setCurrent(false);
                    currentRoomPanel = selectedRoomPanel;
                    currentRoomPanel.setCurrent(true);
                    controlPanel.setInfoPanelText(buildRoomInfoText(room));
                    redrawMap();
                }
                else if (move instanceof MoveOutcome.Blocked blocked) {
                    JOptionPane.showMessageDialog(controlPanel, "Could not go to room: " + blocked.reason(), "Go To Room Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    JOptionPane.showMessageDialog(controlPanel, "Could not go to room: Unknown reason.", "Go To Room Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(controlPanel, "Error going to room: " + e.getMessage(), "Go To Room Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void redrawMap(){
        for (int row = 0; row < dungeonGrid.length; row++) {
            for (int col = 0; col < dungeonGrid[0].length; col++) {
                dungeonGrid[row][col].repaint();
            }
        }
    }
}
