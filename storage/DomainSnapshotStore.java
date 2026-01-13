// By Ethan Spiece

package storage;

import domain.SnapshotStore;

public class DomainSnapshotStore implements SnapshotStore {
    private static final String KEY = "game.snapshot";
    private final KeyValueStore kv;

    public DomainSnapshotStore(KeyValueStore kv) {
        this.kv = kv;
    }

    @Override
    public void saveSnapshot(String snapshot) {
        kv.put(KEY, snapshot);
    }

    @Override
    public String loadSnapshotOrNull() {
        return kv.getOrNull(KEY);
    }

    @Override
    public void clear() {
        kv.remove(KEY);
    }
}