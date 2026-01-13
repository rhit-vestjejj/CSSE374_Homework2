// By Ethan Spiece

package storage;

import domain.LeaderboardStore;

public class DomainLeaderboardStore implements LeaderboardStore {
    private static final String KEY = "game.leaderboard";
    private final KeyValueStore kv;

    public DomainLeaderboardStore(KeyValueStore kv) {
        this.kv = kv;
    }

    @Override
    public void saveLeaderboard(String data) {
        kv.put(KEY, data);
    }

    @Override
    public String loadLeaderboardOrNull() {
        return kv.getOrNull(KEY);
    }
}
