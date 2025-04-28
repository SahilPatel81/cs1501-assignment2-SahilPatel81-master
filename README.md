# CS 1501 - Assignment 2 (30 points): Robot Navigation and Signal Compression

## 🤖 Overview
In this assignment, you will implement three methods in the context of **robot movement and command interpretation**. The assignment is designed to integrate concepts from tries (DLB), dynamic programming, graph traversal, and compression (LZW). Each method serves a different purpose:

### 1. `compressCommandPatterns(DLBNode trieRoot, List<List<String>> sequences) → List<Integer>`

**Purpose:**  
Given a pre‑populated DLB trie of known robot commands, compress a batch of new command sequences into a single flat list of integer codes. Internally this uses an LZW‑style approach: at each step you find the longest matching pattern in the trie, emit its code, then insert the extended pattern (match + next command) under the next available code.

**Key Points:**
- Begins with `trieRoot` already containing basic primitives (e.g. `"MOVE"`, `"TURN"`, etc.).
- **Must update** `trieRoot` in‑place by inserting every new extended pattern as you process the sequences.
- Dynamically grows the trie as you read each sequence.
- Returns one combined list of all output codes in traversal order.

---

### 2. `computeClosestExits(Map<String, List<Edge>> roomGraph, List<String> exitRooms) → Map<String, ExitInfo>`

**Purpose:**  
For every room in a robot’s map, determine which designated exit it can reach with the minimum travel cost, and how far it is. Uses a (multi‑source) Dijkstra's seeded by all `exitRooms`.

**Key Points:**
- **Input:** An adjacency map from **room names** to `Edge(toRoom, weight)`, plus a list of valid exit room names.
- **Algorithm:** Run one multi‑source Dijkstra from all exits (distance 0), so that every room’s `dist[room]` is its cost to the nearest exit.
- **Output:** A map from each room name to an `ExitInfo { nearestExitName, distance }`. Rooms unreachable from any exit map to `null` and an infinite (`Integer.MAX_VALUE`) distance.

---

### 3. `recoverSignal(List<String> corrupted, Map<String, List<String>> transitions, String exitRoom) → List<String>`

**Purpose:**  
Reconstruct the most likely actual path a robot took through rooms, given a noisy or partial log of visited room names and knowledge of valid room‑to‑room transitions. This generalizes edit‑distance alignment (insert/delete/substitute) so that corrections are only allowed along real graph edges.

**Key Points:**
- **DP Table:** `dp[i][room]` holds the minimal cost to align the first `i` observed tokens ending in `room`.
- **Operations:**
  - **Match/substitute:** traverse an edge and consume one corrupted token (cost 0 if it matches, 1 otherwise).
  - **Insertion:** traverse an edge without consuming a token (cost 1).
  - **Deletion:** consume a token without moving (cost 1).  This allows “skipping” spurious or missing observations—so even an isolated exit room can be reached by deleting all corrupted tokens if no transitions exist.
- **Result:** Backtrack from `exitRoom` in the DP tables to recover the path with minimal total corrections.  
  - If `exitRoom` exists in the graph, you will always produce at least `["exitRoom"]` (possibly deleting all inputs).  
  - Only if `exitRoom` is entirely absent from `transitions.keySet()` will the method return an empty list.

---

## 🧩 Detailed Explanation: `compressCommandPatterns(...)`

### 📘 Description
This method performs compression of robot command sequences using a **DLB trie** as a codebook and the **LZW** algorithm to generate and reuse repeating command patterns.

#### Method Signature:
```java
List<Integer> compressCommandPatterns(DLBNode trieRoot, List<List<String>> sequences);
```

#### Parameters:
- `trieRoot`: The root node of a DLB trie that may already contain previously known patterns.
- `sequences`: A list of robot command sequences, each represented as a list of string tokens (e.g., `"TURN"`, `"MOVE"`, `"STOP"`).

#### Returns:
A flat list of integer codes representing the compressed sequences using codewords assigned by LZW.  The method **must update** `trieRoot` in‑place by inserting every new extended pattern as you process the sequences.

