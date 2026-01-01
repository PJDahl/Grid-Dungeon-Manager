package userInterface.swing;

import util.Position;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import model.DungeonManager;
import model.Room;

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

        JPanel controlPanel = new JPanel();
        createDungeonControlDisplay(controlPanel, manager);
        frame.add(controlPanel);

        JPanel mapPanel = new JPanel();
        createDungeonMapDisplay(mapPanel, manager, dungeonGrid);
        frame.add(mapPanel);

        frame.setSize(1000,600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static void createDungeonMapDisplay(JPanel panel, DungeonManager manager, RoomPanel[][] dungeonGrid) {
        panel.setLayout(new GridLayout(7,5));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Dungeon Map", TitledBorder.CENTER, TitledBorder.TOP));

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

    private static void createDungeonControlDisplay(JPanel panel, DungeonManager manager) {
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Dungeon Controls", TitledBorder.CENTER, TitledBorder.TOP));
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