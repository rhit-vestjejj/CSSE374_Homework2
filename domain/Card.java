package domain;

import java.util.*;

public class Card {
    private final String id;
    private final int victoryPoints;
    private final EnumMap<ChipColor, Integer> cost;

    public Card(String id, int victoryPoints, Map<ChipColor, Integer> cost) {
        this.id = id;
        this.victoryPoints = victoryPoints;
        this.cost = new EnumMap<>(ChipColor.class);
        for (ChipColor c : ChipColor.values()) this.cost.put(c, 0);
        for (var e : cost.entrySet()) this.cost.put(e.getKey(), e.getValue());
    }

    public String getId() { return id; }
    public int getVictoryPoints() { return victoryPoints; }
    public Map<ChipColor, Integer> getCost() { return Collections.unmodifiableMap(cost); }

    // Example: R3B3W0G0K0 -> but you can shorten to only non-zero for display.
    public String costString() {
        StringBuilder sb = new StringBuilder();
        for (ChipColor c : ChipColor.values()) {
            int n = cost.getOrDefault(c, 0);
            if (n > 0) sb.append(c.toChar()).append(n);
        }
        if (sb.length() == 0) sb.append("FREE");
        return sb.toString();
    }
}