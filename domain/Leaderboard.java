package domain;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class Leaderboard {
    public static class Entry {
        private final long timestampMillis;
        private final int player1Vp;
        private final int player2Vp;

        public Entry(long timestampMillis, int player1Vp, int player2Vp) {
            this.timestampMillis = timestampMillis;
            this.player1Vp = player1Vp;
            this.player2Vp = player2Vp;
        }

        public long getTimestampMillis() { return timestampMillis; }
        public int getPlayer1Vp() { return player1Vp; }
        public int getPlayer2Vp() { return player2Vp; }
    }

    private final Deque<Entry> entries = new ArrayDeque<>();

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public void addEntry(int player1Vp, int player2Vp, long timestampMillis) {
        entries.addFirst(new Entry(timestampMillis, player1Vp, player2Vp));
        trimToLastFour();
    }

    public String encode() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Entry e : entries) {
            if (!first) sb.append(";");
            first = false;
            sb.append(e.timestampMillis)
              .append(",")
              .append(e.player1Vp)
              .append(",")
              .append(e.player2Vp);
        }
        return sb.toString();
    }

    public static Leaderboard decode(String data) {
        Leaderboard lb = new Leaderboard();
        if (data == null || data.isBlank()) return lb;

        for (String entry : data.split(";")) {
            String[] bits = entry.split(",");
            if (bits.length != 3) continue;
            try {
                long ts = Long.parseLong(bits[0]);
                int p1 = Integer.parseInt(bits[1]);
                int p2 = Integer.parseInt(bits[2]);
                lb.entries.addLast(new Entry(ts, p1, p2));
            } catch (NumberFormatException ignored) {
            }
        }

        lb.trimToLastFour();
        return lb;
    }

    private void trimToLastFour() {
        while (entries.size() > 4) {
            entries.removeLast();
        }
    }
}