### 📦 Key Concepts
- **DLB Trie (De La Briandais Trie)**: A trie structure optimized for storing sequences of tokens with sibling and child pointers.
- **LZW (Lempel-Ziv-Welch)**: A dictionary-based compression algorithm that maps sequences to integer codes. New patterns are added to the dictionary dynamically as input is read.

### 📚 DLB Trie Structure

Our implementation uses a **De La Briandais (DLB) trie** to store sequences of robot command **tokens** (e.g. `"MOVE"`, `"TURN"`, `"FORWARD"`). Unlike a classic character‐based trie, each node here represents a whole token and has at most two pointers:

- **`token`**  
  The actual command string for this node.  

- **`code`**  
  An integer code assigned when a complete sequence ends at this node (or `-1` if none).  

- **`child`** →  
  Points **down** to the first node in the next level of the trie. All sequences that extend the current node’s prefix hang off this `child` pointer.

- **`sibling`** →  
  Points **sideways** to the next alternative token at the **same** level. This lets you represent “one of these tokens” without wasting space on a full array.

#### Top‑Level (Root)  
- The **root** of the trie is **not** a dummy holder—it is itself one of the valid first‐level tokens (or `null` if the trie is initially empty).  
- To build your initial set of primitives, you insert each top‐level `DLBNode` into the **sibling chain** starting at `root`.  

#### Traversal Logic  
1. **Lookup**  
   - To match a token at the current position, scan the **sibling chain** via `findChild(head, token)`.  
   - If found, follow its `child` for the next token.  

2. **Insertion**  
   - When extending a matched sequence, check `node.child` for the next token.  
   - If missing, create a new `DLBNode(token)` and **prepend** it to the `child` chain.  

This **“down” + “sideways”** design gives efficient, memory‑compact storage of variable‑length token sequences, and supports fast longest‑prefix matching needed by our compression and pattern‑lookup algorithms.  


### 🧠 How LZW Compression Works
1. The DLB trie is already initialized with the basic commands and possibly some command patterns.
2. For each command sequence:
   - Find the longest prefix in the trie.
   - Emit the code for that prefix.
   - Add the extended pattern (prefix + next token) to the trie with the next available code.
3. Repeat until all sequences are processed.
4. All codes are added in order to a result list and returned.

### 🧪 Example
#### Input:
```java
sequences = [
  ["MOVE", "FORWARD", "MOVE", "FORWARD"],
  ["TURN", "LEFT", "MOVE", "FORWARD"]
]
```

#### Step-by-step Compression:
- The input DLB Trie will contain the single-token basic commands (e.g., `MOVE`, `FORWARD`, `TURN`, `LEFT`) assigned to codes 0–3.
- First token: `"MOVE"` → emit code `0`, add `"MOVE", "FORWARD"` to trie with code `4`
- Next token: `"FORWARD"` → emit code `1`, , add `"FORWARD", "MOVE"` to trie with code `5`
- Next token: `"MOVE", "FORWARD"` → emit code `4`

- Next sequence: 
- `"TURN"` → emit `2`, add `"TURN", "LEFT"` as code `6`
- `"LEFT"` → emit `3`, add `"LEFT", "MOVE"` as code `7`
- Next token: `"MOVE", "FORWARD"` → emit code `4`

#### Output:
```java
[0, 1, 4, 2, 3, 4]
```
(*Note: The actual robot commands used may vary. A basic set of commands such as `"MOVE"`, `"FORWARD"`, etc., will already be included in the initial DLB trie you are given. Your compression logic must build upon these basic primitives without duplicating them in the trie.*)

---

## 🧩 Detailed Explanation: `computeClosestExits(...)` 🚀

**Purpose:**  
For each room 🏠 in the map, determine which designated exit 🚪 it can reach at lowest cost, and report both the exit’s name and the distance.

---

### 🔹 Example 1: Single Exit, Simple Chain

- **Graph:**  
  A 🏠 —1→ B 🏠 —1→ C 🏠 —1→ D 🚪  
