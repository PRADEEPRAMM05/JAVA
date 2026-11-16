# Task 03: Design Control Logic and Edge Condition Handling

## 1. Control Logic Design

The core operational workflow of `ParkingFacility` governs:
1. **Slot Allocation**: Scanning multi-level floors and identifying the first available slot.
2. **Slot Release**: Validating boundary coordinates and releasing an occupied slot back to the free pool.

### Allocation Algorithm:
- **Search Strategy**: A nested sequential traversal over floors ($0 \le f < \text{floors}$) and slots within each floor ($0 \le s < \text{slots}[f]$).
- **Early Termination**: As soon as `slots[f][s] == null` is encountered, the slot is immediately claimed (`slots[f][s] = vehicleNo`), and the coordinates `new int[]{f, s}` are returned.
- **Exhaustion**: If both loops terminate without finding an empty slot, the facility is strictly full, returning `null`.

### Release Algorithm & Defensive Edge Checks:
To prevent array out-of-bounds crashes, memory corruption, and duplicate release inconsistencies:
1. **Floor Bounds Check**: $f < 0 \lor f \ge \text{slots.length} \implies \text{return } \mathbf{false}$.
2. **Slot Bounds Check**: $s < 0 \lor s \ge \text{slots}[f].length \implies \text{return } \mathbf{false}$.
3. **Idempotence / Double-Release Check**: If `slots[f][s] == null`, the slot is already vacant $\implies \text{return } \mathbf{false}$.
4. **Successful Reset**: Otherwise, reset `slots[f][s] = null` and return `true`.

---

## 2. Complete the Code Implementation

Below is the verified, completed `ParkingFacility.java` source:

```java
public class ParkingFacility {
    // slots[floor][slot] holds a vehicle number, or null if the slot is free
    private final String[][] slots;

    public ParkingFacility(int[] slotsPerFloor) {
        slots = new String[slotsPerFloor.length][];
        for (int f = 0; f < slotsPerFloor.length; f++) {
            slots[f] = new String[slotsPerFloor[f]];
        }
    }

    /** Returns {floor, slot} of the first free slot, or null if it is full. */
    public int[] allocate(String vehicleNo) {
        // TODO 1: Search floor by floor and slot by slot; place vehicleNo in the first
        // free slot and return {floor, slot}
        if (vehicleNo == null) {
            return null;
        }
        for (int f = 0; f < slots.length; f++) {
            for (int s = 0; s < slots[f].length; s++) {
                if (slots[f][s] == null) {
                    slots[f][s] = vehicleNo;
                    return new int[]{f, s};
                }
            }
        }
        return null;
    }

    /** Frees a slot. Returns false if the position is invalid or already free. */
    public boolean release(int floor, int slot) {
        // TODO 2: Validate floor and slot, free the slot and return true; otherwise
        // return false
        if (floor < 0 || floor >= slots.length) {
            return false;
        }
        if (slot < 0 || slot >= slots[floor].length) {
            return false;
        }
        if (slots[floor][slot] == null) {
            return false; // Already free
        }
        slots[floor][slot] = null;
        return true;
    }

    public static void main(String[] args) {
        // floor 0 has 2 slots, floor 1 has 1 slot
        ParkingFacility p = new ParkingFacility(new int[]{2, 1});
        System.out.println(java.util.Arrays.toString(p.allocate("TN01AB1111")));
        System.out.println(java.util.Arrays.toString(p.allocate("TN02CD2222")));
        System.out.println(java.util.Arrays.toString(p.allocate("TN03EF3333")));
        System.out.println(java.util.Arrays.toString(p.allocate("TN04GH4444")));
        System.out.println(p.release(0, 1) + " " + p.release(0, 1) + " " + p.release(5, 0));
        System.out.println(java.util.Arrays.toString(p.allocate("TN04GH4444")));
    }
}
```

---

## 3. Compilation & Verification

### Commands:
```bash
javac ParkingFacility.java
java ParkingFacility
```

### Verified Terminal Output:
```text
[0, 0]
[0, 1]
[1, 0]
null
true false false
[0, 1]
```

### Output Trace Explanation:
- `allocate("TN01AB1111")` occupies floor 0, slot 0 $\rightarrow$ `[0, 0]`.
- `allocate("TN02CD2222")` occupies floor 0, slot 1 $\rightarrow$ `[0, 1]`.
- `allocate("TN03EF3333")` occupies floor 1, slot 0 $\rightarrow$ `[1, 0]`.
- Total facility capacity (3 slots) is now saturated. `allocate("TN04GH4444")` correctly returns `null`.
- `release(0, 1)`: frees occupied slot $\rightarrow$ returns `true`.
- Second `release(0, 1)`: slot is now vacant $\rightarrow$ returns `false`.
- `release(5, 0)`: floor index 5 exceeds bounds $\rightarrow$ returns `false`.
- Resulting string `"true false false"` matches specification.
- Subsequent `allocate("TN04GH4444")` immediately claims the freed slot $\rightarrow$ returns `[0, 1]`.

---

## 4. Complexity Analysis

| Operation | Time Complexity | Space Complexity | Description |
| :--- | :--- | :--- | :--- |
| `allocate()` | **Best Case: $O(1)$**<br>**Worst Case: $O(N)$** ($N = \sum \text{slots}[f]$) | **$O(1)$** auxiliary space | First free slot search scans until an empty slot is encountered or all $N$ slots are checked. |
| `release()` | **$O(1)$** strictly | **$O(1)$** auxiliary space | Direct array element indexing via `slots[floor][slot]`. |
