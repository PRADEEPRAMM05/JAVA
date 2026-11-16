# Assignment 2 – Task 05 (Q5): Derive a Generic Ranking Component

## 1. Problem Context: The Tournament Leaderboard

Sports, gaming, and academic platforms frequently require sorting and ranking entities (teams, players, submissions) according to multi-tier criteria. For example, in a soccer league table:
1. Primary criterion: **Points** (descending).
2. Secondary criterion (tie-breaker): **Goal Difference** (descending).
3. Tertiary criterion (tie-breaker): **Team Name** (ascending alphabetically).

Rather than writing monolithic sorting logic specific to `Team`, we engineer a reusable, type-safe **`Leaderboard<T>`** component.

---

## 2. Generics and the PECS Principle (`Comparator<? super T>`)

In `Leaderboard<T>`, the sorting policy is injected as:
```java
private final Comparator<? super T> order;
```
This adheres to **PECS (Producer Extends, Consumer Super)**:
- A `Comparator` **consumes** objects of type `T` to compare them.
- Using `? super T` allows a comparator defined for a general supertype (e.g., `EntityWithName`) to rank subtypes (such as `Team` or `Player`), providing maximum architectural flexibility.

---

## 3. Immutability & Defense Against Side Effects

The `ranking()` method must never mutate the caller's internal order:
```java
List<T> ranking() {
    List<T> sortedCopy = new ArrayList<>(items);
    sortedCopy.sort(order);
    return sortedCopy;
}
```
- Modifying `items` directly would cause destructive side effects: ongoing matches or parallel threads reading insertion history would observe unstable state.
- Creating an independent snapshot guarantees referential transparency.

---

## 4. "Complete the Code" Implementation & Verification

### Solution in `LeaderboardDemo.java`:
```java
// Comparator Chaining
Comparator<Team> byRank = Comparator
        .comparingInt((Team t) -> t.points).reversed()
        .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
        .thenComparing(t -> t.name);

Leaderboard<Team> board = new Leaderboard<>(byRank);
board.add(new Team("Panthers", 9, 4));
board.add(new Team("Eagles", 9, 6));
board.add(new Team("Cobras", 6, 1));
board.add(new Team("Bisons", 9, 4));
```

### Verified Terminal Output:
```text
Tournament Leaderboard Ranking:
[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]
```

### Step-by-Step Ranking Trace:
1. `Eagles` has 9 points and $+6$ goal difference $\rightarrow$ **Rank 1**.
2. Both `Bisons` and `Panthers` have 9 points and $+4$ goal difference.
   - Tie broken alphabetically: `"Bisons".compareTo("Panthers") < 0` $\rightarrow$ `Bisons` is **Rank 2**, `Panthers` is **Rank 3**.
3. `Cobras` has 6 points $\rightarrow$ **Rank 4**.

---

## 5. Domain Reusability Demonstration (`RankingDesignTest.java`)

To prove true generic reusability, the exact same `Leaderboard<T>` class was deployed to rank gaming `Player` entities by `score` (descending) and `ping` (ascending):
```text
Ranked Output (Score desc, Ping asc):
  Rank 1: Viper [score=2800, ping=30ms]
  Rank 2: Blaze [score=2500, ping=18ms]
  Rank 3: Shadow [score=2500, ping=45ms]
  Rank 4: Frost [score=1900, ping=12ms]

Internal items list remained unmutated: true
Correctly resolved alphabetical tie-breaker: true
```
