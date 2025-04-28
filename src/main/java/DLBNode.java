/**
 * A node in a DLB trie representing a token from a robot command sequence.
 */
public class DLBNode {
    public String token;
    public int code;
    public DLBNode child;
    public DLBNode sibling;

    public DLBNode(String token) {
        this.token = token;
        this.code = -1;
        this.child = null;
        this.sibling = null;
    }
}