package userInterface.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;


import model.DungeonManager;
import model.Room;
import util.Direction;
import util.DoorState;
import util.MoveOutcome;
import util.Position;
import util.RoomOutcome;
import util.UnlockOutcome;

public class SwingController {
    private DungeonManager manager;
    private RoomPanel[][] dungeonGrid;
    private ControlPanel controlPanel;
    private RoomPanel currentRoomPanel;
    private RoomPanel selectedRoomPanel;
    
    public SwingController(DungeonManager manager, RoomPanel[][] dungeonGrid, ControlPanel controlPanel) {
        this.manager = manager;
        this.dungeonGrid = dungeonGrid;
        this.controlPanel = controlPanel;
        this.currentRoomPanel = getPanelAt(manager.getCurrentPosition());
        currentRoomPanel.setCurrent(true);
        this.selectedRoomPanel = currentRoomPanel;
    }

    public void initialize() {
        addDungeonMapActionListener();
        addMainMenuActionListener();
        addMovementActionListeners();
        addRoomOptionsActionListener();
        addExtrasActionListener();
    }

    private void addDungeonMapActionListener() {
        for (int row = 0; row < dungeonGrid.length; row++) {
            for (int col = 0; col < dungeonGrid[0].length; col++) {
                Position pos = new Position(row, col);
                RoomPanel roomPanel = dungeonGrid[row][col];
                roomPanel.setClickListener((position, listener) -> {
                    Room room = manager.getRoomAtPosition(pos);
                    RoomPanel clickedPanel = getPanelAt(position);
                    if (selectedRoomPanel != null) {
                        selectedRoomPanel.setSelected(false);
                    }
                    selectedRoomPanel = clickedPanel;
                    selectedRoomPanel.setSelected(true);
                    RoomSnapshot snapshot = clickedPanel.getSnapshot();
                    controlPanel.setRoomPreviewPanel(snapshot);
                    controlPanel.setRoomOptionsPanel(position.equals(manager.getCurrentPosition()));
                    boolean current = clickedPanel.equals(currentRoomPanel);
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
                Position currentPosition = manager.getCurrentPosition();
                Room currentRoom = manager.getCurrentRoom();
                RoomSnapshot currentSnapshot = manager.getRoomSnapshot(currentPosition);
                RoomPanel currentPanel = getPanelAt(currentPosition);
                setAsCurrent(currentPanel, currentRoom, currentSnapshot);
                setAsSelected(currentPanel, currentRoom, currentSnapshot);
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
            Integer currentSaveSlot = manager.getCurrentSaveSlot();
            if (currentSaveSlot == null) {
                currentSaveSlot = -1;
            }
            if (manager.isSlotOccupied(choice) && choice != currentSaveSlot) {
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
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, manager.getTotalNumberOfRooms(), 1));
            int choice = JOptionPane.showConfirmDialog(controlPanel, spinner, "Select room to clear. 0 to clear all", JOptionPane.OK_CANCEL_OPTION);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
            choice = (Integer) spinner.getValue();
            Position position;
            RoomSnapshot snapshot;
            if (choice == 0) {
                manager.clearDungeon();
                position = manager.getStartingPosition();
                snapshot = manager.getRoomSnapshot(position);
            } else {
                manager.clearDungeon(choice);
                position = manager.getCurrentPosition();
                snapshot = manager.getRoomSnapshot(position);
            }
            RoomPanel panel = getPanelAt(position);
            setAsCurrent(panel, manager.getRoomAtPosition(position), snapshot);
            setAsSelected(panel, manager.getRoomAtPosition(position), snapshot);
            redrawMap();
        });

        controlPanel.onRemoveSave(listener -> {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
            int choice = JOptionPane.showConfirmDialog(controlPanel, spinner, "Choose save slot to remove", JOptionPane.OK_CANCEL_OPTION);
            if (choice != JOptionPane.OK_OPTION) {
                return;
            }
            choice = (Integer) spinner.getValue();
            if (!manager.isSlotOccupied(choice)){
                JOptionPane.showMessageDialog(controlPanel, "Slot " + choice + " is already empty.", "Remove Save Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(controlPanel, "Are you sure you want to delete save slot " + choice + "? \nThis action cannot be undone.", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                manager.deleteSaveFile(choice);
                JOptionPane.showMessageDialog(controlPanel, "Save slot " + choice + " deleted successfully.", "Remove Save", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(controlPanel, "Error deleting save file: " + e.getMessage(), "Remove Save Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void addMovementActionListeners() {
        for (Direction direction : Direction.values()) {
            controlPanel.onMove(direction, listener -> {
                MoveOutcome move = manager.tryToMove(direction);
                if (move instanceof MoveOutcome.Moved) {
                    Position newPosition = manager.getCurrentPosition(); ;
                    Room room = manager.getCurrentRoom();
                    RoomSnapshot snapshot = manager.getRoomSnapshot(newPosition);
                    RoomPanel targetPanel = getPanelAt(newPosition);
                    setAsCurrent(targetPanel, room, snapshot);
                    setAsSelected(targetPanel, room, snapshot);
                    redrawMap();
                }
                else if (move instanceof MoveOutcome.Blocked blocked) {
                    JOptionPane.showMessageDialog(controlPanel, "Could not move: " + blocked.reason(), "Movement Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } else if (move instanceof MoveOutcome.NeedsPlacement needsPlacement) {
                    Room room = showDraftRoomDialog(needsPlacement.options());
                    if (room == null) {
                        return;
                    }
                    Position newPosition = needsPlacement.newPosition();
                    manager.placeRoom(room, newPosition, direction, true);
                    RoomPanel selectedRoomPanel = getPanelAt(newPosition);
                    RoomSnapshot snapshot = manager.getRoomSnapshot(newPosition);
                    setAsCurrent(selectedRoomPanel, room, snapshot);
                    setAsSelected(selectedRoomPanel, room, snapshot);
                    redrawMap();
                }
            });

            controlPanel.onUnlock(direction, listener -> {
                try {
                    UnlockOutcome outcome = manager.unlockDoor(direction);
                    if (outcome == UnlockOutcome.UNLOCKED){
                        controlPanel.setDoorButtonPanel(direction.getIndex(), DoorState.OPEN, true);
                        redrawMap();
                        JOptionPane.showMessageDialog(controlPanel, "Door unlocked successfully.", "Unlock Door", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if (outcome == UnlockOutcome.ALREADY_UNLOCKED){
                        JOptionPane.showMessageDialog(controlPanel, "Door is already unlocked.", "Unlock Door", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if (outcome == UnlockOutcome.NO_DOOR){
                        JOptionPane.showMessageDialog(controlPanel, "No door in that direction to unlock.", "Unlock Door Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else if (outcome == UnlockOutcome.DOOR_BLOCKED){
                        JOptionPane.showMessageDialog(controlPanel, "Door is blocked and cannot be unlocked.", "Unlock Door Error", JOptionPane.ERROR_MESSAGE);
                    }
                    else {
                        JOptionPane.showMessageDialog(controlPanel, "Could not unlock door: Unknown reason.", "Unlock Door Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(controlPanel, "Error unlocking door: " + e.getMessage(), "Unlock Door Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }

    private Room showDraftRoomDialog(List<Room> options) {
        DefaultListModel<Room> model = new DefaultListModel<>();
        for (Room room : options) {
            model.addElement(room);
        }

        JList<Room> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(model.size());
        list.setCellRenderer((jList, value, index, isSelected, cellHasFocus) -> {
            String text = "Room " + value.getRoomNumber() + "- " + value.getName() + "(" + value.getDoorCount() + " doors)";
            JLabel label = new JLabel(text);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(6,8,6,8));
            if (isSelected) {
                label.setBackground(jList.getSelectionBackground());
                label.setForeground(jList.getSelectionForeground());
            } else {
                label.setBackground(jList.getBackground());
                label.setForeground(jList.getForeground());
            }
            return label;
        });

        if (model.getSize() > 0) {
            list.setSelectedIndex(0);
        }

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        JTextArea preview = new JTextArea(8, 24);
        preview.setEditable(false);
        preview.setLineWrap(true);
        preview.setWrapStyleWord(true);
        preview.setBorder(BorderFactory.createTitledBorder("Room Info"));
        JScrollPane previewScrollPane = new JScrollPane(preview);
        previewScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        updatePreview(preview, list.getSelectedValue());

        list.addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting()) {
                Room selectedRoom = list.getSelectedValue();
                updatePreview(preview, selectedRoom);
            }
        });

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        JButton removeButton = new JButton("Remove Room from pool");
        removeButton.setToolTipText("Remove the selected room from the global pool. The room can never be drafted again.");
        okButton.setEnabled(model.getSize() > 0 && list.getSelectedValue() != null);

        list.addListSelectionListener(e -> {
            okButton.setEnabled(model.getSize() > 0 && list.getSelectedValue() != null);
        });
        
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(scrollPane, BorderLayout.WEST);
        centerPanel.add(previewScrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(removeButton);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        JOptionPane optionPane = new JOptionPane(contentPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[] {okButton, cancelButton}, okButton);
        JDialog dialog = optionPane.createDialog(controlPanel, "Draft a Room to Place");
        dialog.setModal(true);

        removeButton.addActionListener(listener -> {
            Room selectedRoom = list.getSelectedValue();
            if (selectedRoom == null) {
                JOptionPane.showMessageDialog(dialog, "Select a room first.", "No selection", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int result = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to remove " + selectedRoom.getName() + " from the pool?\nThis action cannot be undone.", "Confirm Remove Room", JOptionPane.YES_NO_OPTION);

            if (result != JOptionPane.YES_OPTION) {
                return;
            }

            RoomOutcome outcome = manager.removeRoomFromPool(selectedRoom.getRoomNumber());
            if (outcome instanceof RoomOutcome.Failed failed) {
                JOptionPane.showMessageDialog(dialog, "Could not remove room: " + failed.reason(), "Remove Room Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int selectedIndex = list.getSelectedIndex();
            model.removeElement(selectedRoom);

            if (model.getSize() > 0) {
                if (selectedIndex >= model.getSize()) {
                    selectedIndex = model.getSize() - 1;
                }
                list.setSelectedIndex(selectedIndex);
                list.ensureIndexIsVisible(selectedIndex);
            } else {
                preview.setText("No draftable rooms available. \nPress Cancel to exit.");
                okButton.setEnabled(false);
                return;
            }
        });

        okButton.addActionListener(listener -> {
            optionPane.setValue(okButton);
        });

        cancelButton.addActionListener(listener -> {
            optionPane.setValue(cancelButton);
        });

        dialog.setVisible(true);
        dialog.dispose();

        Object selectedValue = optionPane.getValue();
        if (selectedValue == okButton) {
            return list.getSelectedValue();
        } else {
            return null;
        }
    }

    private void updatePreview(JTextArea preview, Room selectedRoom) {
        if (selectedRoom == null) {
            preview.setText("No room selected.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Room ").append(selectedRoom.getRoomNumber()).append(": ").append(selectedRoom.getName()).append("\n");
        sb.append("Doors: ").append(selectedRoom.getDoorCount()).append("\n\n");
        if (selectedRoom.getDescription() != null) {
            sb.append("Description:\n").append(selectedRoom.getDescription());
        }
        preview.setText(sb.toString());
        preview.setCaretPosition(0);
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
                    setAsSelected(selectedRoomPanel, manager.getRoom(choice), snapshot);
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
                    RoomSnapshot snapshot = manager.getRoomSnapshot(selectedPosition);
                    setAsCurrent(selectedRoomPanel, room, snapshot);
                    setAsSelected(selectedRoomPanel, room, snapshot);
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

    private void addExtrasActionListener() {
        controlPanel.onSetDraftFive(listener -> {
            manager.setRoomDraftAmountToFive();
            JOptionPane.showMessageDialog(controlPanel, "Room draft amount set to 5.", "Draft Options", JOptionPane.INFORMATION_MESSAGE);
        });

        controlPanel.onSetDraftThree(listener -> {
            manager.setRoomDraftAmountToThree();
            JOptionPane.showMessageDialog(controlPanel, "Room draft amount set to 3.", "Draft Options", JOptionPane.INFORMATION_MESSAGE);
        });

        controlPanel.onIncreaseBlockedChance(listener -> {
            manager.increaseBlockedDoorChance();
            JOptionPane.showMessageDialog(controlPanel, "Blocked door chance increased to " + manager.getBlockedDoorChance() + "%.", "Draft Options", JOptionPane.INFORMATION_MESSAGE);
        });

        controlPanel.onDecreaseBlockedChance(listener -> {
            manager.decreaseBlockedDoorChance();
            JOptionPane.showMessageDialog(controlPanel, "Blocked door chance decreased to " + manager.getBlockedDoorChance() + "%.", "Draft Options", JOptionPane.INFORMATION_MESSAGE);
        });

        controlPanel.onShowMiniatures(listener -> {
            HashMap<Room, String> miniatures = manager.getAllMiniaturesInHouse();
            StringBuilder sb = new StringBuilder();
            for (Room room: miniatures.keySet()) {
                sb.append(room.getRoomNumber() + ": " + miniatures.get(room)).append("\n");
            }
            controlPanel.setInfoPanelText(sb.toString());
        });
    }

    private void setAsCurrent(RoomPanel targetPanel, Room room, RoomSnapshot snapshot) {  
                    currentRoomPanel.setCurrent(false);
                    currentRoomPanel = targetPanel;
                    currentRoomPanel.setCurrent(true);
                    controlPanel.setInfoPanelText(buildRoomInfoText(room));
    }

    private void setAsSelected(RoomPanel targetPanel, Room room, RoomSnapshot snapshot) {  
                    selectedRoomPanel.setSelected(false);
                    selectedRoomPanel = targetPanel;
                    selectedRoomPanel.setSelected(true);

                    controlPanel.setRoomPreviewPanel(snapshot);
                    boolean current = targetPanel.equals(currentRoomPanel);
                    for (int i = 0; i < 4; i++) {
                        controlPanel.setDoorButtonPanel(i, snapshot.doors()[i], current);
                    }
                    controlPanel.setRoomOptionsPanel(current);
                    controlPanel.setInfoPanelText(buildRoomInfoText(room));
    }

    private void redrawMap(){
        for (int row = 0; row < dungeonGrid.length; row++) {
            for (int col = 0; col < dungeonGrid[0].length; col++) {
                dungeonGrid[row][col].setSnapshot(manager.getRoomSnapshot(new Position(row, col)));
                dungeonGrid[row][col].repaint();
            }
        }
    }
    
    private RoomPanel getPanelAt(Position position) {
        return dungeonGrid[position.row()][position.col()];
    }
}
