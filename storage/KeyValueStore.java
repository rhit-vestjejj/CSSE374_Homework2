// By Ethan Spiece

package storage;

public interface KeyValueStore {
    void put(String key, String value);
    String getOrNull(String key);
    void remove(String key);
}