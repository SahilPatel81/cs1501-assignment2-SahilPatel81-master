import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

/**
 * JUnit 4 tests for compressCommandPatterns in A2.java,
 * using a flat‐structure DLB trie (no dummy root).
 */
public class CompressCommandPatternsTest {

    /**
     * Find a node whose token matches `token` in the sibling chain starting at `head`.
     *
     * @param head  first node in a sibling chain (may be null)
     * @param token the token to search for
     * @return the matching DLBNode or null if not found
     */
    public static DLBNode findChild(DLBNode head, String token) {
        DLBNode curr = head;
        while (curr != null) {
            if (curr.token.equals(token)) return curr;
            curr = curr.sibling;
        }
        return null;
    }

    /**
     * Insert a full sequence of tokens into a DLB trie whose top‐level
     * head pointer may initially be null.  Returns the updated head.
     *
     * @param head      current head of the top‐level siblings
     * @param sequence  list of tokens for the sequence (size>=1)
     * @param code      integer code to assign at the terminal node
     * @return new head of the top‐level siblings chain
     */
    public static DLBNode insertSequence(DLBNode head,
                                         List<String> sequence,
                                         int code) {
        if (sequence.isEmpty()) return head;

        // 1) handle first token at top level
        String first = sequence.get(0);
        DLBNode node = findChild(head, first);
        if (node == null) {
            node = new DLBNode(first);
            node.code = (code == -1 ? -1 : node.code); // will set below if terminal
            // prepend to head chain
            node.sibling = head;
            head = node;
        }

        // 2) walk deeper levels
        for (int i = 1; i < sequence.size(); i++) {
            String tok = sequence.get(i);
            DLBNode child = findChild(node.child, tok);
            if (child == null) {
                child = new DLBNode(tok);
                // link into child list
                child.sibling = node.child;
                node.child = child;
            }
            node = child;
        }

        // 3) assign code at terminal node
        node.code = code;
        return head;
    }

    @Test
    public void testCompressionBasic() {
        A2Interface a2 = new A2();
        DLBNode root = null;

        // Preload basic commands with their codes
        root = insertSequence(root, Arrays.asList("MOVE"),    0);
        root = insertSequence(root, Arrays.asList("FORWARD"), 1);
        root = insertSequence(root, Arrays.asList("TURN"),    2);
        root = insertSequence(root, Arrays.asList("LEFT"),    3);

        List<List<String>> sequences = Arrays.asList(
            Arrays.asList("MOVE","FORWARD","MOVE","FORWARD"),
            Arrays.asList("TURN","LEFT","MOVE","FORWARD")
        );

        System.out.println("Input sequences: " + sequences);
        List<Integer> result = a2.compressCommandPatterns(root, sequences);
        System.out.println("Compressed codes: " + result);

        // Expected: [0,1,2,2,3,4]
        List<Integer> expected = Arrays.asList(0, 1, 4, 2, 3, 4);
        assertEquals("Basic compression output mismatch", expected, result);
    }

    @Test
    public void testOverlappingPrefixReuse() {
        A2Interface a2 = new A2();
        DLBNode root = null;

        root = insertSequence(root, Arrays.asList("MOVE"),    0);
        root = insertSequence(root, Arrays.asList("FORWARD"), 1);

        List<String> seq = Arrays.asList("MOVE","FORWARD","MOVE","FORWARD");
        System.out.println("Input sequence: " + seq);
        List<Integer> result = a2.compressCommandPatterns(root, Collections.singletonList(seq));
        System.out.println("Compressed codes: " + result);

        // After emitting 0,1; the pattern "MOVE FORWARD" gets code 2 and is reused
        List<Integer> expected = Arrays.asList(0, 1, 2);
        assertEquals("Overlapping prefix reuse failed", expected, result);
    }

    @Test
    public void testUnknownCommandIntroduced() {
        A2Interface a2 = new A2();
        DLBNode root = null;

        root = insertSequence(root, Arrays.asList("MOVE"),    0);
        root = insertSequence(root, Arrays.asList("FORWARD"), 1);
        root = insertSequence(root, Arrays.asList("JUMP"),    2);

        List<String> seq = Arrays.asList("MOVE","FORWARD","JUMP");
        System.out.println("Input sequence: " + seq);
        List<Integer> result = a2.compressCommandPatterns(root, Collections.singletonList(seq));
        System.out.println("Compressed codes: " + result);

        // MOVE→0, FORWARD→1, JUMP→2 (primitive), no new multi-token needed
        List<Integer> expected = Arrays.asList(0, 1, 2);
        assertEquals("Unknown command introduction failed", expected, result);
    }
}
