# Assignment 3 – Task 06 (Q6): Deadlock and Inter-Thread Communication

## 1. Problem Context: Mess Tokens & Digital Wallet Transfers

This task explores two core concurrency challenges:
1. **Producer-Consumer Coordination**: A mess cook produces tokens into a finite buffer (capacity 5); student threads consume them. Threads must coordinate without wasteful CPU busy-spinning.
2. **Deadlock in Digital Wallet Transfers**: When two accounts concurrently transfer funds to each other, opposite lock acquisition order creates a fatal deadlock.

---

## 2. Inter-Thread Communication: `wait()` and `notifyAll()`

In `TokenCounter.java`:
```java
public synchronized void put(int token) throws InterruptedException {
    while (count == buffer.length) {
        wait(); // Releases monitor lock, pauses in WAITING state
    }
    buffer[in] = token;
    in = (in + 1) % buffer.length;
    count++;
    notifyAll(); // Wakes up consumers
}
```

### Why a `while` Loop is Mandatory (Spurious Wakeups):
- Java thread monitors are subject to **spurious wakeups** (a thread may wake up without any `notify()` signal).
- Furthermore, under `notifyAll()`, multiple threads wake up, but only **one** can consume the single available slot.
- Using `if (count == buffer.length)` would cause buffer overflow when a second thread wakes up and proceeds without re-verifying the condition. A `while` loop re-checks the predicate upon waking.

---

## 3. Coffman's 4 Deadlock Conditions & Prevention

Deadlock occurs if and only if all four **Coffman Conditions** hold simultaneously:
1. **Mutual Exclusion**: Only one thread can hold an account lock at a time.
2. **Hold and Wait**: Thread 1 holds Account A while waiting for Account B.
3. **No Preemption**: Locks cannot be forcefully confiscated from threads.
4. **Circular Wait**: Thread 1 waits for Account B (held by Thread 2), while Thread 2 waits for Account A (held by Thread 1).

### The Solution: Global Lock Ordering
We eliminate the **Circular Wait** condition by enforcing a strict global ordering on lock acquisition:
```java
// Always acquire lock on smaller ID first, regardless of transfer direction!
WalletAccount firstLock = from.id < to.id ? from : to;
WalletAccount secondLock = from.id < to.id ? to : from;

synchronized (firstLock) {
    synchronized (secondLock) {
        from.balance -= amount;
        to.balance += amount;
    }
}
```
Because both threads acquire Account 101 before Account 202, a circular dependency is mathematically impossible.

---

## 4. Execution Output

### `TokenCounter.java`:
```text
Sum of tokens collected: 210 (expected 210)
```
*(Sum of tokens $1 + 2 + \dots + 20 = \frac{20 \times 21}{2} = 210$)*

### `WalletDeadlockDemo.java`:
```text
All 1,000 transfers completed successfully without deadlocking!
Final Balances -> Arun: Rs. 5000.00 | Divya: Rs. 8000.00
Total System Money Preserved: Rs. 13000.0
```
