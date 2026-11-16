import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Task 05: Complete the Code - Generic Ranking Component.
 * Demonstrates:
 * 1. Generic class Leaderboard<T>
 * 2. PECS Principle: Comparator<? super T> allows flexible comparators
 * 3. Chained comparators (points desc, goalDiff desc, name asc)
 * 4. Immutability: ranking() returns a sorted copy without modifying internal items
 */
class Team {
    final String name;
    final int points;
    final int goalDiff;

    Team(String name, int points, int goalDiff) {
        this.name = name;
        this.points = points;
        this.goalDiff = goalDiff;
    }

    @Override
    public String toString() {
        return name + "(" + points + "," + goalDiff + ")";
    }
}

class Leaderboard<T> {
    private final List<T> items = new ArrayList<>();
    private final Comparator<? super T> order;

    Leaderboard(Comparator<? super T> order) {
        this.order = order;
    }

    void add(T item) {
        items.add(item);
    }

    List<T> ranking() {
        // TODO 1: Return a new sorted copy of items using order, without modifying items
        List<T> sortedCopy = new ArrayList<>(items);
        sortedCopy.sort(order);
        return sortedCopy;
    }

    // Helper to verify internal items remain unchanged
    List<T> getRawItems() {
        return new ArrayList<>(items);
    }
}

public class LeaderboardDemo {
    public static void main(String[] args) {
        // TODO 2: Build the comparator: points (high first), then goal difference (high
        // first), then name (A-Z)
        Comparator<Team> byRank = Comparator
                .comparingInt((Team t) -> t.points).reversed()
                .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
                .thenComparing(t -> t.name);

        Leaderboard<Team> board = new Leaderboard<>(byRank);
        board.add(new Team("Panthers", 9, 4));
        board.add(new Team("Eagles", 9, 6));
        board.add(new Team("Cobras", 6, 1));
        board.add(new Team("Bisons", 9, 4));

        System.out.println("Tournament Leaderboard Ranking:");
        System.out.println(board.ranking());
    }
}
