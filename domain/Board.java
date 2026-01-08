package domain;

import java.util.*;

public class Board {
    private final List<Card> available = new ArrayList<>();

    public List<Card> getAvailable() {
        return Collections.unmodifiableList(available);
    }

    public Card getCardById(String id) {
        for (Card c : available) if (c.getId().equals(id)) return c;
        return null;
    }

    public void removeCard(String id) {
        available.removeIf(c -> c.getId().equals(id));
    }

    public void resetWith15Cards(List<Card> cards) {
        available.clear();
        available.addAll(cards);
    }

    public boolean isEmpty() {
        return available.isEmpty();
    }
}