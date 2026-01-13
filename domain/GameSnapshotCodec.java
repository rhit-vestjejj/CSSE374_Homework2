// By JP Vestjens


package domain;

import java.util.*;
import java.util.stream.Collectors;

public class GameSnapshotCodec {

    // Snapshot format:
    // P0:chips=R0,B1,G0,K2,W0;vp=3
    // P1:chips=R0,B0,G0,K0,W0;vp=0
    // TURN:current=0;choseChipAction=true;chipsTaken=R,B
    // BOARD:C1|1|B2K2;C2|2|R3

    public String encode(Game g) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 2; i++) {
            Player p = g.getPlayer(i);
            sb.append("P").append(i).append(":chips=");
            sb.append(encodeChips(p));
            sb.append(";vp=").append(p.getVictoryPoints()).append("\n");
        }

        TurnState t = g.getTurnState();
        sb.append("TURN:current=").append(t.getCurrentPlayerIndex());
        sb.append(";choseChipAction=").append(t.hasChoseChipAction());
        sb.append(";chipsTaken=");
        sb.append(
                t.getChipsTakenThisTurn().stream()
                        .map(c -> String.valueOf(c.toChar()))
                        .collect(Collectors.joining(","))
        );
        sb.append("\n");

        sb.append("BOARD:");
        List<Card> cards = g.getBoard().getAvailable();
        for (int i = 0; i < cards.size(); i++) {
            if (i > 0) sb.append(";");
            Card c = cards.get(i);
            sb.append(c.getId())
              .append("|")
              .append(c.getVictoryPoints())
              .append("|")
              .append(c.costString());
        }
        sb.append("\n");

        return sb.toString();
    }

    public Game decode(String snapshot, SnapshotStore store, LeaderboardStore leaderboardStore, Leaderboard leaderboard) {
        Game g = Game.newEmpty(store, leaderboardStore, leaderboard);
        if (snapshot == null || snapshot.isBlank()) {
            return g;
        }

        Map<String, String> lines = new HashMap<>();
        for (String line : snapshot.split("\n")) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                lines.put(line.substring(0, idx), line.substring(idx + 1));
            }
        }

        // Players
        for (int i = 0; i < 2; i++) {
            String line = lines.get("P" + i);
            if (line != null) {
                parsePlayerInto(g.getPlayer(i), line);
            }
        }

        // Turn
        String turnLine = lines.get("TURN");
        if (turnLine != null) {
            parseTurnInto(g.getTurnState(), turnLine);
        }

        // Board
        String boardLine = lines.get("BOARD");
        if (boardLine != null) {
            parseBoardInto(g.getBoard(), boardLine);
        }

        g.recomputeProgress();
        return g;
    }

    // ---------- Helpers ----------

    private String encodeChips(Player p) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (ChipColor c : ChipColor.values()) {
            if (!first) sb.append(",");
            first = false;
            sb.append(c.toChar()).append(p.getChips(c));
        }
        return sb.toString();
    }

    private void parsePlayerInto(Player p, String data) {
        p.reset();

        String[] parts = data.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("chips=")) {
                String chips = part.substring("chips=".length());
                for (String token : chips.split(",")) {
                    if (token.isEmpty()) continue;
                    ChipColor c = ChipColor.fromChar(token.charAt(0));
                    int n = Integer.parseInt(token.substring(1));
                    p.addChip(c, n);
                }
            } else if (part.startsWith("vp=")) {
                p.setVictoryPoints(Integer.parseInt(part.substring(3)));
            }
        }
    }

    private void parseTurnInto(TurnState t, String data) {
        int current = 0;
        boolean chose = false;
        List<ChipColor> chipsTaken = new ArrayList<>();

        String[] parts = data.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("current=")) {
                current = Integer.parseInt(part.substring("current=".length()));
            } else if (part.startsWith("choseChipAction=")) {
                chose = Boolean.parseBoolean(part.substring("choseChipAction=".length()));
            } else if (part.startsWith("chipsTaken=")) {
                String list = part.substring("chipsTaken=".length());
                if (!list.isBlank()) {
                    for (String tok : list.split(",")) {
                        chipsTaken.add(ChipColor.fromChar(tok.charAt(0)));
                    }
                }
            }
        }

        t.restore(current, chose, chipsTaken);
    }

    private void parseBoardInto(Board b, String data) {
        List<Card> cards = new ArrayList<>();

        if (!data.isBlank()) {
            for (String entry : data.split(";")) {
                String[] bits = entry.split("\\|");
                if (bits.length != 3) continue;

                String id = bits[0];
                int vp = Integer.parseInt(bits[1]);
                Map<ChipColor, Integer> cost = parseCost(bits[2]);
                cards.add(new Card(id, vp, cost));
            }
        }

        b.resetWith15Cards(cards);
    }

    private Map<ChipColor, Integer> parseCost(String s) {
        Map<ChipColor, Integer> cost = new EnumMap<>(ChipColor.class);
        for (ChipColor c : ChipColor.values()) cost.put(c, 0);

        if (s.equalsIgnoreCase("FREE")) return cost;

        for (int i = 0; i < s.length(); ) {
            char ch = s.charAt(i++);
            int start = i;
            while (i < s.length() && Character.isDigit(s.charAt(i))) i++;
            int n = Integer.parseInt(s.substring(start, i));
            cost.put(ChipColor.fromChar(ch), n);
        }

        return cost;
    }
}