- **Exits:** `["D"]`  
- **Expected Output:**  
  - `A → ExitInfo(exit="D", distance=3)`  
  - `B → ExitInfo(exit="D", distance=2)`  
  - `C → ExitInfo(exit="D", distance=1)`  
  - `D → ExitInfo(exit="D", distance=0)`  

---

### 🔹 Example 2: Two Exits, Branching 🌳

- **Graph:**  
  ```
      1        1      1
  A 🏠 → B 🏠 → C 🏠 → D 🚪 (exit1)
   \        
    2       1
     E 🏠 → F 🚪 (exit2)
  ```
- **Exits:** `["D","F"]`  
- **Expected Output:**  
  - `A → ExitInfo(exit="D", distance=3)` or `exit="F", distance=3` (tie‑breaker by list order)  
  - `B → ExitInfo(exit="D", distance=2)`  
  - `C → ExitInfo(exit="D", distance=1)`  
  - `E → ExitInfo(exit="F", distance=1)`  
  - `F → ExitInfo(exit="F", distance=0)`  
  - `D → ExitInfo(exit="D", distance=0)`  

---

### 🔹 Example 3: Disconnected Zone 🚧

- **Graph:**  
  - **Zone 1:** A 🏠 —1→ B 🏠 —1→ C 🚪 (exit)  
  - **Zone 2:** X 🏠 —1→ Y 🏠  (no exit)  
- **Exits:** `["C"]`  
- **Expected Output:**  
  - `A → ExitInfo(exit="C", distance=2)`  
  - `B → ExitInfo(exit="C", distance=1)`  
  - `C → ExitInfo(exit="C", distance=0)`  
  - `X → null` (no path to any exit)  
  - `Y → null` (no path to any exit)  

---
### Hints for Method 2: `computeClosestExits`

- **Multi‑Source Shortest Paths**  
  Start with _all_ exit rooms at distance 0.  This way Dijkstra's will propagate the nearest‑exit distances outward naturally.

- **Tracking Nearest Exits**  
  Whenever you update a neighbor, carry along _which exit_ that path leads to.  Store both `dist[neighbor]` and `nearestExit[neighbor]` so you can report them at the end.

- **Edge‑Case**  
  Rooms that never get a distance assigned (i.e. remain at ∞) simply map to `null`—they can’t reach any exit.

---

## 🧩 Detailed Explanation: `recoverSignal(...)` 🔄

**Purpose:**  
Given a noisy log of room visits and a graph of valid transitions, reconstruct the most plausible path ending at a specified exit 🚪 by minimizing edits (substitute, insert, delete) along real edges.

---

### 🔹 Example 1: Exact Match ✅

- **Graph:** `A → B → C → D`  
- **Exit Room:** `"D"`  
- **Corrupted:** `["A","B","C","D"]`  
- **Expected:** `["A","B","C","D"]` (cost = 0)

---

### 🔹 Example 2: Single Substitution 🔁

- **Graph:** `A → B → C → D`  
- **Exit Room:** `"D"`  
- **Corrupted:** `["A","X","C","D"]` (“X” should be “B”)  
- **Expected:** `["A","B","C","D"]` (cost = 1)

---

### 🔹 Example 3: Insertion ➕

- **Graph:** `A → B → C → D`  
- **Exit Room:** `"D"`  
- **Corrupted:** `["A","B","X","C","D"]` (extra “X”)  
- **Expected:** `["A","B","C","D"]` (cost = 1)  

---

### 🔹 Example 4: Deletion ➖

- **Graph:** `A → B → C`  
- **Exit Room:** `"C"`  
- **Corrupted:** `["A","C"]`  
- **Expected:** `["A","B","C"]` (cost = 1) or `["B","C"]` (cost = 1)

---

### 🔹 Example 5: Skip to Exit Only 🏁

