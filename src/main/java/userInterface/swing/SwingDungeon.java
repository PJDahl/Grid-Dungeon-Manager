package userInterface.swing;

import util.Position;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import model.DungeonManager;

import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;

public class SwingDungeon {
    
    public static void main(String[] args) {
        DungeonManager manager = new DungeonManager();

        try {  
            manager.newDungeon(new Position(0, 2), new Position(4, 2));
        } catch (IOException e) {
            System.out.println("Error creating dungeon: " + e.getMessage());
            return;
        }

        RoomPanel[][] dungeonGrid = new RoomPanel[7][5];

        JFrame frame = new JFrame();
        frame.setLayout(new GridLayout(1,2));
        frame.setTitle("Maelirs Dungeon");

        JPanel mapPanel = new JPanel();
        createDungeonMapDisplay(mapPanel, manager, dungeonGrid);

        JPanel controlPanel = createDungeonControlDisplay(manager, dungeonGrid);
        frame.add(controlPanel);
        frame.add(mapPanel);

        frame.setSize(1500,800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void createDungeonMapDisplay(JPanel panel, DungeonManager manager, RoomPanel[][] dungeonGrid) {
        panel.setFont(new Font("Georgia", Font.PLAIN, 14));
        panel.setLayout(new GridLayout(7,5));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Dungeon Map", TitledBorder.CENTER, TitledBorder.TOP, panel.getFont().deriveFont(Font.BOLD, 14f)));

        for (int row = 0; row < 7; row++) {
            for (int col = 0; col < 5; col++) {
                Position pos = new Position(row, col);
                RoomPanel label = new RoomPanel(pos);
                dungeonGrid[row][col] = label;
            }
        }

        addRoomPanels(dungeonGrid, panel);
        updateDungeonDisplay(dungeonGrid, manager);

        dungeonGrid[manager.getCurrentPosition().row()][manager.getCurrentPosition().col()].setCurrent(true);
    }

    private static JPanel createDungeonControlDisplay(DungeonManager manager, RoomPanel[][] dungeonGrid) {
        RoomSnapshot StartingRoomSnapshot = dungeonGrid[manager.getCurrentPosition().row()][manager.getCurrentPosition().col()].getSnapshot();
        RoomPreviewPanel previewPanel = new RoomPreviewPanel(StartingRoomSnapshot);
        ControlPanel controlPanel = new ControlPanel(previewPanel);
        return controlPanel;
    }

    private static void updateDungeonDisplay(RoomPanel[][] dungeonGrid, DungeonManager manager) {
        int rows = dungeonGrid.length;
        int cols = dungeonGrid[0].length;
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Position pos = new Position(row, col);
                RoomSnapshot snapshot = manager.getRoomSnapshot(pos);
                dungeonGrid[row][col].setSnapshot(snapshot);
            }
        }
    }

    private static void addRoomPanels(RoomPanel[][] grid, JPanel panel) {
        int rows = grid.length;
        int cols = grid[0].length;

        for (int r = rows - 1; r >= 0; r--) {
            for (int c = 0; c < cols; c++) {
                panel.add(grid[r][c]);
            }
        }
    }
}