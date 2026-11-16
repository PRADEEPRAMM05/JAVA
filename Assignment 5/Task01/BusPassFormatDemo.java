package task01;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * BusPassFormatDemo
 * Demonstrates delimited text vs DataOutputStream binary format for
 * University Transport Office Bus-Pass records with robust damage detection.
 */
public class BusPassFormatDemo {

    public static class BusPass {
        public final String passId;
        public final String regNo;
        public final String name;
        public final int routeNumber;
        public final String boardingStop;
        public final String validityDate;
        public final double amountPaid;

        public BusPass(String passId, String regNo, String name, int routeNumber,
                       String boardingStop, String validityDate, double amountPaid) {
            this.passId = passId;
            this.regNo = regNo;
            this.name = name;
            this.routeNumber = routeNumber;
            this.boardingStop = boardingStop;
            this.validityDate = validityDate;
            this.amountPaid = amountPaid;
        }

        @Override
        public String toString() {
            return String.format("[%s, %s, %s, Route %d, Stop: %s, Valid: %s, ₹%.2f]",
                    passId, regNo, name, routeNumber, boardingStop, validityDate, amountPaid);
        }
    }

    // =========================================================================
    // 1. DELIMITED TEXT FILE STORE (RFC 4180 Escaped)
    // =========================================================================
    public static class TextPassStore {
        private final Path filePath;

        public TextPassStore(Path filePath) {
            this.filePath = filePath;
        }

        public synchronized void addPass(BusPass pass) throws IOException {
            // Escape names containing commas or quotes
            String escapedName = pass.name.contains(",") ? "\"" + pass.name.replace("\"", "\"\"") + "\"" : pass.name;
            String line = String.join(",",
                    pass.passId,
                    pass.regNo,
                    escapedName,
                    String.valueOf(pass.routeNumber),
                    pass.boardingStop,
                    pass.validityDate,
                    String.valueOf(pass.amountPaid)
            );

            try (BufferedWriter bw = Files.newBufferedWriter(filePath,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                bw.write(line);
                bw.newLine();
            }
        }

        public BusPass findPassById(String targetId) throws IOException {
            if (!Files.exists(filePath)) return null;

            try (BufferedReader br = Files.newBufferedReader(filePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    BusPass pass = parseLine(line);
                    if (pass != null && pass.passId.equalsIgnoreCase(targetId)) {
                        return pass;
                    }
                }
            }
            return null;
        }

        public List<BusPass> listPassesByRoute(int targetRoute) throws IOException {
            List<BusPass> list = new ArrayList<>();
            if (!Files.exists(filePath)) return list;

            try (BufferedReader br = Files.newBufferedReader(filePath)) {
                String line;
                while ((line = br.readLine()) != null) {
                    BusPass pass = parseLine(line);
                    if (pass != null && pass.routeNumber == targetRoute) {
                        list.add(pass);
                    }
                }
            }
            return list;
        }

        private BusPass parseLine(String line) {
            if (line == null || line.trim().isEmpty()) return null;
            // Handle quotes and commas
            List<String> tokens = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            boolean inQuotes = false;
            for (int i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == '\"') {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    tokens.add(sb.toString().trim());
                    sb.setLength(0);
                } else {
                    sb.append(c);
                }
            }
            tokens.add(sb.toString().trim());

            if (tokens.size() != 7) {
                // Damaged line detected (wrong field count)
                return null;
            }

            try {
                int route = Integer.parseInt(tokens.get(3));
                double amt = Double.parseDouble(tokens.get(6));
                return new BusPass(tokens.get(0), tokens.get(1), tokens.get(2),
                        route, tokens.get(4), tokens.get(5), amt);
            } catch (NumberFormatException e) {
                // Damaged line detected (corrupted numbers)
                return null;
            }
        }
    }

    // =========================================================================
    // 2. BINARY FILE STORE (DataOutputStream / DataInputStream)
    // =========================================================================
    public static class BinaryPassStore {
        private final File file;

        public BinaryPassStore(File file) {
            this.file = file;
        }

        public synchronized void addPass(BusPass pass) throws IOException {
            try (DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file, true)))) {
                dos.writeUTF(pass.passId);
                dos.writeUTF(pass.regNo);
                dos.writeUTF(pass.name);
                dos.writeInt(pass.routeNumber);
                dos.writeUTF(pass.boardingStop);
                dos.writeUTF(pass.validityDate);
                dos.writeDouble(pass.amountPaid);
                dos.flush();
            }
        }

        public BusPass findPassById(String targetId) throws IOException {
            if (!file.exists()) return null;

            try (DataInputStream dis = new DataInputStream(
                    new BufferedInputStream(new FileInputStream(file)))) {
                while (dis.available() > 0) {
                    String id = dis.readUTF();
                    String reg = dis.readUTF();
                    String name = dis.readUTF();
                    int route = dis.readInt();
                    String stop = dis.readUTF();
                    String val = dis.readUTF();
                    double amt = dis.readDouble();

                    if (id.equalsIgnoreCase(targetId)) {
                        return new BusPass(id, reg, name, route, stop, val, amt);
                    }
                }
            } catch (EOFException e) {
                // Reached end of stream
            }
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Running BusPassFormatDemo verification...");

        Path tempText = Files.createTempFile("bus_passes", ".txt");
        TextPassStore textStore = new TextPassStore(tempText);

        // Add records (including name with comma)
        textStore.addPass(new BusPass("BP-101", "192311001", "Patel, Aarav", 12, "Tambaram", "2027-05-31", 6500.0));
        textStore.addPass(new BusPass("BP-102", "192311002", "Bhavna Rao", 7, "Guindy", "2027-05-31", 5200.0));
        textStore.addPass(new BusPass("BP-103", "192311003", "Chetan Kumar", 12, "Chromepet", "2027-05-31", 6500.0));

        // Inject damaged lines
        Files.writeString(tempText, "CORRUPTED_LINE_WITHOUT_DELIMITERS\n", StandardOpenOption.APPEND);
        Files.writeString(tempText, "BP-999,192311999,BadAmt,12,Stop,2027-05-31,NOT_A_DOUBLE\n", StandardOpenOption.APPEND);

        // Verify Find
        BusPass found = textStore.findPassById("BP-102");
        System.out.println("Found BP-102: " + found);
        assert found != null && found.name.equals("Bhavna Rao");

        // Verify List by route
        List<BusPass> route12 = textStore.listPassesByRoute(12);
        System.out.println("Passes on Route 12: " + route12.size());
        assert route12.size() == 2;

        // Binary Store Test
        File tempBin = File.createTempFile("bus_passes", ".dat");
        BinaryPassStore binStore = new BinaryPassStore(tempBin);
        binStore.addPass(new BusPass("BP-201", "192311004", "Divya Sundaram", 5, "Porur", "2027-05-31", 4800.0));
        BusPass binFound = binStore.findPassById("BP-201");
        System.out.println("Binary Found BP-201: " + binFound);
        assert binFound != null && binFound.name.equals("Divya Sundaram");

        Files.deleteIfExists(tempText);
        tempBin.delete();

        System.out.println("BusPassFormatDemo verification PASSED.");
    }
}
