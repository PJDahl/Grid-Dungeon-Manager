package userInterface.swing;

import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import util.Direction;
import util.DoorState;

public class ControlPanel extends JPanel {
    RoomPreviewPanel roomPanel;
    JButton saveButton = new JButton("Save");
    JButton loadButton = new JButton("Load");
    JButton clearButton = new JButton("Clear Dungeon");
    JButton removeSaveButton = new JButton("Remove a Save File");
    JButton exitButton = new JButton("Exit");

    JPanel[] buttonPanels = new JPanel[4]; // North(0), East(1), South(2), West(3)
    JButton northButton = new JButton("Go North");
    JButton northUnlockButton = new JButton("Unlock Room");
    JButton eastButton = new JButton("Go East");
    JButton eastUnlockButton = new JButton("Unlock Room");
    JButton southButton = new JButton("Go South");
    JButton southUnlockButton = new JButton("Unlock Room");
    JButton westButton = new JButton("Go West");
    JButton westUnlockButton = new JButton("Unlock Room");

    JPanel optionsPanel = new JPanel();
    JButton goToButton = new JButton("Go To Room");
    JButton removeButton = new JButton("Remove Room");
    JButton placeButton = new JButton("Place Room");
    JButton miniatureButton = new JButton("Show Miniatures");
    JButton manageDraftButton = new JButton("Manage Drafting");

    JTextArea infoPanel = new JTextArea();

