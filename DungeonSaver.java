import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DungeonSaver {
    private static final List<String> header = Arrays.asList(
        "Room Number", "Room Type", "Name", "Door Count", "Locked", "Description",
        "Danger", "Contents", "Special Effect", "Miniature"
    );

    public static void saveRooms(String filename, ArrayList<Room> rooms) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            bw.write(joinRowWithComma(header));
            bw.newLine();

            for(Room room : rooms){
                List<String> row = roomToList(room);
                bw.write(joinRowWithComma(row));
                bw.newLine();
            }
            bw.flush();
        }
    }

    private static List<String> roomToList(Room room) {
        List<String> roomData = new ArrayList<>();
        roomData.add(String.valueOf(room.getRoomNumber()));
        roomData.add(nullToDash(room.getPrerequisite()));
        roomData.add(nullToDash(room.getName()));
        roomData.add(String.valueOf(room.getDoorCount()));
        roomData.add(String.valueOf(room.locked()));
        roomData.add(nullToDash(room.getDescription()));
        roomData.add(nullToDash(room.getDanger()));
        roomData.add(nullToDash(room.getContent()));
        roomData.add(nullToDash(room.getEffect()));
        roomData.add(nullToDash(room.getMiniature()));
        return roomData;
    }

    private static String joinRowWithComma(List<String> row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(formatString(row.get(i)));
        }
        return sb.toString();
    }

    private static String formatString(String entry) {
        boolean needQuotes = false;
        if (entry.contains(",") || entry.contains("\"") || entry.contains("\n")) {
            needQuotes = true;
        }
        if (!needQuotes) {
            return entry;
        }
        StringBuilder sb = new StringBuilder();
        sb.append('"');

        for (int i = 0; i < entry.length(); i++) {
            char c = entry.charAt(i);
            if (c == '"') {
                sb.append("\"\"");
            } else {
                sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    private static String nullToDash(String s) {
        return s == null ? "-" : s;
    }

    public static void saveGrid(String filename, int[][] roomGrid, int[] position) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (int[] row : roomGrid) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) sb.append(',');
                    sb.append(row[i]);
                }
                bw.write(sb.toString());
                bw.newLine();
            }
            bw.write("#PLAYER_POSITION");
            bw.newLine();
            bw.write(position[0] + "," + position[1]);
            bw.flush();
        }
    }

    public static boolean deleteSaves(String directory, String slot) throws IOException {
        Path dir = directory == null ? Paths.get("") : Paths.get(directory);
        Path pAll   = dir.resolve("allRooms" + slot + ".csv");
        Path pUnused= dir.resolve("unusedRooms" + slot + ".csv");
        Path pGrid  = dir.resolve("roomGrid" + slot + ".csv");

        boolean any = false;
        any |= Files.deleteIfExists(pAll);
        any |= Files.deleteIfExists(pUnused);
        any |= Files.deleteIfExists(pGrid);
        return any;
    }

    public static boolean emptySlot(String file) {
        Path allRoomPath = Paths.get(file);
        if (Files.exists(allRoomPath)) {
            return false;
        }
        return true;
    }
}