- **Graph:** `A→B→C→A`, and `D` isolated 
- **Exit Room:** `"D"`  
- **Corrupted:** `["X","Y","Z"]` (completely wrong)  
- **Expected:** `["D"]` (delete 2 inputs and substitute D) (cost = 3)

### Hints for Method 3: `recoverSignal`

- **DP Table Layout**  
  Think of a 2D table `dp[i][r]` where:
  - **i** = number of corrupted tokens consumed (0..n)
  - **r** = current room name  
  Each cell stores the **min cost** to align the first `i` observations ending at room `r`.

- **Three Edit Operations**  
  For each `(i, r)` you’ll consider:
  1. **Match/Substitute**  
     Move along a valid edge `p → r` and consume the next token.  
     - Cost = `dp[i–1][p] + (corrupted[i]≠r ? 1 : 0)`
  2. **Insertion**  
     Move along the edge `p → r` _without_ consuming a token.  
     - Cost = `dp[i][p] + 1`
  3. **Deletion**  
     Stay at room `r` and consume the next corrupted token.  
     - Cost = `dp[i–1][r] + 1`

- **Recursive Definition**  
  Let `C(i, r)` = minimal cost for first `i` tokens ending at room `r`. Then:
  ```text
  C(i, r) = min {
    minₚ [ C(i–1, p) + (r == corrupted[i] ? 0 : 1) ],   // match/substitute via p→r
    minₚ [ C(i, p) + 1 ],                              // insertion via p→r
    C(i–1, r) + 1                                      // deletion at r
  }

---

## 📦 Assignment Logistics (Flat Java Structure)

### 📂 Folder Structure
Your repository should exactly follow this flat layout:
```
.
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   └── java
│   │       ├── A2Interface.java
│   │       ├── A2.java          ← **only file you may modify**
│   │       ├── DLBNode.java
│   │       ├── Edge.java
│   │       └── ExitInfo.java
│   └── test
│       └── java
│           ├── CompressCommandPatternsTest.java
│           ├── ComputeClosestExitsTest.java
└──         └── RecoverSignalTest.java
```

---

### 🛠️ How to Compile
From the project root (where `pom.xml` resides), run:
```bash
mvn clean compile
```

---

### ✅ How to Run the Tests
Just like Assignment 1:
```bash
mvn test
```
To run a single test class (e.g., `CompressCommandPatternsTest`):
```bash
mvn -Dtest=CompressCommandPatternsTest test
```

### Debugging Test Cases in VS Code with Test Runner

To debug JUnit test cases in VS Code, follow these steps:

### Prerequisites:
- Install the **Java Extension Pack** in VS Code.
- You may need to install version **0.40.0** of the **Test Runner for Java** extension if debugging options do not appear.

#### Steps:
1. Open the test file (e.g., `CompressCommandPatternsTest.java`) in the editor.
2. Set breakpoints by clicking on the gutter next to the line numbers.
3. Right-click on the gutter next to the line number of the test method name and select **Debug Test**.
4. Use the debug toolbar to step through code, inspect variables, and view call stacks.

This allows you to easily verify internal state, control flow, and ensure correctness of your implementation.

---

### ☁️ Using GitHub Codespaces
1. Click **Code → Open with Codespaces** in GitHub.
2. Maven is pre-installed; use `mvn clean compile` and `mvn test`.

---

### 📥 Installing Maven Locally
- **macOS (Homebrew):** `brew install maven`  
- **Ubuntu/Debian:** `sudo apt update && sudo apt install maven`  
- **Windows (Chocolatey):** `choco install maven`  
Verify installation with `mvn -v`.

---

### 🚧 Submission Rules
- **Only** edit `src/main/java/A2.java`.  
- **Do not** depend on changes to other files. Only `A2.java` will be graded.  
- Submit the GitHub repository; Gradescope will import it automatically.

---

### ✅ Final Checklist
- [ ] `A2.java` implements methods in `A2Interface`.  
- [ ] `mvn clean compile` completes without errors.  
- [ ] `mvn test` reports as few failures as you can.  
- [ ] Only `A2.java` was modified.

Happy coding! 🤖🚀

