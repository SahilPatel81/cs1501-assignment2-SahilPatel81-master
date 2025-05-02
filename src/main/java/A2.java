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
        int n = corrupted.size();
        Set<String> rooms = graph.keySet();
    
        // dp[i][room] = min cost to process first i corrupted tokens ending at room
        Map<String, int[]> dp = new HashMap<>();
        Map<String, String[]> parent = new HashMap<>();
        Map<String, String[]> moveType = new HashMap<>();
    
        for (String room : rooms) {
            dp.put(room, new int[n + 1]);
            Arrays.fill(dp.get(room), Integer.MAX_VALUE / 2);
            parent.put(room, new String[n + 1]);
            moveType.put(room, new String[n + 1]);
        }
    
        // base case: start from START room with 0 corrupted tokens
        if (dp.containsKey("START")) {
            dp.get("START")[0] = 0;
        }
    
        // Fill DP table
        for (int i = 0; i <= n; i++) {
            for (String r : rooms) {
                if (i > 0) {
                    // Deletion: stay at r, consume corrupted token
                    int cost = dp.get(r)[i - 1] + 1;
                    if (cost < dp.get(r)[i]) {
                        dp.get(r)[i] = cost;
                        parent.get(r)[i] = r;
                        moveType.get(r)[i] = "DELETE";
                    }
                }
    
                for (String p : rooms) {
                    if (graph.getOrDefault(p, Collections.emptyList()).contains(r)) {
                        if (i > 0) {
                            // Match/Substitute: move p -> r, consume corrupted[i-1]
                            int matchCost = dp.get(p)[i - 1] + (r.equals(corrupted.get(i - 1)) ? 0 : 1);
                            if (matchCost < dp.get(r)[i]) {
                                dp.get(r)[i] = matchCost;
                                parent.get(r)[i] = p;
                                moveType.get(r)[i] = "MATCH";
                            }
                        }
                
                        // Insertion: move p -> r without consuming token
                        if (!p.equals(r)) {  // prevent unnecessary self-insertion
                            int insertCost = dp.get(p)[i] + 1;
                            if (insertCost < dp.get(r)[i]) {
                                dp.get(r)[i] = insertCost;
                                parent.get(r)[i] = p;
                                moveType.get(r)[i] = "INSERT";
                            }
                        }
                    }
                }
            }
        }
    
        // Recover path from (n, exitRoom)
        List<String> path = new ArrayList<>();
        String currentRoom = exitRoom;
        int currentIndex = n;
        String prevRoom = null;
    
        while (currentRoom != null && (currentIndex > 0 || !currentRoom.equals("START"))) {
            if(!path.contains(currentRoom)){
                path.add(currentRoom);
            }

            String move = moveType.get(currentRoom)[currentIndex];
            prevRoom = parent.get(currentRoom)[currentIndex];
            if (move == null) {
                break;
            }
            if (move.equals("MATCH") || move.equals("DELETE")) {
                currentIndex--;
            }
            currentRoom = prevRoom;
        }
    
        Collections.reverse(path);
        return path;
    }
    
    
}
