import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Task 02 (Q2): When is ArrayList an Appropriate Engineering Choice?
 * Investigates performance of ArrayList vs. LinkedList vs. ArrayDeque
 * across 4 core playlist operations:
 * 1. Append to end
 * 2. Insert at head (position 0)
 * 3. Insert / Remove in middle (position size / 2)
 * 4. Random access lookup (get(k))
 */
public class PlaylistBenchmark {

    static class Song {
        final int id;
        final String title;

        Song(int id) {
            this.id = id;
            this.title = "Track_" + id;
        }
    }

    private static void runBenchmark(int size, int operations) {
        System.out.println("========================================================================================");
        System.out.printf("PLAYLIST BENCHMARK: Size = %,d songs | Operations per test = %,d%n", size, operations);
        System.out.println("========================================================================================");

        // Prepopulate lists
        List<Song> arrayList = new ArrayList<>(size);
        List<Song> linkedList = new LinkedList<>();
        Deque<Song> arrayDeque = new ArrayDeque<>(size);

        for (int i = 0; i < size; i++) {
            Song s = new Song(i);
            arrayList.add(s);
            linkedList.add(s);
            arrayDeque.add(s);
        }

        // 1. Append to End
        long start = System.nanoTime();
        List<Song> alAppend = new ArrayList<>();
        for (int i = 0; i < operations; i++) alAppend.add(new Song(i));
        double alAppendMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        List<Song> llAppend = new LinkedList<>();
        for (int i = 0; i < operations; i++) llAppend.add(new Song(i));
        double llAppendMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        Deque<Song> adAppend = new ArrayDeque<>();
        for (int i = 0; i < operations; i++) adAppend.addLast(new Song(i));
        double adAppendMs = (System.nanoTime() - start) / 1_000_000.0;

        // 2. Insert at Head (Index 0)
        start = System.nanoTime();
        List<Song> alHead = new ArrayList<>(arrayList);
        for (int i = 0; i < operations; i++) alHead.add(0, new Song(90000 + i));
        double alHeadMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        List<Song> llHead = new LinkedList<>(linkedList);
        for (int i = 0; i < operations; i++) llHead.add(0, new Song(90000 + i));
        double llHeadMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        Deque<Song> adHead = new ArrayDeque<>(arrayDeque);
        for (int i = 0; i < operations; i++) adHead.addFirst(new Song(90000 + i));
        double adHeadMs = (System.nanoTime() - start) / 1_000_000.0;

        // 3. Random Access get(k)
        Random rng = new Random(42);
        int[] indices = new int[operations];
        for (int i = 0; i < operations; i++) indices[i] = rng.nextInt(size);

        long checksumAl = 0;
        start = System.nanoTime();
        for (int idx : indices) checksumAl += arrayList.get(idx).id;
        double alGetMs = (System.nanoTime() - start) / 1_000_000.0;

        long checksumLl = 0;
        start = System.nanoTime();
        // Limit get() on huge LinkedList to prevent timeout
        int llGetOps = (size > 10_000) ? Math.min(operations, 500) : operations;
        for (int i = 0; i < llGetOps; i++) {
            checksumLl += linkedList.get(indices[i]).id;
        }
        double llGetMs = ((System.nanoTime() - start) / 1_000_000.0) * (operations / (double) llGetOps);

        // 4. Middle Insertion (Index size / 2)
        start = System.nanoTime();
        List<Song> alMid = new ArrayList<>(arrayList);
        for (int i = 0; i < operations; i++) alMid.add(alMid.size() / 2, new Song(80000 + i));
        double alMidMs = (System.nanoTime() - start) / 1_000_000.0;

        start = System.nanoTime();
        List<Song> llMid = new LinkedList<>(linkedList);
        int llMidOps = (size > 10_000) ? Math.min(operations, 500) : operations;
        for (int i = 0; i < llMidOps; i++) llMid.add(llMid.size() / 2, new Song(80000 + i));
        double llMidMs = ((System.nanoTime() - start) / 1_000_000.0) * (operations / (double) llMidOps);

        System.out.printf("%-24s | %-16s | %-16s | %-16s%n",
                "Operation (" + operations + " ops)", "ArrayList (ms)", "LinkedList (ms)", "ArrayDeque (ms)");
        System.out.println("----------------------------------------------------------------------------------------");
        System.out.printf("%-24s | %-16.3f | %-16.3f | %-16.3f%n", "1. Append to End", alAppendMs, llAppendMs, adAppendMs);
        System.out.printf("%-24s | %-16.3f | %-16.3f | %-16.3f%n", "2. Insert at Head", alHeadMs, llHeadMs, adHeadMs);
        System.out.printf("%-24s | %-16.3f | %-16.3f | %-16s%n", "3. Random Access get(k)", alGetMs, llGetMs, "N/A (No index)");
        System.out.printf("%-24s | %-16.3f | %-16.3f | %-16s%n", "4. Insert in Middle", alMidMs, llMidMs, "N/A (No index)");
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println("########################################################################################");
        System.out.println("   TASK 02 BENCHMARK: ARRAYLIST vs LINKEDLIST vs ARRAYDEQUE (PLAYLIST EDITOR)           ");
        System.out.println("########################################################################################\n");

        // Small Playlist (50 songs, typical user queue)
        runBenchmark(50, 1_000);

        // Medium Playlist (2,000 songs, large personal album library)
        runBenchmark(2_000, 1_000);

        // Large Playlist (50,000 songs, streaming database catalog)
        runBenchmark(50_000, 1_000);
    }
}
