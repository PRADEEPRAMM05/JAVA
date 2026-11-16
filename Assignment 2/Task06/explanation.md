# Assignment 2 – Task 06 (Q6): Priority, Order, and Fairness in a Queue

## 1. Problem Context: Emergency Room Clinical Triage

In an emergency medical department, arriving patients are evaluated and assigned an Emergency Severity Index (ESI) rating:
- **Severity 1 (Resuscitation)**: Immediate life-threatening trauma (cardiac arrest, severe haemorrhage).
- **Severity 2 (Emergent)**: High-risk conditions (chest pain, acute stroke).
- **Severity 3 (Urgent)**: Severe symptoms requiring multiple hospital resources (fractures, abdominal pain).
- **Severity 4 (Less Urgent)**: Minor injury requiring a single diagnostic or treatment.
- **Severity 5 (Non-urgent)**: Minor cuts, suture removal, or prescription renewal.

### The Operational Challenge:
The ER doctor must always treat the most critical patient first (lowest severity number). However, among patients with the **exact same severity rating**, medical ethics and hospital regulations strictly mandate **First-Come, First-Served (FCFS)** ordering.

---

## 2. Java `PriorityQueue` & Heap Instability Analysis

Java's `PriorityQueue<E>` is implemented as an array-based **binary min-heap**:
- Insertion (`offer` / `add`): $O(\log N)$ via sift-up.
- Retrieval (`poll`): $O(\log N)$ via sift-down.

### The Binary Heap Instability Hazard:
A binary heap is **NOT a stable sorting structure**:
- When two elements have equal keys (e.g., both are Severity 3), the binary heap makes no guarantee that the one inserted earlier will be placed ahead in the tree or array.
- During node swaps in `siftUp()` and `siftDown()`, newer elements can leapfrog older elements with the same priority.
- **The Solution**: An explicit monotonic sequence counter (`counter++`) must be recorded at admission time and chained into the comparator:
  ```java
  Comparator.comparingInt((Patient p) -> p.severity)
            .thenComparingLong(p -> p.arrival);
  ```

---

## 3. "Complete the Code" Implementation & Verification (`Triage.java`)

```java
public class Triage {
    private long counter = 0;

    // TODO 1: Create PriorityQueue serving lowest severity first, then earliest arrival
    private final PriorityQueue<Patient> queue = new PriorityQueue<>(
            Comparator.comparingInt((Patient p) -> p.severity)
                    .thenComparingLong(p -> p.arrival)
    );

    void admit(String name, int severity) {
        queue.add(new Patient(name, severity, counter++));
    }

    Patient next() {
        return queue.poll();
    }
}
```

### Verified Terminal Output:
```text
Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)
```

### Step-by-Step Execution Verification:
1. `Meena(S1)` and `Kumar(S1)` have highest priority (Severity 1). Because Meena arrived before Kumar, Meena is treated first, followed by Kumar.
2. `Lata(S2)` is treated next (Severity 2).
3. `Ravi(S3)` and `John(S3)` both have Severity 3. Ravi arrived before John, so Ravi is treated first.
4. `Asha(S5)` is treated last.
Every patient was treated in strict adherence to medical urgency and arrival fairness.

---

## 4. Empirical Investigation: Starvation Analysis (`TriageInvestigation.java`)

Our empirical investigation (`TriageInvestigation.java`) revealed two critical vulnerabilities:

### 1. Unstable Queue Failure without Arrival Counter:
Without the arrival tie-breaker, admitting `Patient_A(S3)` followed by `Patient_B(S3)` resulted in `Patient_B` being polled before `Patient_A`, violating FIFO fairness.

### 2. Low-Priority Starvation (Indefinite Delay):
In a busy urban hospital, critical trauma patients (S1 and S2) arrive continuously. Under a pure `PriorityQueue`:
- As long as new S1/S2 patients arrive, lower-priority patients (S4/S5) will wait **indefinitely**.
- In extreme cases, a non-urgent patient's medical condition may deteriorate into an emergency while waiting.
- **Diagnosis**: Pure heap-based priority scheduling lacks **aging and fairness**. This diagnostic limitation forms the exact problem solved in Task 07 (Day-4 Design Challenge).