    JPopupMenu draftMenu = new JPopupMenu();
    JMenuItem draftThreeButton = new JMenuItem("Set Draft To 3");
    JMenuItem draftFiveButton = new JMenuItem("Set Draft To 5");
    JMenuItem increaseBlockButton = new JMenuItem("Increase Blocked Chance");
    JMenuItem decreaseBlockButton = new JMenuItem("Decrease Blocked Chance");
    
    
    public ControlPanel(RoomPreviewPanel roomPanel) {
        this.roomPanel = roomPanel;
        
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Dungeon Controls", TitledBorder.CENTER, TitledBorder.TOP, new Font("Georgia", Font.BOLD, 14)));
        setLayout(new BorderLayout());
        setFont(new Font("Georgia", Font.PLAIN, 14));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2,1));
        add(topPanel, BorderLayout.NORTH);

        JPanel movementPanel = createMovementPanel();
        add(movementPanel, BorderLayout.CENTER);

        JScrollPane scrollPane = createInfoPanel();
        JPanel middlemanPanel = createBottomButtonPanel();

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(middlemanPanel, BorderLayout.NORTH);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        createDraftingMenu();

        JPanel mainMenu = createMainMenuPanel();
        topPanel.add(mainMenu);

        optionsPanel.setLayout(new CardLayout());
        JPanel roomOptionsCurrent = createRoomOptionsPanel(true);
        optionsPanel.add(roomOptionsCurrent, "CURRENT");
        JPanel roomOptionsNotCurrent = createRoomOptionsPanel(false);
        optionsPanel.add(roomOptionsNotCurrent, "NOT_CURRENT");

        setButtonFonts();
    }


    /*
     * Setup methods
     */
    private void setButtonFonts() {
        saveButton.setFont(getFont());
        loadButton.setFont(getFont());
        clearButton.setFont(getFont());
        removeSaveButton.setFont(getFont());
        exitButton.setFont(getFont());

        northButton.setFont(getFont());
        eastButton.setFont(getFont());
        southButton.setFont(getFont());
        westButton.setFont(getFont());

        northUnlockButton.setFont(getFont());
        eastUnlockButton.setFont(getFont());
        southUnlockButton.setFont(getFont());
        westUnlockButton.setFont(getFont());

        goToButton.setFont(getFont());
        removeButton.setFont(getFont());
        placeButton.setFont(getFont());
        miniatureButton.setFont(getFont());
        manageDraftButton.setFont(getFont());

        draftThreeButton.setFont(getFont());
        draftFiveButton.setFont(getFont());
        increaseBlockButton.setFont(getFont());
        decreaseBlockButton.setFont(getFont());
    }

    private JPanel createMovementPanel() {
        JPanel movementPanel = new JPanel();
        movementPanel.setLayout(new GridBagLayout());
        Insets pad = new Insets(6, 6, 6, 6);
        GridBagConstraints c = new GridBagConstraints();

        JPanel northButtonPanel = new JPanel();
        northButtonPanel.setLayout(new CardLayout());
        northButtonPanel.add(northButton, "GO");
        northButtonPanel.add(northUnlockButton, "UNLOCK");
        JPanel northNothing = new JPanel();
        northNothing.setPreferredSize(new Dimension(northButton.getPreferredSize().width+30, northButton.getPreferredSize().height));
        northButtonPanel.add(northNothing, "NONE");
        buttonPanels[0] = northButtonPanel;

        JPanel eastButtonPanel = new JPanel();
        eastButtonPanel.setLayout(new CardLayout());
        eastButtonPanel.add(eastButton, "GO");
        eastButtonPanel.add(eastUnlockButton, "UNLOCK");
        eastButtonPanel.add(new JPanel(), "NONE");
        buttonPanels[1] = eastButtonPanel;

        JPanel southButtonPanel = new JPanel();
        southButtonPanel.setLayout(new CardLayout());
        southButtonPanel.add(southButton, "GO");
        southButtonPanel.add(southUnlockButton, "UNLOCK");
        JPanel southNothing = new JPanel();
        southNothing.setPreferredSize(new Dimension(southButton.getPreferredSize().width+30, southButton.getPreferredSize().height));
        southButtonPanel.add(southNothing, "NONE");
        buttonPanels[2] = southButtonPanel;

        JPanel westButtonPanel = new JPanel();
        westButtonPanel.setLayout(new CardLayout());
        westButtonPanel.add(westButton, "GO");
        westButtonPanel.add(westUnlockButton, "UNLOCK");
        westButtonPanel.add(new JPanel(), "NONE");
        buttonPanels[3] = westButtonPanel;

        c.fill = GridBagConstraints.NONE;
        c.insets = pad;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 1;
        c.gridy = 0;
        movementPanel.add(buttonPanels[0], c);

        c = new GridBagConstraints();

        c.fill = GridBagConstraints.NONE;
        c.insets = pad;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 1;
        movementPanel.add(buttonPanels[3], c);

        c = new GridBagConstraints();

        c.fill = GridBagConstraints.NONE;
        c.insets = pad;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 1;
        c.gridy = 1;
        movementPanel.add(roomPanel, c);

        c = new GridBagConstraints();

        c.fill = GridBagConstraints.NONE;
        c.insets = pad;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 2;
        c.gridy = 1;
        movementPanel.add(buttonPanels[1], c);

        c = new GridBagConstraints();

        c.fill = GridBagConstraints.NONE;
        c.insets = pad;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 1;
        c.gridy = 2;
        movementPanel.add(buttonPanels[2], c);

        return movementPanel;
    }

    private JPanel createRoomOptionsPanel(boolean current) {
        JPanel roomOptionsPanel = new JPanel();
        if (current) {
            return roomOptionsPanel;
        } else {
            roomOptionsPanel.add(placeButton);
            roomOptionsPanel.add(removeButton);
            roomOptionsPanel.add(goToButton);
            return roomOptionsPanel;
        }
    }

    private JPanel createMainMenuPanel() {
        JPanel mainMenu = new JPanel();
        mainMenu.setLayout(new FlowLayout(FlowLayout.CENTER));
        mainMenu.add(saveButton);
        mainMenu.add(loadButton);
        mainMenu.add(clearButton);
        mainMenu.add(removeSaveButton);
        mainMenu.add(exitButton);
        return mainMenu;
    }

    private void createDraftingMenu() {
        draftMenu.add(draftThreeButton);
        draftMenu.add(draftFiveButton);
        draftMenu.addSeparator();
        draftMenu.add(increaseBlockButton);
        draftMenu.add(decreaseBlockButton);
        manageDraftButton.addActionListener(e -> draftMenu.show(manageDraftButton, 0, manageDraftButton.getHeight()));
    }

    private JPanel createBottomButtonPanel() {
        JPanel middlemanPanel = new JPanel();
        JPanel miniatureAndDraftPanel = new JPanel();
        middlemanPanel.setLayout(new BorderLayout());
        middlemanPanel.add(optionsPanel, BorderLayout.WEST);
        miniatureAndDraftPanel.add(miniatureButton);
        miniatureAndDraftPanel.add(manageDraftButton);
        middlemanPanel.add(miniatureAndDraftPanel, BorderLayout.EAST);
        return middlemanPanel;
    }


    private JScrollPane createInfoPanel() {
        infoPanel.setEditable(false);
        infoPanel.setLineWrap(true);
        infoPanel.setWrapStyleWord(true);
        infoPanel.setRows(15);
        infoPanel.setCaretColor(infoPanel.getBackground());
        infoPanel.setFont(new Font("Georgia", Font.PLAIN, 16));
        infoPanel.setText("Welcome to Maelir's Dungeon!\nUse the movement buttons to navigate through the dungeon.\nSelect options for the current room or other rooms using the buttons.\nEnjoy your adventure!");
        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setWheelScrollingEnabled(true);
        return scrollPane;
    }


    /* 
     * Update methods
     */

    public void setRoomPreviewPanel(RoomSnapshot snapshot) {
        roomPanel.setSnapshot(snapshot);
    }

    public void setRoomOptionsPanel(boolean isCurrent) {
        CardLayout cl = (CardLayout) optionsPanel.getLayout();
        if (isCurrent) {
            cl.show(optionsPanel, "CURRENT");
        } else {
            cl.show(optionsPanel, "NOT_CURRENT");
        }
    }

    public void setDoorButtonPanel(int directionIndex, DoorState doorState, boolean isCurrentRoom) {
        CardLayout cl = (CardLayout) buttonPanels[directionIndex].getLayout();
        JPanel panel = buttonPanels[directionIndex];
        if(isCurrentRoom == false) {
            cl.show(panel, "NONE");
            return;
        }
        switch (doorState) {
            case NONE:
                cl.show(panel, "NONE");
                break;
            case OPEN:
                cl.show(panel, "GO");
                break;
            case BLOCKED:
                cl.show(panel, "NONE");
                break;
            case LOCKED:
                cl.show(panel, "UNLOCK");
                break;
        }
    }

    public void setInfoPanelText(String text) {
        infoPanel.setText(text);
        SwingUtilities.invokeLater(() -> infoPanel.setCaretPosition(0));
    }

    public void showDraftOptions(boolean show) {
        draftThreeButton.setVisible(show);
        draftFiveButton.setVisible(show);
        increaseBlockButton.setVisible(show);
        decreaseBlockButton.setVisible(show);
    }


    /*
     * Event listener methods
     */
    public void onMove(Direction direction, ActionListener listener) {
        JButton button = getButton(direction);
        for (ActionListener al : button.getActionListeners()) {
            button.removeActionListener(al);
        }
        button.addActionListener(listener);
    }

    private JButton getButton(Direction direction) {
        return switch (direction) {
            case North -> northButton;
            case East -> eastButton;
            case South -> southButton;
            case West -> westButton;
        };
    }


    public void onUnlock(Direction direction, ActionListener listener) {
        JButton button = getUnlockButton(direction);
        for (ActionListener al : button.getActionListeners()) {
            button.removeActionListener(al);
        }
        button.addActionListener(listener);
    }

    private JButton getUnlockButton(Direction direction) {
        return switch (direction) {
            case North -> northUnlockButton;
            case East -> eastUnlockButton;
            case South -> southUnlockButton;
            case West -> westUnlockButton;
        };
    }


    public void onSave(ActionListener listener) {
        for (ActionListener al : saveButton.getActionListeners()) {
            saveButton.removeActionListener(al);
        }
        saveButton.addActionListener(listener);
    }

    public void onLoad(ActionListener listener) {   
        for (ActionListener al : loadButton.getActionListeners()) {
            loadButton.removeActionListener(al);
        }
        loadButton.addActionListener(listener);
    }

    public void onClear(ActionListener listener) {
        for (ActionListener al : clearButton.getActionListeners()) {
            clearButton.removeActionListener(al);
        }
        clearButton.addActionListener(listener);
    }

    public void onRemoveSave(ActionListener listener) {
        for (ActionListener al : removeSaveButton.getActionListeners()) {
            removeSaveButton.removeActionListener(al);
        }
        removeSaveButton.addActionListener(listener);
    }

    public void onExit(ActionListener listener) {
        for (ActionListener al : exitButton.getActionListeners()) {
            exitButton.removeActionListener(al);
        }
        exitButton.addActionListener(listener);
    }

    public void onPlaceRoom(ActionListener listener) {
        for (ActionListener al : placeButton.getActionListeners()) {
            placeButton.removeActionListener(al);
        }
        placeButton.addActionListener(listener);
    }

    public void onRemoveRoom(ActionListener listener) {
        for (ActionListener al : removeButton.getActionListeners()) {
            removeButton.removeActionListener(al);
        }
        removeButton.addActionListener(listener);
    }

    public void onGoToRoom(ActionListener listener) {
        for (ActionListener al : goToButton.getActionListeners()) {
            goToButton.removeActionListener(al);
        }
        goToButton.addActionListener(listener);
    }

    public void onManageDrafting(ActionListener listener) {
        for (ActionListener al : manageDraftButton.getActionListeners()) {
            manageDraftButton.removeActionListener(al);
        }
        manageDraftButton.addActionListener(listener);
    }

    public void onShowMiniatures(ActionListener listener) {
        for (ActionListener al : miniatureButton.getActionListeners()) {
            miniatureButton.removeActionListener(al);
        }
        miniatureButton.addActionListener(listener);
    }

    public void onSetDraftThree(ActionListener listener) {
        for (ActionListener al : draftThreeButton.getActionListeners()) {
            draftThreeButton.removeActionListener(al);
        }
        draftThreeButton.addActionListener(listener);
    }

    public void onSetDraftFive(ActionListener listener) {
        for (ActionListener al : draftFiveButton.getActionListeners()) {
            draftFiveButton.removeActionListener(al);
        }
        draftFiveButton.addActionListener(listener);
    }

    public void onIncreaseBlockedChance(ActionListener listener) {
        for (ActionListener al : increaseBlockButton.getActionListeners()) {
            increaseBlockButton.removeActionListener(al);
        }
        increaseBlockButton.addActionListener(listener);
    }

    public void onDecreaseBlockedChance(ActionListener listener) {
        for (ActionListener al : decreaseBlockButton.getActionListeners()) {
            decreaseBlockButton.removeActionListener(al);
        }
        decreaseBlockButton.addActionListener(listener);
    }
}
