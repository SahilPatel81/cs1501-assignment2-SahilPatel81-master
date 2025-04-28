/**
 * Represents a weighted edge in the robot’s room-graph for path planning.
 * Each edge connects a source room (the map key) to a destination room with a traversal cost.
 */
public class Edge {
    /**
     * The destination room ID.
     */
    public String to;

    /**
     * The traversal cost or time between rooms.
     */
    public int weight;

    /**
     * Constructs an edge from the current room to the specified destination with a given weight.
     *
     * @param to the destination room ID
     * @param weight the cost or time to traverse to that room
     */
    public Edge(String to, int weight) {
        this.to = to;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return String.format("Edge(to=%s, weight=%d)", to, weight);
    }
}