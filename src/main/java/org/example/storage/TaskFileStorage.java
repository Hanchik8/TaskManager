package org.example.storage;

import org.example.model.Task;
import org.example.model.TaskManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class TaskFileStorage {
    private static final String FORMAT_VERSION = "1";

    public void save(Path path, List<Task> tasks) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("formatVersion", FORMAT_VERSION);

        TaskManager taskManager = new TaskManager();
        taskManager.replaceTasks(tasks);
        List<Task> validatedTasks = taskManager.getTasks();

        properties.setProperty("count", String.valueOf(validatedTasks.size()));


        for (int i = 0; i < validatedTasks.size(); i++) {
            TaskSnapshot snapshot = TaskSnapshot.fromTask(validatedTasks.get(i));
            String prefix = "task." + i + ".";
            properties.setProperty(prefix + "id", Integer.toString(snapshot.id()));
            properties.setProperty(prefix + "title", snapshot.title());
            properties.setProperty(prefix + "done", Boolean.toString(snapshot.done()));
        }

        writeAtomically(path, properties);
    }

    public List<Task> load(Path path) throws IOException {
        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid properties format", e);
        }
        validateFormatVersion(properties);
        int count = readInt(properties, "count");
        if (count < 0) throw new IOException("Invalid integer value for count: " + properties.getProperty("count"));
        TaskManager taskManager = new TaskManager();
        for (int i = 0; i < count; i++) {
            try {
                taskManager.addTask(readTask(properties, i));
            } catch (IllegalArgumentException e) {
                throw new IOException("Duplicate task ID: " + properties.getProperty("task." + i + ".id") + " at index " + i, e);
            }
        }
        return taskManager.getTasks();
    }

    private boolean readDone(Properties properties, String key) throws IOException {
        String result = properties.getProperty(key);
        if (Objects.equals(result, "true")) return true;
        if (!Objects.equals(result, "false")) throw new IOException("Invalid boolean value for key: " + key);
        return false;
    }

    private int readInt(Properties properties, String key) throws IOException {
        String result = properties.getProperty(key);
        try {
            return Integer.parseInt(result);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid integer value for key: " + key, e);
        }
    }

    private Task readTask(Properties properties, int index) throws IOException {
        String prefix = "task." + index + ".";
        int id = readInt(properties, prefix + "id");
        String title = properties.getProperty(prefix + "title");
        boolean done = readDone(properties, prefix + "done");
        try {
            TaskSnapshot snapshot = new TaskSnapshot(id, title, done);
            return snapshot.toTask();
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid task for prefix: " + prefix, e);
        }
    }

    private void validateFormatVersion(Properties properties) throws IOException {
        String formatVersion = properties.getProperty("formatVersion");
        if (formatVersion == null) return;
        if (!formatVersion.equals(FORMAT_VERSION)) {
            throw new IOException("Unsupported task file version: " + properties.getProperty("formatVersion"));
        }
    }

    private void writeAtomically(Path path, Properties properties) throws IOException {
        Path target = path.toAbsolutePath();
        Path parent = target.getParent();
        if (parent == null) throw new IOException("Cannot write task file as parent directory is null");
        Path temp = Files.createTempFile(parent, ".taskmanager-", ".tmp");
        try {
            try (var writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                properties.store(writer, null);
            }
            Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException cleanupError) {
                e.addSuppressed(cleanupError);
            }
            throw e;
        }
    }
}