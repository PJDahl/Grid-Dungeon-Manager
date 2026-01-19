package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import util.DoorState;
import util.Position;

public class DungeonRepository {
    private final ObjectMapper objectMapper;

    public DungeonRepository() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public Map<Integer, Room> loadAllRooms() throws IOException {
        List<Room> rooms = readRooms();
        HashMap<Integer, Room> roomMap = new HashMap<>();
        for (Room room : rooms) {
            roomMap.put(room.getRoomNumber(), room);
        }
        return roomMap;
    }

    public DungeonSaveData loadSaveData(int slot) throws IOException {
        Path path = slotPath(slot);
        return objectMapper.readValue(path.toFile(), DungeonSaveData.class);
    }

    public void save(int slot, Map<Integer, Room> allRooms, List<Room> unusedRooms, int[][] houseGrid,
                     Position currentPosition, Position startingPosition, int startingRoomId, int blockedDoorChance, int roomAmount) throws IOException {
        DungeonSaveData saveData = new DungeonSaveData();
        saveData.houseGrid = houseGrid;
        saveData.currentPosition = currentPosition;
        saveData.startingPosition = startingPosition;
        saveData.startingRoomId = startingRoomId;
        saveData.blockedDoorChance = blockedDoorChance;
        saveData.roomAmount = roomAmount;

        List<Integer> unusedRoomIds = new ArrayList<>();
        for (Room room : unusedRooms) {
            unusedRoomIds.add(room.getRoomNumber());
        }
        saveData.unusedRoomIds = unusedRoomIds;

        List<RoomStateData> roomStates = new ArrayList<>();
        for (Room room : allRooms.values()) {
            DoorState[] doors = room.getDoors();

            if(anyDoor(doors)) {
                RoomStateData stateData = new RoomStateData(room.getRoomNumber(), doors);
                roomStates.add(stateData);
            }
        }
        saveData.roomStates = roomStates;

        Path filePath = slotPath(slot);
        Files.createDirectories(filePath.getParent());
        objectMapper.writeValue(filePath.toFile(), saveData);
    }

    private boolean anyDoor(DoorState[] array) {
        for (DoorState a : array){
            if (a != DoorState.NONE) {
                return true;
            }
        }
        return false;
    }



    /*
     * Helper methods for file paths
     */

    public boolean saveExists(int slot) {
        return Files.exists(slotPath(slot));
    }

    private Path slotPath(int slot) {
        String slotStr = String.valueOf(slot);
        return Paths.get("saves", slotStr, "dungeon_save_" + slotStr + ".json");
    }




    /* 
     * Helper methods for reading rooms from CSV
     */
    private static ArrayList<Room> readRooms() throws IOException{
        Path path = Paths.get("saves", "default", "rooms.csv");

        ArrayList<Room> rooms = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            br.readLine(); // Skip header line

            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                Room newRoom = parseRoom(line);
                rooms.add(newRoom);
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
        return buildRoomFromRow(row);
    }

    private static Room buildRoomFromRow(List<String> row) {
        int roomNumber = Integer.valueOf(row.get(0));
        String prerequisite = emptyToNull(row.get(1));
        String name = emptyToNull(row.get(2));
        Integer doorCount = Integer.valueOf(row.get(3));
        String lockedStr = emptyToNull(row.get(4));
        boolean locked = lockedStr != null && (lockedStr.equalsIgnoreCase("ja") || lockedStr.equalsIgnoreCase("true"));
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
        if (t.isEmpty() || t.equals("-")) {
            return null;
        } else {
            return t;
        }
    }
}
