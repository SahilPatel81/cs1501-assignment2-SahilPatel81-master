import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * JUnit 4 tests for recoverSignal in A2 (Assignment 2).
 */
public class RecoverSignalTest {

    /**
     * Helper to build a simple linear graph.
     */
    private Map<String, List<String>> linearGraph(String... nodes) {
        Map<String, List<String>> graph = new HashMap<>();
        for (int i = 0; i < nodes.length - 1; i++) {
            graph.put(nodes[i], Collections.singletonList(nodes[i + 1]));
        }
        graph.put(nodes[nodes.length - 1], Collections.emptyList());
        return graph;
    }

    @Test
    public void testExactMatch() {
        A2Interface a2 = new A2();
        // Graph: START->A->B->C
        Map<String, List<String>> graph = new HashMap<>();
        graph.put("START", Collections.singletonList("A"));
        graph.putAll(linearGraph("A", "B", "C"));
        String exitRoom = "C";

        List<String> corrupted = Arrays.asList("A", "B", "C");
        List<String> result = a2.recoverSignal(corrupted, graph, exitRoom);

        System.out.println("\n=== testExactMatch ===");
        System.out.println("Corrupted: " + corrupted);
        System.out.println("Graph     : " + graph);
        System.out.println("Desired exit    : " + exitRoom);      
        System.out.println("Result:   " + result);
        System.out.println("Expected: [A, B, C]");

        assertEquals(Arrays.asList("A", "B", "C"), result);
    }

    @Test
    public void testSingleSubstitution() {
        A2Interface a2 = new A2();
        Map<String, List<String>> graph = new HashMap<>();
        graph.put("START", Collections.singletonList("A"));
        graph.putAll(linearGraph("A", "B", "C"));

        String exitRoom = "C";

        List<String> corrupted = Arrays.asList("A", "X", "C");
        List<String> result = a2.recoverSignal(corrupted, graph, exitRoom);

        System.out.println("\n=== testSingleSubstitution ===");
        System.out.println("Corrupted: " + corrupted);
        System.out.println("Graph     : " + graph);
        System.out.println("Desired exit    : " + exitRoom);      
        System.out.println("Result:   " + result);
        System.out.println("Expected: [A, B, C]");

        assertEquals(Arrays.asList("A", "B", "C"), result);
    }

    @Test
    public void testInsertion() {
        A2Interface a2 = new A2();
        Map<String, List<String>> graph = new HashMap<>();
        graph.put("START", Collections.singletonList("A"));
        graph.putAll(linearGraph("A", "B", "C"));
        String exitRoom = "C";

        List<String> corrupted = Arrays.asList("A", "B", "X", "C");
        List<String> result = a2.recoverSignal(corrupted, graph, exitRoom);

        System.out.println("\n=== testInsertion ===");
        System.out.println("Corrupted: " + corrupted);
        System.out.println("Graph     : " + graph);
        System.out.println("Desired exit    : " + exitRoom);      
        System.out.println("Result:   " + result);
        System.out.println("Expected: [A, B, C]");
        assertEquals(Arrays.asList("A", "B", "C"), result);
    }

    @Test
    public void testDeletion() {
        A2Interface a2 = new A2();
        Map<String, List<String>> graph = new HashMap<>();
        graph.put("START", Collections.singletonList("A"));
        graph.putAll(linearGraph("A", "B", "C"));
        String exitRoom = "C";

        List<String> corrupted = Arrays.asList("A", "C");
        List<String> result = a2.recoverSignal(corrupted, graph, exitRoom);

        System.out.println("\n=== testDeletion ===");
        System.out.println("Corrupted: " + corrupted);  
        System.out.println("Graph     : " + graph);
        System.out.println("Desired exit    : " + exitRoom);      
        System.out.println("Result:   " + result);
        Set<List<String>> expected = new HashSet<>();
        expected.add(Arrays.asList("A","B","C")); 
        expected.add(Arrays.asList("B","C"));

        assertTrue(
          "Deletion test: result " + result + " not in " + expected,
          expected.contains(result)
        );
    }

    @Test
    public void testComplexNoise() {
        A2Interface a2 = new A2();
        Map<String, List<String>> graph = new HashMap<>();
        graph.put("START", Collections.singletonList("A"));
        graph.putAll(linearGraph("A", "B", "C", "D"));
        String exitRoom = "D";
        List<String> corrupted = Arrays.asList("X", "A", "B", "Y", "D");
        System.out.println("\n=== testComplexNoise ===");
        System.out.println("Corrupted: " + corrupted);
        System.out.println("Graph     : " + graph);
        System.out.println("Desired exit    : " + exitRoom);
        List<String> result = a2.recoverSignal(corrupted, graph, exitRoom);      

        Set<List<String>> expected = new HashSet<>();
        expected.add(Arrays.asList("START", "A","B","C", "D"));
        expected.add(Arrays.asList("A", "B","C", "D"));     // delete C then insert C

        assertTrue(
          "Complex Noise test: result " + result + " not in " + expected,
          expected.contains(result)
        );        
    }

    @Test
    public void testRecoverSignal_SkipToExitOnly() {
        A2Interface a2 = new A2();
        // a simple corrupted trace
        List<String> corrupted = Arrays.asList("A", "B", "C");
        // define transitions so that rooms A→B→C form a cycle, and D is isolated
        Map<String, List<String>> transitions = new HashMap<>();
        transitions.put("A", Collections.singletonList("B"));
        transitions.put("B", Collections.singletonList("C"));
        transitions.put("C", Collections.singletonList("A"));
        transitions.put("D", Collections.emptyList()); // exit room, no outgoing edges

        String exitRoom = "D";

        System.out.println("Corrupted trace : " + corrupted);
        System.out.println("Graph     : " + transitions);
        System.out.println("Desired exit    : " + exitRoom);

        List<String> result = a2.recoverSignal(corrupted, transitions, exitRoom);

        System.out.println("Recovered path  : " + result);

        // Since we can delete all three inputs and remain at D, expect ["D"]
        assertEquals(
                "Expected to skip all corrupted tokens and end at D",
                Collections.singletonList("D"),
                result);
    }
}
