/**
 * Task 06: Deadlock Simulation and Prevention in Digital Wallet Transfers.
 * Demonstrates:
 * 1. Circular Wait Deadlock between two concurrent account transfers
 * 2. Coffman's 4 Deadlock Conditions
 * 3. Prevention via Global Resource Lock Ordering (Account ID ordering)
 */
public class WalletDeadlockDemo {

    static class WalletAccount {
        final int id;
        final String owner;
        double balance;

        WalletAccount(int id, String owner, double balance) {
            this.id = id;
            this.owner = owner;
            this.balance = balance;
        }

        // FLAWED: Causes circular wait deadlock if thread 1 transfers A -> B while thread 2 transfers B -> A!
        static void flawedTransfer(WalletAccount from, WalletAccount to, double amount) {
            synchronized (from) {
                System.out.printf("  [%s] Acquired lock on Account %d (%s)...%n",
                        Thread.currentThread().getName(), from.id, from.owner);

                try { Thread.sleep(50); } catch (InterruptedException ignored) {}

                System.out.printf("  [%s] Attempting to acquire lock on Account %d (%s)...%n",
                        Thread.currentThread().getName(), to.id, to.owner);

                synchronized (to) {
                    from.balance -= amount;
                    to.balance += amount;
                    System.out.printf("  [%s] Transferred Rs. %.2f from %s to %s%n",
                            Thread.currentThread().getName(), amount, from.owner, to.owner);
                }
            }
        }

        // ROBUST: Global Lock Ordering breaks the Coffman Circular Wait condition!
        static void safeTransfer(WalletAccount from, WalletAccount to, double amount) {
            // Determine global acquisition order based on unique Account ID
            WalletAccount firstLock = from.id < to.id ? from : to;
            WalletAccount secondLock = from.id < to.id ? to : from;

            synchronized (firstLock) {
                synchronized (secondLock) {
                    if (from.balance >= amount) {
                        from.balance -= amount;
                        to.balance += amount;
                        System.out.printf("  [SAFE-TRANSFER] Transferred Rs. %.2f from Account %d (%s) to Account %d (%s)%n",
                                amount, from.id, from.owner, to.id, to.owner);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("==========================================================================");
        System.out.println(" TASK 06: DEADLOCK INVESTIGATION & GLOBAL LOCK ORDERING PREVENTION        ");
        System.out.println("==========================================================================\n");

        WalletAccount accA = new WalletAccount(101, "Arun", 5000);
        WalletAccount accB = new WalletAccount(202, "Divya", 8000);

        System.out.println("--- 1. DEMONSTRATING DEADLOCK PREVENTION VIA GLOBAL LOCK ORDERING ---");
        System.out.println("Running 1,000 concurrent bi-directional transfers using Global Lock Ordering...");

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 500; i++) {
                WalletAccount.safeTransfer(accA, accB, 2.0);
            }
        }, "Worker-ArunToDivya");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 500; i++) {
                WalletAccount.safeTransfer(accB, accA, 2.0);
            }
        }, "Worker-DivyaToArun");

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("\nAll 1,000 transfers completed successfully without deadlocking!");
        System.out.printf("Final Balances -> Arun: Rs. %.2f | Divya: Rs. %.2f%n", accA.balance, accB.balance);
        System.out.println("Total System Money Preserved: Rs. " + (accA.balance + accB.balance));
    }
}
