import java.util.List;
import java.util.Map;

public interface A2Interface {
    /**
     * Compress a list of robot movement command sequences using a token-pattern
     * dictionary.
     * Starts with a possibly pre-populated DLB trie and assigns numeric codes using
     * LZW-style pattern growth.
     *
     * @param trieRoot  root node of a DLB trie storing previously known token
     *                  sequences
     * @param sequences list of command sequences, each a list of string tokens
     * @return a list of integer codes representing the codewrods of LZW
     * 
     */
    List<Integer> compressCommandPatterns(DLBNode trieRoot, List<List<String>> sequences);

    /**
     * For each room in the robot's room-graph, compute the closest exit and
     * distance.
     * Rooms that cannot reach any exit should be recorded with null exit and
     * infinite (Integer.MAX_VALUE) distance.
     *
     * @param roomGraph adjacency list: map from room name to list of weighted
     *                  outgoing edges
     * @param exitRooms list of room names considered valid exits
     * @return a map from each room name to its ExitInfo (closest exit and distance)
     */

    Map<String, ExitInfo> computeClosestExits(Map<String, List<Edge>> roomGraph, List<String> exitRooms);

    /**
     * Recover the robot's most likely original room path from a corrupted sequence
     * of visited room outputs.
     * The room graph defines valid transitions, and the recovered path must reach
     * the given exit room.
     * Costs are assigned based on number of changes (edit distance) including
     * insertions, deletions, and substitutions.
     *
     * @param corruptedOutput a list of observed room labels (some may be incorrect)
     * @param roomGraph       a graph where keys are room labels and values are
     *                        lists of reachable room labels (neighbors)
     * @param exitRoom        the desired final room in the reconstructed path
     * @return the most likely true room sequence from the start to the exit
     */
    List<String> recoverSignal(List<String> corruptedOutput, Map<String, List<String>> roomGraph, String exitRoom);
}