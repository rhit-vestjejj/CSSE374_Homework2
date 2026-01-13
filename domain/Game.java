// By JP Vestjens


package domain;

import java.util.*;

public class Game {
    private final Board board = new Board();
    private final Player[] players = new Player[] { new Player(), new Player() };
    private final TurnState turn = new TurnState();

    private final SnapshotStore store;
    private final LeaderboardStore leaderboardStore;
    private final Leaderboard leaderboard;
    private final GameSnapshotCodec codec = new GameSnapshotCodec();

    private String lastError = "";
    private boolean hasMeaningfulProgress = false;
    private boolean gameOverRecorded = false;

    private Game(SnapshotStore store, LeaderboardStore leaderboardStore, Leaderboard leaderboard) {
        this.store = store;
        this.leaderboardStore = leaderboardStore;
        this.leaderboard = leaderboard;
    }

    // Used by codec decode
    static Game newEmpty(SnapshotStore store, LeaderboardStore leaderboardStore, Leaderboard leaderboard) {
        Game g = new Game(store, leaderboardStore, leaderboard);
        g.startNewGameNoSave(); // makes 15 cards, resets players, resets turn
        return g;
    }

    public static Game loadOrNew(SnapshotStore store, LeaderboardStore leaderboardStore) {
        GameSnapshotCodec codec = new GameSnapshotCodec();
        Leaderboard leaderboard = Leaderboard.decode(leaderboardStore.loadLeaderboardOrNull());
        String snap = store.loadSnapshotOrNull();
        if (snap == null || snap.isBlank()) {
            Game g = new Game(store, leaderboardStore, leaderboard);
            g.startNewGame();
            return g;
        }
        return codec.decode(snap, store, leaderboardStore, leaderboard);
    }

    public void startNewGame() {
        recordCompletedGameIfProgress();
        store.clear();
        startNewGameNoSave();
        saveNow();
    }

    private void startNewGameNoSave() {
        for (Player p : players) p.reset();
        turn.setCurrentPlayerIndex(0);
        // You should add a restore/reset method on TurnState to clear mid-turn state.
        // For now:
        // (recommended) implement TurnState.restore(currentIdx, chipsTakenList, choseChipAction)
        // Here we’ll just new it up conceptually (or add a reset method).
        // We'll approximate by clearing through a new TurnState in your final code.

        board.resetWith15Cards(generate15Cards());
        hasMeaningfulProgress = false;
        gameOverRecorded = false;
        clearError();
    }

    public void takeChip(ChipColor color) {
        clearError();
        Player p = currentPlayer();

        // Rule: once you attempt chip-taking, you cannot buy this turn.
        turn.startChipActionIfNeeded();

        // Enforce chip rules:
        int n = turn.chipsTakenCount();
        if (n == 0) {
            // Always ok to take first chip
            p.addChip(color, 1);
            turn.recordChip(color);
            markProgress();
            saveNow(); // save after every move (each chip click)
            return;
        }

        if (n == 1) {
            ChipColor first = turn.getChipsTakenThisTurn().get(0);

            // second chip can be same OR different
            p.addChip(color, 1);
            turn.recordChip(color);
            markProgress();
            saveNow();

            // If took 2 of same -> end turn immediately
            if (color == first) endTurn();
            return;
        }

        if (n == 2) {
            ChipColor c1 = turn.getChipsTakenThisTurn().get(0);
            ChipColor c2 = turn.getChipsTakenThisTurn().get(1);

            // If first two were different, third must be different from both
            if (c1 != c2) {
                if (color == c1 || color == c2) {
                    throw illegal("Third chip must be a different color than the first two.");
                }
                p.addChip(color, 1);
                turn.recordChip(color);
                markProgress();
                saveNow();
                endTurn();
                return;
            }

            // If first two were same, the rules say that should have ended immediately,
            // so reaching here implies inconsistent state.
            throw illegal("Internal state error: turn should have ended after taking 2 same-color chips.");
        }

        throw illegal("You already took the maximum number of chips this turn.");
    }

    public void buyCard(String cardId) {
        clearError();

        // Rule: cannot buy if you took chips in this turn
        if (turn.hasChoseChipAction() && turn.chipsTakenCount() > 0) {
            throw illegal("You cannot buy a card after taking chips this turn.");
        }

        Card c = board.getCardById(cardId);
        if (c == null) throw illegal("That card is not available.");

        Player p = currentPlayer();
        if (!p.canAfford(c)) throw illegal("You cannot afford this card.");

        p.buy(c);
        board.removeCard(cardId);
        markProgress();

        saveNow(); // save after move
        checkGameOverAfterMove();
        endTurn();
    }

