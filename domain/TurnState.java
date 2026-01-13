// By JP Vestjens


package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TurnState {

    private int currentPlayerIndex = 0;
    private final List<ChipColor> chipsTakenThisTurn = new ArrayList<>();
    private boolean choseChipAction = false;

    // ---------- Getters ----------
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public List<ChipColor> getChipsTakenThisTurn() {
        // IMPORTANT: UI gets read-only view
        return Collections.unmodifiableList(chipsTakenThisTurn);
    }

    public boolean hasChoseChipAction() {
        return choseChipAction;
    }

    // ---------- Turn control ----------
    public void setCurrentPlayerIndex(int idx) {
        this.currentPlayerIndex = idx;
    }

    public void startChipActionIfNeeded() {
        this.choseChipAction = true;
    }

    public void recordChip(ChipColor c) {
        chipsTakenThisTurn.add(c);
        choseChipAction = true;
    }

    public int chipsTakenCount() {
        return chipsTakenThisTurn.size();
    }

    public boolean hasTakenColor(ChipColor c) {
        return chipsTakenThisTurn.contains(c);
    }

    public void resetForNextTurn() {
        chipsTakenThisTurn.clear();
        choseChipAction = false;
        currentPlayerIndex = 1 - currentPlayerIndex;
    }

    public void resetSamePlayer() {
        chipsTakenThisTurn.clear();
        choseChipAction = false;
    }

    // ---------- SAVE / RESTORE SUPPORT ----------
    public void restore(int currentPlayerIndex,
                        boolean choseChipAction,
                        List<ChipColor> chipsTaken) {

        this.currentPlayerIndex = currentPlayerIndex;
        this.choseChipAction = choseChipAction;

        this.chipsTakenThisTurn.clear();
        this.chipsTakenThisTurn.addAll(chipsTaken);
    }
}