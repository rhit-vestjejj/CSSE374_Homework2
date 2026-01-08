package app;

import domain.Game;
import storage.*;
import presentation.SplendorFrame;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        KeyValueStore kv = new FileKeyValueStore(Path.of("data", "minisplendor.properties"));
        var snapshotStore = new DomainSnapshotStore(kv);
        var leaderboardStore = new DomainLeaderboardStore(kv);

        Game game = Game.loadOrNew(snapshotStore, leaderboardStore);

        javax.swing.SwingUtilities.invokeLater(() -> new SplendorFrame(game));
    }
}
