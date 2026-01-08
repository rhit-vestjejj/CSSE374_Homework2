package domain;

public interface LeaderboardStore {
    void saveLeaderboard(String data);
    String loadLeaderboardOrNull();
}
