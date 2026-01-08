package domain;

import java.util.EnumMap;

public class Player {
    private int victoryPoints;
    private final EnumMap<ChipColor, Integer> chips = new EnumMap<>(ChipColor.class);

    public Player() {
        reset();
    }

    public void reset() {
        victoryPoints = 0;
        for (ChipColor c : ChipColor.values()) chips.put(c, 0);
    }

    public int getVictoryPoints() { return victoryPoints; }
    public int getChips(ChipColor c) { return chips.getOrDefault(c, 0); }

    public void addChip(ChipColor c, int n) {
        chips.put(c, getChips(c) + n);
    }

    public void spendChip(ChipColor c, int n) {
        int have = getChips(c);
        if (have < n) throw new IllegalMoveException("Not enough chips to spend");
        chips.put(c, have - n);
    }

    public boolean canAfford(Card card) {
        for (var e : card.getCost().entrySet()) {
            if (getChips(e.getKey()) < e.getValue()) return false;
        }
        return true;
    }

    public void buy(Card card) {
        if (!canAfford(card)) throw new IllegalMoveException("You cannot afford this card.");
        for (var e : card.getCost().entrySet()) {
            if (e.getValue() > 0) spendChip(e.getKey(), e.getValue());
        }
        victoryPoints += card.getVictoryPoints();
    }
    
    public void setVictoryPoints(int vp) {
        this.victoryPoints = vp;
    }
}