import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DungeonLoader {

    /*
     * Methods to read rooms from a CSV file
     */
    
    public static ArrayList<Room> readRooms(String filename) throws IOException{

        ArrayList<Room> rooms = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); // Skip header line

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                rooms.add(parseRoom(line));
            }
        }   
        return rooms;
    }

    private static Room parseRoom(String line) {
        List<String> row = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentValue.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                row.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        row.add(currentValue.toString());
        Room room = buildRoomFromRow(row);
        return room;
    }

    private static Room buildRoomFromRow(List<String> row) {
        int roomNumber = Integer.valueOf(row.get(0));
        String prerequisite = emptyToNull(row.get(1));
        String name = emptyToNull(row.get(2));
        Integer doorCount = Integer.valueOf(row.get(3));
        boolean locked = row.get(4).equalsIgnoreCase("ja") || row.get(4).equalsIgnoreCase("true") ? true : false;
        String description = emptyToNull(row.get(5));
        String danger = emptyToNull(row.get(6));
        String contents = emptyToNull(row.get(7));
        String specialEffect = emptyToNull(row.get(8));
        String miniature = emptyToNull(row.get(9));

        return new Room.Builder()
                .roomNumber(roomNumber)
                .prerequisite(prerequisite)
                .name(name)
                .doorCount(doorCount)
                .locked(locked)
                .description(description)
                .danger(danger)
                .contents(contents)
                .specialEffect(specialEffect)
                .miniature(miniature)
                .build();
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()){
            return null;
        } else if (t.equals("-")){
            return null;
        } else {
            return t;
        }
    }

    /*
     * Methods to read room grid from a CSV file
     */

    public static GridData readGrid(String filename) throws IOException {
        ArrayList<int[]> rows = new ArrayList<>();
        int[] position = new int[2];

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                if (line.startsWith("#PLAYER_POSITION")) {
                    String posLine = br.readLine();
                    if (posLine != null) {
                        position = parsePositionLine(posLine);
                    }
                    break;
                }
                String[] tokens = line.split(",");
                int[] row = new int[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    row[i] = Integer.parseInt(tokens[i].trim());
                }
                rows.add(row);
            }
        }

        int[][] grid = new int[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            grid[i] = rows.get(i);
        }

        return new GridData(grid, position);
    }

    private static int[] parsePositionLine(String line) {
        String[] tokens = line.split(",");
        int[] position = new int[2];
        position[0] = Integer.parseInt(tokens[0].trim());
        position[1] = Integer.parseInt(tokens[1].trim());
        return position;
    }

    public static class GridData {
        public final int[][] grid;
        public final int[] position;

        public GridData(int[][] grid, int[] position) {
            this.grid = grid;
            this.position = position;
        }
    }
}