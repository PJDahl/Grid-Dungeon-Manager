package refactored.model;

import java.util.Arrays;

public class Room {
    
    private final int roomNumber;
    private final String prerequisite;
    private final String name;
    private final int doorCount;
    private final boolean locked;
    private final String description;
    private final String danger;
    private final String contents;
    private final String specialEffect;
    private final String miniature;
    private boolean[] lockedDoors = new boolean[4]; // Array to represent locked status of doors in directions N(0), E(1), S(2), W(3)
    private boolean[] doors = new boolean[4]; // Array to represent existence of doors in directions N(0), E(1), S(2), W(3)
    private boolean[] blockedDoors = new boolean[4]; // Array to represent blocked status of doors in directions N(0), E(1), S(2), W(3)
    private String connections; // e.g., "N,E,S,W" to indicate open connections

    private Room(Builder b) {
        this.roomNumber = b.roomNumber;
        this.prerequisite = b.prerequisite;
        this.name = b.name;
        this.doorCount = b.doorCount;
        this.locked = b.locked;
        this.description = b.description;
        this.danger = b.danger;
        this.contents = b.contents;
        this.specialEffect = b.specialEffect;
        this.miniature = b.miniature;
    }

    public boolean doesDoorExist(int directionIndex) {
        return directionIndex >= 0 && directionIndex < doors.length && doors[directionIndex];
    }

    public boolean isDoorBlocked(int directionIndex) {
        return directionIndex >= 0 && directionIndex < blockedDoors.length && blockedDoors[directionIndex];
    }

    public boolean isDoorLocked(int directionIndex) {
        return directionIndex >= 0 && directionIndex < lockedDoors.length && lockedDoors[directionIndex];
    }

    public void updateConnections() {
        StringBuilder sb = new StringBuilder();
        String[] dirLabels = {"N", "E", "S", "W"};
        for (int i = 0; i < doors.length; i++) {
            if (doors[i]) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(dirLabels[i]);
            }
        }
        connections = sb.toString();
    }

    /* 
     * Setters
     */

    public void setLockStatus(int directionIndex, boolean lockStatus) {
        if (directionIndex >= 0 && directionIndex < lockedDoors.length) {
            lockedDoors[directionIndex] = lockStatus;
        }
    }

    public void setDoors(boolean[] doorLayout) {
        if (doorLayout != null && doorLayout.length == doors.length) {
            doors = Arrays.copyOf(doorLayout, doorLayout.length);
            updateConnections();
        }
    }

    public void setDoorExists(int directionIndex, boolean exists) {
        if (directionIndex>= 0 && directionIndex < doors.length) {
            doors[directionIndex] = exists;
            updateConnections();
        }
    }

    public void setBlockedDoor(int directionIndex, boolean blocked) {
        if (directionIndex >= 0 && directionIndex < blockedDoors.length) {
            blockedDoors[directionIndex] = blocked;
        }
    }

    /* 
     * Getters
     */
    
    public int getRoomNumber() {
        return roomNumber;
    }

    public String getPrerequisite() {
        return prerequisite;
    }

    public String getName() {
        return name;
    }

    public boolean locked() {
        return locked;
    }

    public String getDescription() {
        return description;
    }

    public String getDanger() {
        return danger;
    }

    public String getContent() {
        return contents;
    }

    public String getEffect() {
        return specialEffect;
    }

    public int getDoorCount() {
        return doorCount;
    }

    public String getMiniature() {
        return miniature;
    }

    public boolean[] getLockedDoors() {
        return Arrays.copyOf(lockedDoors, lockedDoors.length);
    }

    public boolean[] getDoors() {
        return Arrays.copyOf(doors, doors.length);
    }

    public String getConnections() {
        return connections;
    }

    public boolean[] getBlockedDoors() {
        return Arrays.copyOf(blockedDoors, blockedDoors.length);
    }

    /*
     * Override Methods
     */


    @Override
    public String toString() {
        return "Room{\n" +
                "roomNumber= " + roomNumber +
                "\nprerequisite= " + prerequisite +
                "\nname= " + name +
                "\ndoorCount= " + doorCount +
                "\nlocked= " + locked +
                "\ndescription= " + description +
                "\ndanger= " + danger +
                "\ncontents= " + contents +
                "\nspecialEffect= " + specialEffect +
                "\nminiature= " + miniature +
                "\n}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room room = (Room) o;
        return roomNumber == room.roomNumber;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(roomNumber);
    }

    /*
     * Builder Class
     */

    public static class Builder {
        private int roomNumber;
        private String prerequisite;
        private String name;
        private Integer doorCount;
        private boolean locked;
        private String description;
        private String danger;
        private String contents;
        private String specialEffect;
        private String miniature;

        public Builder roomNumber(int v) { this.roomNumber = v; return this; }
        public Builder prerequisite(String v) { this.prerequisite = v; return this; }
        public Builder name(String v) { this.name = v; return this; }
        public Builder doorCount(Integer v) { this.doorCount = v; return this; }
        public Builder locked(boolean v) { this.locked = v; return this; }
        public Builder description(String v) { this.description = v; return this; }
        public Builder danger(String v) { this.danger = v; return this; }
        public Builder contents(String v) { this.contents = v; return this; }
        public Builder specialEffect(String v) { this.specialEffect = v; return this; }
        public Builder miniature(String v) { this.miniature = v; return this; }

        public Room build() {
            return new Room(this);
        }
    }
}