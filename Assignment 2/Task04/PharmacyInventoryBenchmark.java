import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Task 04 (Q4): Choosing a Map for a Pharmacy Inventory.
 * Evaluates HashMap, TreeMap, and LinkedHashMap against 3 operational requirements:
 * 1. Fast Dispensing: O(1) Barcode lookup
 * 2. Range Query & Natural Key Ordering: TreeMap O(log n)
 * 3. FIFO Restocking Audit: LinkedHashMap O(1) with insertion-order preservation
 */
public class PharmacyInventoryBenchmark {

    static class Medicine {
        final String code;
        final String name;
        int stock;

        Medicine(String code, String name, int stock) {
            this.code = code;
            this.name = name;
            this.stock = stock;
        }

        @Override
        public String toString() {
            return code + ":" + name + "(" + stock + ")";
        }
    }

    private static List<Medicine> generateMedicines(int count) {
        List<Medicine> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            // Unsorted alphanumeric codes to test sorting behavior
            String code = String.format("MED-%06d", (i * 7919) % 1_000_000);
            list.add(new Medicine(code, "Drug_" + i, 50 + (i % 100)));
        }
        return list;
    }

    private static void verifyOrderingBehavior() {
        System.out.println("--- 1. VERIFYING ITERATION ORDER ACROSS MAP IMPLEMENTATIONS ---");
        List<Medicine> sample = Arrays.asList(
                new Medicine("MED-300", "Paracetamol", 120),
                new Medicine("MED-100", "Amoxicillin", 80),
                new Medicine("MED-200", "Cetirizine", 200)
        );

        Map<String, Medicine> hashMap = new HashMap<>();
        Map<String, Medicine> treeMap = new TreeMap<>();
        Map<String, Medicine> linkedMap = new LinkedHashMap<>();

        for (Medicine m : sample) {
            hashMap.put(m.code, m);
            treeMap.put(m.code, m);
            linkedMap.put(m.code, m);
        }

        System.out.println("Insertion Order (Original): [MED-300, MED-100, MED-200]");
        System.out.println("HashMap Iteration Keys     : " + hashMap.keySet() + " (Unpredictable Hash Buckets)");
        System.out.println("TreeMap Iteration Keys     : " + treeMap.keySet() + " (Strict Natural Sorted Order)");
        System.out.println("LinkedHashMap Iteration Keys: " + linkedMap.keySet() + " (Guaranteed FIFO Insertion Order)\n");
    }

    private static void runPerformanceBenchmark(int datasetSize, int lookupOperations) {
        System.out.println("========================================================================================");
        System.out.printf("PHARMACY BENCHMARK: %,d medicines | %,d barcode lookups%n", datasetSize, lookupOperations);
        System.out.println("========================================================================================");

        List<Medicine> dataset = generateMedicines(datasetSize);

        // 1. Insertion Benchmark
        long start = System.nanoTime();
        Map<String, Medicine> hashMap = new HashMap<>(datasetSize);
        for (Medicine m : dataset) hashMap.put(m.code, m);
        double hashInsertMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        Map<String, Medicine> treeMap = new TreeMap<>();
        for (Medicine m : dataset) treeMap.put(m.code, m);
        double treeInsertMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        Map<String, Medicine> linkedMap = new LinkedHashMap<>(datasetSize);
        for (Medicine m : dataset) linkedMap.put(m.code, m);
        double linkedInsertMs = (System.nanoTime() - start) / 1_000_000.0;

        // 2. Barcode Lookup (Point of Sale Dispensing)
        Random rng = new Random(1337);
        String[] queryKeys = new String[lookupOperations];
        for (int i = 0; i < lookupOperations; i++) {
            queryKeys[i] = dataset.get(rng.nextInt(datasetSize)).code;
        }

        long checksumHash = 0;
        start = System.nanoTime();
        for (String k : queryKeys) checksumHash += hashMap.get(k).stock;
        double hashLookupMs = (System.nanoTime() - start) / 1_000_000.0;

        long checksumTree = 0;
        start = System.nanoTime();
        for (String k : queryKeys) checksumTree += treeMap.get(k).stock;
        double treeLookupMs = (System.nanoTime() - start) / 1_000_000.0;

        long checksumLinked = 0;
        start = System.nanoTime();
        for (String k : queryKeys) checksumLinked += linkedMap.get(k).stock;
        double linkedLookupMs = (System.nanoTime() - start) / 1_000_000.0;

        // 3. Full Inventory Traversal (Shelf Audit)
        start = System.nanoTime();
        long hashSum = 0;
        for (Medicine m : hashMap.values()) hashSum += m.stock;
        double hashIterMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        long treeSum = 0;
        for (Medicine m : treeMap.values()) treeSum += m.stock;
        double treeIterMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        long linkedSum = 0;
        for (Medicine m : linkedMap.values()) linkedSum += m.stock;
        double linkedIterMs = (System.nanoTime() - start) / 1_000_000.0;

        System.out.printf("%-20s | %-16s | %-16s | %-16s%n",
                "Metric", "HashMap", "TreeMap", "LinkedHashMap");
        System.out.println("----------------------------------------------------------------------------------------");
        System.out.printf("%-20s | %-16.3f | %-16.3f | %-16.3f%n", "Insert Time (ms)", hashInsertMs, treeInsertMs, linkedInsertMs);
        System.out.printf("%-20s | %-16.3f | %-16.3f | %-16.3f%n", "Lookup Time (ms)", hashLookupMs, treeLookupMs, linkedLookupMs);
        System.out.printf("%-20s | %-16.3f | %-16.3f | %-16.3f%n", "Iteration Time (ms)", hashIterMs, treeIterMs, linkedIterMs);
        System.out.printf("%-20s | %-16s | %-16s | %-16s%n", "Time Complexity", "O(1) hash", "O(log n) tree", "O(1) hash + list");
        System.out.printf("%-20s | %-16s | %-16s | %-16s%n", "Memory per Entry", "~32 bytes", "~40 bytes", "~48 bytes");
        System.out.printf("Speedup: HashMap lookup is %.1fx faster than TreeMap!%n%n", treeLookupMs / hashLookupMs);
    }

    public static void main(String[] args) {
        System.out.println("########################################################################################");
        System.out.println("   TASK 04 BENCHMARK: HASHMAP vs TREEMAP vs LINKEDHASHMAP (PHARMACY INVENTORY)          ");
        System.out.println("########################################################################################\n");

        verifyOrderingBehavior();
        runPerformanceBenchmark(10_000, 50_000);
        runPerformanceBenchmark(50_000, 100_000);
    }
}
