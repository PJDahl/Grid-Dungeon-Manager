import java.util.ArrayList;
import java.util.List;

public class Room {
    
    private final int roomNumber;
    private final String roomType;
    private final String name;
    private final Integer doorCount;
    private final boolean locked;
    private final String description;
    private final String danger;
    private final String contents;
    private final String specialEffect;
    private final String miniature;
    private boolean[] lockedDoors = new boolean[4]; // Array to represent locked status of doors in directions N(0), E(1), S(2), W(3)
    private String connections; // e.g., "N,E,S,W" to indicate open connections

    private Room(Builder b) {
        this.roomNumber = b.roomNumber;
        this.roomType = b.roomType;
        this.name = b.name;
        this.doorCount = b.doorCount;
        this.locked = b.locked;
        this.description = b.description;
        this.danger = b.danger;
        this.contents = b.contents;
        this.specialEffect = b.specialEffect;
        this.miniature = b.miniature;
    }

    /* 
     * Setters
     */

    public void setLockStatus(int directionIndex, boolean lockStatus) {
        if (directionIndex >= 0 && directionIndex < lockedDoors.length) {
            lockedDoors[directionIndex] = lockStatus;
        }
    }

    public void setConnections(String connections) {
        this.connections = connections;
    }

    /* 
     * Getters
     */
    
    public int getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
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
        return lockedDoors;
    }

    public String getConnections() {
        return connections;
    }

    /*
     * Override Methods
     */


    @Override
    public String toString() {
        return "Room{" +
                "roomNumber=" + roomNumber +
                ", roomType='" + roomType + '\'' +
                ", name='" + name + '\'' +
                ", doorCount=" + doorCount +
                ", locked=" + locked +
                ", description='" + description + '\'' +
                ", danger='" + danger + '\'' +
                ", contents='" + contents + '\'' +
                ", specialEffect='" + specialEffect + '\'' +
                ", miniature='" + miniature + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room room = (Room) o;
        return roomNumber == room.roomNumber;
    }

    /*
     * Builder Class
     */

    public static class Builder {
        private int roomNumber;
        private String roomType;
        private String name;
        private Integer doorCount;
        private boolean locked;
        private String description;
        private String danger;
        private String contents;
        private String specialEffect;
        private String miniature;

        public Builder roomNumber(int v) { this.roomNumber = v; return this; }
        public Builder roomType(String v) { this.roomType = v; return this; }
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
