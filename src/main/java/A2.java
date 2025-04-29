import java.util.*;

public class A2 implements A2Interface {

    @Override
    public List<Integer> compressCommandPatterns(DLBNode trieRoot, List<List<String>> sequences) {
        List<Integer> compressed = new ArrayList<>();
        
        int nextCode = getMaxCode(trieRoot) + 1; // Start assigning codes from the next available code

        DLBNode current = trieRoot;
        for (List<String> sequence : sequences) {
            int j = 0;                
            // loop through all the words in the sequence
            while (j < sequence.size()) {
                //find matching node
                DLBNode match = findChild(current, sequence.get(j));
                if (match != null && match.child != null){
                    //look deeper into the trie and find matching child
                    if(match.child.token == sequence.get(j + 1)){
                        compressed.add(match.child.code);    
                        if(sequence.size() > j + 2){                        
                            DLBNode newNode = insertChild(current, Arrays.asList(sequence.get(j),sequence.get(j+1)), nextCode);
                            nextCode += 1;
                        }
                        j++;                        
                    }
                }else{
                    compressed.add(match.code);  
                    if(sequence.size() > j + 1){                      
                        DLBNode newNode = insertChild(current, Arrays.asList(sequence.get(j),sequence.get(j+1)), nextCode);
                        nextCode += 1;
                    }
                }
                 
                j++;
            }          
        }

        return compressed;
    }

    private int getMaxCode(DLBNode node) {
        if (node == null) return -1;
        int max = node.code;
        max = Math.max(max, getMaxCode(node.child));
        max = Math.max(max, getMaxCode(node.sibling));
        return max;
    }
    
    private DLBNode findChild(DLBNode head, String token) {
        DLBNode curr = head;
        while (curr != null) {
            if (curr.token.equals(token)) return curr;
            curr = curr.sibling;
        }
        return null;
    }

   
    
    public DLBNode insertChild(DLBNode head,
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

    @Override
    public Map<String, ExitInfo> computeClosestExits(Map<String, List<Edge>> roomGraph, List<String> exitRooms) {
        Map<String, ExitInfo> result = new HashMap<>();
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> nearestExit = new HashMap<>();

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(distances::get));
        Set<String> visited = new HashSet<>();

        for (String room : roomGraph.keySet()) {
            distances.put(room, Integer.MAX_VALUE);
        }

        for (String exit : exitRooms) {
            distances.put(exit, 0);
            nearestExit.put(exit, exit);
            pq.add(exit);
        }

        while (!pq.isEmpty()) {
            String current = pq.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            int currDist = distances.get(current);

            for (Edge edge : roomGraph.getOrDefault(current, new ArrayList<>())) {
                String neighbor = edge.to;
                int newDist = currDist + edge.weight;

                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    nearestExit.put(neighbor, nearestExit.get(current));
                    pq.add(neighbor);
                }
            }
        }

        for (String room : roomGraph.keySet()) {
            int dist = distances.getOrDefault(room, Integer.MAX_VALUE);
            String exit = nearestExit.getOrDefault(room, null);
            result.put(room, new ExitInfo(exit, dist));
        }

        return result;
    }


    @Override
    public List<String> recoverSignal(List<String> corrupted, Map<String, List<String>> graph, String exitRoom) {
        return null;
    }
    
    
}
