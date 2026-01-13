// By JP Vestjens


package domain;

public interface SnapshotStore {
    void saveSnapshot(String snapshot);
    String loadSnapshotOrNull();
    void clear();
}