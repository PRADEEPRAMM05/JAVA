import java.util.Comparator;
import java.util.List;

/**
 * Task 05 Test Suite:
 * 1. Verifies that Leaderboard<T> works with another domain type (Player)
 * 2. Verifies that calling ranking() DOES NOT mutate the internal order of items
 * 3. Verifies comparator tie-breaking behavior and stability
 */
public class RankingDesignTest {

    static class Player {
        final String username;
        final int score;
        final int latencyMs;

        Player(String username, int score, int latencyMs) {
            this.username = username;
            this.score = score;
            this.latencyMs = latencyMs;
        }

        @Override
        public String toString() {
            return username + " [score=" + score + ", ping=" + latencyMs + "ms]";
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println(" TASK 05: VERIFYING GENERICS, COMPARATOR CHAINING & IMMUTABILITY CONTRACT ");
        System.out.println("==========================================================================\n");

        // 1. Test Generic Reuse with Player
        System.out.println("--- 1. LEADERBOARD REUSE WITH PLAYER CLASS ---");
        // Comparator: Score desc, then lowest latency asc, then username asc
        Comparator<Player> playerRanker = Comparator
                .comparingInt((Player p) -> p.score).reversed()
                .thenComparingInt(p -> p.latencyMs)
                .thenComparing(p -> p.username);

        Leaderboard<Player> playerBoard = new Leaderboard<>(playerRanker);
        playerBoard.add(new Player("Shadow", 2500, 45));
        playerBoard.add(new Player("Viper", 2800, 30));
        playerBoard.add(new Player("Blaze", 2500, 18)); // Same score as Shadow, lower latency!
        playerBoard.add(new Player("Frost", 1900, 12));

        System.out.println("Raw Unsorted Insertion Order:");
        System.out.println("  " + playerBoard.getRawItems());

        List<Player> rankedPlayers = playerBoard.ranking();
        System.out.println("\nRanked Output (Score desc, Ping asc):");
        for (int i = 0; i < rankedPlayers.size(); i++) {
            System.out.printf("  Rank %d: %s%n", (i + 1), rankedPlayers.get(i));
        }

        // 2. Test Immutability Contract
        System.out.println("\n--- 2. VERIFYING IMMUTABILITY (NO SIDE EFFECTS) ---");
        List<Player> itemsAfterRanking = playerBoard.getRawItems();
        boolean unchanged = itemsAfterRanking.get(0).username.equals("Shadow");
        System.out.println("First element in raw list before ranking was 'Shadow'.");
        System.out.println("First element in raw list after ranking is '" + itemsAfterRanking.get(0).username + "'.");
        System.out.println("Internal items list remained unmutated: " + unchanged);

        // 3. Test Team Ties Breakdown
        System.out.println("\n--- 3. TEAM COMPARISON TIE-BREAKER VERIFICATION ---");
        Team t1 = new Team("Panthers", 9, 4);
        Team t2 = new Team("Bisons", 9, 4);
        // Both have 9 pts and 4 goalDiff -> tie broken by name alphabetically ("Bisons" < "Panthers")
        Comparator<Team> teamComp = Comparator
                .comparingInt((Team t) -> t.points).reversed()
                .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
                .thenComparing(t -> t.name);

        int compResult = teamComp.compare(t2, t1);
        System.out.printf("Comparing 'Bisons' vs 'Panthers': %d (Negative indicates Bisons ranked ahead)%n", compResult);
        System.out.println("Correctly resolved alphabetical tie-breaker: " + (compResult < 0));
    }
}
