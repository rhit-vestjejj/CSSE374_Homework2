package storage;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class FileKeyValueStore implements KeyValueStore {
    private final Path filePath;

    public FileKeyValueStore(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public synchronized void put(String key, String value) {
        Properties p = loadProps();
        p.setProperty(key, value);
        saveProps(p);
    }

    @Override
    public synchronized String getOrNull(String key) {
        Properties p = loadProps();
        return p.getProperty(key);
    }

    @Override
    public synchronized void remove(String key) {
        Properties p = loadProps();
        p.remove(key);
        saveProps(p);
    }

    private Properties loadProps() {
        Properties p = new Properties();
        if (!Files.exists(filePath)) return p;
        try (InputStream in = Files.newInputStream(filePath)) {
            p.load(in);
            return p;
        } catch (IOException e) {
            // If corrupted, treat as empty to avoid crashing your UI
            return new Properties();
        }
    }

    private void saveProps(Properties p) {
        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException ignored) { }
        try (OutputStream out = Files.newOutputStream(filePath)) {
            p.store(out, "Mini-Splendor Save");
        } catch (IOException e) {
            throw new RuntimeException("Failed to save game state", e);
        }
    }
}