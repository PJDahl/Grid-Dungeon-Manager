package userInterface.swing;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

public class ControlPanel extends JPanel {
    RoomPreviewPanel roomPanel;
    JButton saveButton = new JButton("Save & Exit");
    JButton loadButton = new JButton("Load Dungeon");
    JButton clearButton = new JButton("Clear Dungeon");
    JButton exitButton = new JButton("Exit");

    JButton northButton = new JButton("Go North");
    JButton eastButton = new JButton("Go East");
    JButton southButton = new JButton("Go South");
    JButton westButton = new JButton("Go West");

    JPanel optionsPanel = new JPanel();
    JButton goToButton = new JButton("Go To Room");
    JButton removeButton = new JButton("Remove Room");
    JButton placeButton = new JButton("Place Room");
    JButton unlockButton = new JButton("Unlock Room");
    JButton miniatureButton = new JButton("Show Miniatures");
    JButton manageDraftButton = new JButton("Manage Drafting");

    JTextArea infoPanel = new JTextArea();

    JButton draftThreeButton = new JButton("Set Draft To 3");
    JButton draftFiveButton = new JButton("Set Draft To 5");
    JButton increaseBlockButton = new JButton("Increase Blocked Chance");
    JButton decreaseBlockButton = new JButton("Decrease Blocked Chance");
    
    
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

        infoPanel.setEditable(false);
        infoPanel.setLineWrap(true);
        infoPanel.setWrapStyleWord(true);
        infoPanel.setPreferredSize(getPreferredSize());
        infoPanel.setCaretColor(infoPanel.getBackground());
        Font font = infoPanel.getFont();
        infoPanel.setFont(new Font("Georgia", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(infoPanel);
        infoPanel.setText("Welcome to Maelir's Dungeon!\nUse the movement buttons to navigate through the dungeon.\nSelect options for the current room or other rooms using the buttons below.\nEnjoy your adventure!");

        JPanel bottomPanel = new JPanel();
        JPanel middlemanPanel = new JPanel();
        JPanel miniatureAndDraftPanel = new JPanel();
        middlemanPanel.setLayout(new BorderLayout());
        bottomPanel.setLayout(new BorderLayout());
        optionsPanel.setLayout(new CardLayout());
        middlemanPanel.add(optionsPanel, BorderLayout.WEST);
        miniatureAndDraftPanel.add(miniatureButton);
        miniatureAndDraftPanel.add(manageDraftButton);
        middlemanPanel.add(miniatureAndDraftPanel, BorderLayout.EAST);
        bottomPanel.add(middlemanPanel, BorderLayout.NORTH);
        bottomPanel.add(scrollPane, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);



        JPanel mainMenu = new JPanel();
        mainMenu.setLayout(new FlowLayout(FlowLayout.CENTER));
        mainMenu.add(saveButton);
        mainMenu.add(loadButton);
        mainMenu.add(clearButton);
        mainMenu.add(exitButton);
        topPanel.add(mainMenu);

        JPanel roomOptionsCurrent = createRoomOptionsPanel(true);
        optionsPanel.add(roomOptionsCurrent, "CURRENT");
        JPanel roomOptionsNotCurrent = createRoomOptionsPanel(false);
        optionsPanel.add(roomOptionsNotCurrent, "NOT_CURRENT");
        setRoomOptionsPanel(false);

        configureButtons();
    }

    private void configureButtons() {
        saveButton.setFont(getFont());
        loadButton.setFont(getFont());
        clearButton.setFont(getFont());
        exitButton.setFont(getFont());

        northButton.setFont(getFont());
        eastButton.setFont(getFont());
        southButton.setFont(getFont());
        westButton.setFont(getFont());

        goToButton.setFont(getFont());
        removeButton.setFont(getFont());
        placeButton.setFont(getFont());
        unlockButton.setFont(getFont());
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

        c.fill = GridBagConstraints.NONE;
        c.insets = pad;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 1;
        c.gridy = 0;
        movementPanel.add(northButton, c);

        c = new GridBagConstraints();

        c.fill = GridBagConstraints.NONE;
        c.insets = pad;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 1;
        movementPanel.add(westButton, c);

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
        movementPanel.add(eastButton, c);

        c = new GridBagConstraints();

        c.fill = GridBagConstraints.NONE;
        c.insets = pad;
        c.weightx = 0;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 1;
        c.gridy = 2;
        movementPanel.add(southButton, c);

        return movementPanel;
    }

    private JPanel createRoomOptionsPanel(boolean current) {
        JPanel roomOptionsPanel = new JPanel();
        if (current) {
            roomOptionsPanel.add(unlockButton);
            return roomOptionsPanel;
        } else {
            roomOptionsPanel.add(placeButton);
            roomOptionsPanel.add(removeButton);
            roomOptionsPanel.add(goToButton);
            return roomOptionsPanel;
        }
    }

    public void setRoomOptionsPanel(boolean isCurrent) {
        CardLayout cl = (CardLayout) optionsPanel.getLayout();
        if (isCurrent) {
            cl.show(optionsPanel, "CURRENT");
        } else {
            cl.show(optionsPanel, "NOT_CURRENT");
        }
    }

}
