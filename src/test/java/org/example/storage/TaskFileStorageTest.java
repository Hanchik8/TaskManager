package org.example.storage;

import org.example.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class TaskFileStorageTest {
    @Test
    public void testTaskFileStorage(@TempDir Path tempDir) throws IOException {
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "помыть посуду");
        firstTask.complete();

        List<Task> tasks = new ArrayList<>();
        tasks.add(firstTask);
        tasks.add(secondTask);

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        taskFileStorage.save(filePath, tasks);

        Properties actual = readProperties(filePath);

        assertEquals("1", actual.getProperty("formatVersion"));
        assertEquals("2", actual.getProperty("count"));
        assertEquals("1", actual.getProperty("task.0.id"));
        assertEquals("clean room", actual.getProperty("task.0.title"));
        assertEquals("true", actual.getProperty("task.0.done"));
        assertEquals("2", actual.getProperty("task.1.id"));
        assertEquals("помыть посуду", actual.getProperty("task.1.title"));
        assertEquals("false", actual.getProperty("task.1.done"));
        assertEquals(8, actual.size());
    }

    @Test
    public void savesEmptyTasksListIntoFile(@TempDir Path tempDir) throws IOException {
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "помыть посуду");
        firstTask.complete();

        List<Task> tasks = new ArrayList<>();
        tasks.add(firstTask);
        tasks.add(secondTask);

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        taskFileStorage.save(filePath, tasks);

        Properties actual = readProperties(filePath);

        assertEquals("2", actual.getProperty("count"));

        tasks.clear();
        taskFileStorage.save(filePath, tasks);
        actual = readProperties(filePath);
        assertEquals("1", actual.getProperty("formatVersion"));
        assertEquals(2, actual.size());
        assertEquals("0", actual.getProperty("count"));
    }

    @Test
    public void savesTasksListWithSpecialSymbolsIntoFile(@TempDir Path tempDir) throws IOException {
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "  План=готов\nПуть C:\\temp  ");
        firstTask.complete();

        List<Task> tasks = new ArrayList<>();
        tasks.add(firstTask);
        tasks.add(secondTask);

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        taskFileStorage.save(filePath, tasks);

        Properties actual = readProperties(filePath);

        assertEquals("1", actual.getProperty("formatVersion"));
        assertEquals("2", actual.getProperty("count"));
        assertEquals("1", actual.getProperty("task.0.id"));
        assertEquals("clean room", actual.getProperty("task.0.title"));
        assertEquals("true", actual.getProperty("task.0.done"));
        assertEquals("2", actual.getProperty("task.1.id"));
        assertEquals("  План=готов\nПуть C:\\temp  ", actual.getProperty("task.1.title"));
        assertEquals("false", actual.getProperty("task.1.done"));
        assertEquals(8, actual.size());
    }

    @Test
    public void loadsTasksListInSameOrder(@TempDir Path tempDir) throws IOException {
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "помыть посуду");
        firstTask.complete();

        List<Task> tasks = new ArrayList<>();
        tasks.add(firstTask);
        tasks.add(secondTask);

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        taskFileStorage.save(filePath, tasks);

        List<Task> actual = taskFileStorage.load(filePath);
        assertEquals(2, actual.size());
        assertNotSame(firstTask, actual.get(0));
        assertNotSame(secondTask, actual.get(1));
        assertEquals(1, actual.getFirst().getId());
        assertEquals("clean room", actual.get(0).getTitle());
        assertTrue(actual.get(0).isDone());
        assertEquals(2, actual.get(1).getId());
        assertEquals("помыть посуду", actual.get(1).getTitle());
        assertFalse(actual.get(1).isDone());

    }

    @Test
    public void loadsBlankTasksList(@TempDir Path tempDir) throws IOException {
        List<Task> tasks = new ArrayList<>();

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        taskFileStorage.save(filePath, tasks);

        List<Task> actual = taskFileStorage.load(filePath);
        assertEquals(0, actual.size());
    }

    @Test
    public void reportsWhenDoneFieldIsIncorrect(@TempDir Path tempDir) throws IOException {

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        Files.writeString(filePath,
                """
                        count=1
                        task.0.id=1
                        task.0.title=Test
                        task.0.done=maybe
                        """,
                StandardCharsets.UTF_8);

        IOException exception =
                assertThrows(IOException.class, () -> taskFileStorage.load(filePath));

        assertEquals("Invalid boolean value for key: task.0.done", exception.getMessage());
    }

    @Test
    public void reportsWhenThereIsNoDoneField(@TempDir Path tempDir) throws IOException {

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        Files.writeString(filePath,
                """
                        count=1
                        task.0.id=1
                        task.0.title=Test
                        """,
                StandardCharsets.UTF_8);

        IOException exception =
                assertThrows(IOException.class, () -> taskFileStorage.load(filePath));

        assertEquals("Invalid boolean value for key: task.0.done", exception.getMessage());
    }


    @Test
    public void reportsWhenThereIsNotNumberCountField(@TempDir Path tempDir) throws IOException {

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        Files.writeString(filePath,
                """
                        count=abc
                        task.0.id=1
                        task.0.title=Test
                        task.0.done=true
                        """,
                StandardCharsets.UTF_8);

        IOException exception =
                assertThrows(IOException.class, () -> taskFileStorage.load(filePath));

        assertEquals("Invalid integer value for key: count", exception.getMessage());
        assertInstanceOf(NumberFormatException.class, exception.getCause());
    }

    @Test
    public void reportsWhenThereIsNegativeCountField(@TempDir Path tempDir) throws IOException {

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        Files.writeString(filePath,
                """
                        count=-1
                        task.0.id=1
                        task.0.title=Test
                        task.0.done=true
                        """,
                StandardCharsets.UTF_8);

        IOException exception =
                assertThrows(IOException.class, () -> taskFileStorage.load(filePath));

        assertEquals("Invalid integer value for count: -1", exception.getMessage());
    }


    @Test
    public void reportsWhenThereIsInvalidIdField(@TempDir Path tempDir) throws IOException {

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        Files.writeString(filePath,
                """
                        count=1
                        task.0.id=0
                        task.0.title=Test
                        task.0.done=true
                        """,
                StandardCharsets.UTF_8);

        IOException exception =
                assertThrows(IOException.class, () -> taskFileStorage.load(filePath));

        assertEquals("Invalid task for prefix: task.0.", exception.getMessage());
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    public void reportsWhenThereIsInvalidTitleField(@TempDir Path tempDir) throws IOException {

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        Files.writeString(filePath,
                """
                        count=1
                        task.0.id=1
                        task.0.done=true
                        """,
                StandardCharsets.UTF_8);

        IOException exception =
                assertThrows(IOException.class, () -> taskFileStorage.load(filePath));

        assertEquals("Invalid task for prefix: task.0.", exception.getMessage());
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    public void reportsWhenThereIsDuplicateInIdField(@TempDir Path tempDir) throws IOException {

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        Files.writeString(filePath,
                """
                        count=2
                        task.0.id=2
                        task.0.done=true
                        task.0.title=Test
                        task.1.id=2
                        task.1.done=false
                        task.1.title=Test1
                        """,
                StandardCharsets.UTF_8);

        IOException exception =
                assertThrows(IOException.class, () -> taskFileStorage.load(filePath));

        assertEquals("Duplicate task ID: 2 at index 1", exception.getMessage());
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }


    @Test
    public void reportsWhenThereIsInvalidPropertiesFormatField(@TempDir Path tempDir) throws IOException {

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        Files.writeString(filePath,
                """
                        count=1
                        task.0.id=1
                        task.0.done=true
                        task.0.title=\\uZZZZ
                        """,
                StandardCharsets.UTF_8);

        IOException exception =
                assertThrows(IOException.class, () -> taskFileStorage.load(filePath));

        assertEquals("Invalid properties format", exception.getMessage());
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());

    }


    @Test
    public void loadsLegacyFileWithoutVersion (@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        Files.writeString(filePath,
                """
                        count=1
                        task.0.id=7
                        task.0.done=true
                        task.0.title=Legacy task
                        """,
                StandardCharsets.UTF_8);
        List<Task> tasks = taskFileStorage.load(filePath);
        TaskSnapshot legacyTask = TaskSnapshot.fromTask(tasks.getFirst());
        Properties actual = readProperties(filePath);

        assertEquals("1", actual.getProperty("count"));
        assertEquals(1, tasks.size());
        assertEquals(
                new TaskSnapshot(7, "Legacy task", true), legacyTask);
        assertEquals(null, actual.getProperty("formatVersion"));
    }

    @Test
    public void rejectsUnsupportedFileVersion (@TempDir Path tempDir) throws IOException {
        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        Files.writeString(filePath,
                """
                        formatVersion=2
                        count=0
                        """,
                StandardCharsets.UTF_8);

        IOException exception =
                assertThrows(IOException.class, () -> taskFileStorage.load(filePath));
        assertEquals("Unsupported task file version: 2", exception.getMessage());
    }

    @Test
    public void rejectsDuplicateIdsWithoutOverwritingFile(@TempDir Path tempDir) throws IOException {
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(1, "помыть посуду");
        firstTask.complete();

        List<Task> tasks = new ArrayList<>();
        tasks.add(firstTask);
        tasks.add(secondTask);

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        Files.writeString(filePath, "old content", StandardCharsets.UTF_8);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> taskFileStorage.save(filePath, tasks));

        String fileContent = Files.readString(filePath);

        assertEquals("old content", fileContent);
        assertEquals("Task with ID 1 already exists.", exception.getMessage());
    }

    @Test
    public void rejectsNullTaskWithoutOverwritingFile(@TempDir Path tempDir) throws IOException {
        Task firstTask = new Task(1, "clean room");
        firstTask.complete();

        List<Task> tasks = new ArrayList<>();
        tasks.add(firstTask);
        tasks.add(null);

        Path filePath = tempDir.resolve("saved.txt");
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        Files.writeString(filePath, "old content", StandardCharsets.UTF_8);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> taskFileStorage.save(filePath, tasks));

        String fileContent = Files.readString(filePath);

        assertEquals("old content", fileContent);
        assertEquals("Task can't be null", exception.getMessage());
    }

    @Test
    public void preservesExistingFileWhenWritingFails(@TempDir Path tempDir) throws IOException {
        Task firstTask = new Task(1, "Broken \uD800");
        firstTask.complete();

        Path filePath = tempDir.resolve("saved.txt");
        Files.writeString(filePath, "old content", StandardCharsets.UTF_8);
        TaskFileStorage taskFileStorage = new TaskFileStorage();

        assertThrows(IOException.class, () -> taskFileStorage.save(filePath, List.of(firstTask)));

        String fileContent = Files.readString(filePath);

        assertEquals("old content", fileContent);
        assertEquals(List.of(filePath), listDirectoryEntries(tempDir));
    }

    @Test
    public void removesTemporaryFileWhenMoveFails(@TempDir Path tempDir) throws IOException {
        Task firstTask = new Task(1, "clean room");
        firstTask.complete();

        Path targetDirectory = Files.createDirectory(tempDir.resolve("target"));
        Path filePath = targetDirectory.resolve("keep.txt");
        Files.writeString(filePath, "old content", StandardCharsets.UTF_8);
        TaskFileStorage taskFileStorage = new TaskFileStorage();

        assertThrows(IOException.class, () -> taskFileStorage.save(targetDirectory, List.of(firstTask)));

        String fileContent = Files.readString(filePath);

        assertEquals("old content", fileContent);
        assertEquals(List.of(targetDirectory), listDirectoryEntries(tempDir));
    }


    private Properties readProperties(Path path) throws IOException {
        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        return properties;
    }

    private List<Path> listDirectoryEntries(Path directory) throws IOException {
        ArrayList<Path> pathsList = new ArrayList<>();
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory)) {
            for (Path entry : entries) {
                pathsList.add(entry);
            }
        }
        return pathsList;
    }

    }
