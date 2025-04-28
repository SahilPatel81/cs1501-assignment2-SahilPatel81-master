/**
 * Holds information about the closest exit for a given room.
 */
public class ExitInfo {
    /**
     * The name of the closest exit room, or null if unreachable.
     */
    public String exitRoom;
    /**
     * The shortest distance (cost) to the exit, or Integer.MAX_VALUE if unreachable.
     */
    public int distance;

    /**
     * Constructs an ExitInfo record.
     *
     * @param exitRoom the closest exit room name, or null
     * @param distance the cost to that exit, or Integer.MAX_VALUE
     */
    public ExitInfo(String exitRoom, int distance) {
        this.exitRoom = exitRoom;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "ExitInfo(exitRoom=" + exitRoom + ", distance=" + distance + ")";
    }
}
