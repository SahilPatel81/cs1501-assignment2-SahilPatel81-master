import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * JUnit 4 tests for computeClosestExits in A2Interface.
 */
public class ComputeClosestExitsTest {

    @Test
    public void testSingleExitDirectConnection() {
        A2Interface a2 = new A2();
        Map<String, List<Edge>> graph = new HashMap<>();
        // Simple two-room graph: A <-> B
        graph.put("A", Arrays.asList(new Edge("B", 5)));
        graph.put("B", Arrays.asList(new Edge("A", 5)));
        List<String> exits = Collections.singletonList("B");

        Map<String, ExitInfo> result = a2.computeClosestExits(graph, exits);
        // Room B is exit at distance 0
        ExitInfo infoA = result.get("A");
        ExitInfo infoB = result.get("B");

        assertNotNull(infoA);
        assertEquals("B", infoA.exitRoom);
        assertEquals(5, infoA.distance);

        assertNotNull(infoB);
        assertEquals("B", infoB.exitRoom);
        assertEquals(0, infoB.distance);
    }

    @Test
    public void testUnreachableRoom() {
        A2Interface a2 = new A2();
        Map<String, List<Edge>> graph = new HashMap<>();
        graph.put("A", Arrays.asList(new Edge("B", 1)));
        graph.put("B", Arrays.asList(new Edge("A", 1)));
        graph.put("C", Collections.emptyList()); // isolated room C
        List<String> exits = Collections.singletonList("B");

        Map<String, ExitInfo> result = a2.computeClosestExits(graph, exits);
        ExitInfo infoC = result.get("C");

        assertNotNull(infoC);
        assertNull("C should have no reachable exit", infoC.exitRoom);
        assertEquals("C distance should be infinite", Integer.MAX_VALUE, infoC.distance);
    }

    @Test
    public void testMultipleExitsChooseNearest() {
        A2Interface a2 = new A2();
        Map<String, List<Edge>> graph = new HashMap<>();
        // A -5- B -5- C; A -2- D -2- C
        graph.put("A", Arrays.asList(new Edge("B",5), new Edge("D",2)));
        graph.put("B", Arrays.asList(new Edge("A",5), new Edge("C",5)));
        graph.put("C", Arrays.asList(new Edge("B",5), new Edge("D",2)));
        graph.put("D", Arrays.asList(new Edge("A",2), new Edge("C",2)));
        List<String> exits = Arrays.asList("B", "C");

        Map<String, ExitInfo> result = a2.computeClosestExits(graph, exits);
        // For room A: direct to B cost 5 vs to C via D cost 4 -> choose C
        ExitInfo infoA = result.get("A");
        assertEquals("C", infoA.exitRoom);
        assertEquals(4, infoA.distance);
    }

    @Test
    public void testZoneIsolation() {
        A2Interface a2 = new A2();
        Map<String, List<Edge>> graph = new HashMap<>();
        // Two disconnected zones: {A,B} and {C,D}; exit only in first zone
        graph.put("A", Arrays.asList(new Edge("B",1)));
        graph.put("B", Arrays.asList(new Edge("A",1)));
        graph.put("C", Arrays.asList(new Edge("D",1)));
        graph.put("D", Arrays.asList(new Edge("C",1)));
        List<String> exits = Collections.singletonList("A");

        Map<String, ExitInfo> result = a2.computeClosestExits(graph, exits);
        // Zone 1 reachable
        assertEquals("A", result.get("A").exitRoom);
        assertEquals(0, result.get("A").distance);
        assertEquals("A", result.get("B").exitRoom);
        assertEquals(1, result.get("B").distance);
        // Zone 2 unreachable
        assertNull(result.get("C").exitRoom);
        assertEquals(Integer.MAX_VALUE, result.get("C").distance);
        assertNull(result.get("D").exitRoom);
        assertEquals(Integer.MAX_VALUE, result.get("D").distance);
    }

    @Test
    public void testZoneIsolation2() {
        A2Interface a2 = new A2();
        Map<String, List<Edge>> graph = new HashMap<>();
        // Two disconnected zones: {A,B} and {C,D}; exit only in first zone
        graph.put("A", Arrays.asList(new Edge("B",1)));
        graph.put("B", Arrays.asList(new Edge("A",1)));
        graph.put("C", Arrays.asList(new Edge("D",1)));
        graph.put("D", Arrays.asList(new Edge("C",1)));
        List<String> exits = Arrays.asList("A", "C");

        Map<String, ExitInfo> result = a2.computeClosestExits(graph, exits);
        // Zone 1 reachable
        assertEquals("A", result.get("A").exitRoom);
        assertEquals(0, result.get("A").distance);
        assertEquals("A", result.get("B").exitRoom);
        assertEquals(1, result.get("B").distance);
        // Zone 2 
        assertEquals("C", result.get("C").exitRoom);
        assertEquals(0, result.get("C").distance);
        assertEquals("C", result.get("D").exitRoom);
        assertEquals(1, result.get("D").distance);
    }
}