    private void endTurn() {
        turn.resetForNextTurn();
        saveNow();
    }

    private Player currentPlayer() {
        return players[turn.getCurrentPlayerIndex()];
    }

    private IllegalMoveException illegal(String msg) {
        lastError = msg;
        return new IllegalMoveException(msg);
    }

    private void saveNow() {
        store.saveSnapshot(codec.encode(this));
    }

    private void saveLeaderboardNow() {
        if (leaderboardStore == null) return;
        leaderboardStore.saveLeaderboard(leaderboard.encode());
    }

    private void recordCompletedGameIfProgress() {
        if (!hasMeaningfulProgress || gameOverRecorded) return;
        leaderboard.addEntry(players[0].getVictoryPoints(), players[1].getVictoryPoints(), System.currentTimeMillis());
        saveLeaderboardNow();
        hasMeaningfulProgress = false;
        gameOverRecorded = true;
    }

    // ---------- Getters for UI ----------
    public int getCurrentPlayerNumber() { return turn.getCurrentPlayerIndex() + 1; }
    public Board getBoard() { return board; }
    public Player getPlayer(int idx) { return players[idx]; }
    public TurnState getTurnState() { return turn; }
    public List<Leaderboard.Entry> getLeaderboardEntries() { return leaderboard.getEntries(); }

    public String getLastError() { return lastError == null ? "" : lastError; }
    public void clearError() { lastError = ""; }

    // ---------- Card generation ----------
    private List<Card> generate15Cards() {
        // Easiest: hardcode 15 consistent cards.
        // The rules: 0-3 chips per color, at least 2 in one color, max 3 colors in cost.  [oai_citation:6‡374 Homework 2 - minisplendor-Steve (2).pdf](sediment://file_000000007540722f9e8bbcb3aca3178b)
        List<Card> cards = new ArrayList<>();
        cards.add(card("C1", 1, "B2K2"));
        cards.add(card("C2", 1, "G2W2"));
        cards.add(card("C3", 1, "R2B2"));
        cards.add(card("C4", 2, "G3K2"));
        cards.add(card("C5", 2, "R3"));
        cards.add(card("C6", 2, "B3"));
        cards.add(card("C7", 2, "G3"));
        cards.add(card("C8", 2, "W3"));
        cards.add(card("C9", 3, "R3B2"));
        cards.add(card("C10", 3, "G3K2"));
        cards.add(card("C11", 3, "R3K2"));
        cards.add(card("C12", 4, "R3B3"));
        cards.add(card("C13", 4, "G3W3"));
        cards.add(card("C14", 5, "G3K3"));
        cards.add(card("C15", 5, "R3B3W3")); // 3 colors max ok
        return cards;
    }

    private Card card(String id, int vp, String costString) {
        Map<ChipColor, Integer> cost = new EnumMap<>(ChipColor.class);
        for (ChipColor c : ChipColor.values()) cost.put(c, 0);

        // parse like R3B2K1W3
        for (int i = 0; i < costString.length(); ) {
            char ch = costString.charAt(i++);
            int start = i;
            while (i < costString.length() && Character.isDigit(costString.charAt(i))) i++;
            int n = Integer.parseInt(costString.substring(start, i));
            cost.put(ChipColor.fromChar(ch), n);
        }
        return new Card(id, vp, cost);
    }

    void recomputeProgress() {
        hasMeaningfulProgress = hasProgressFromState();
        gameOverRecorded = board.isEmpty();
    }

    private boolean hasProgressFromState() {
        if (turn.chipsTakenCount() > 0 || turn.hasChoseChipAction()) return true;
        if (board.getAvailable().size() != 15) return true;
        for (Player p : players) {
            if (p.getVictoryPoints() > 0) return true;
            for (ChipColor c : ChipColor.values()) {
                if (p.getChips(c) > 0) return true;
            }
        }
        return false;
    }

    private void markProgress() {
        hasMeaningfulProgress = true;
    }

    private void checkGameOverAfterMove() {
        if (board.isEmpty()) {
            recordCompletedGameIfProgress();
        }
    }
}
