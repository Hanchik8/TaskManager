package org.example.console;

import org.example.export.TaskReportWriter;
import org.example.model.Task;
import org.example.model.TaskManager;
import org.example.storage.TaskFileStorage;
import org.example.storage.TaskSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

public class TaskConsoleTest {

    @Test
    @Timeout(1)
    void completesCorrectlyByCommands() {
        TaskManager manager = new TaskManager();
        Task task = new Task(1, "clean room");
        manager.addTask(task);

        String actualOutput = runConsole("complete\nabc\ncomplete\n1\nexit\n", manager);

        assertTrue(actualOutput.contains("Task completed"));
        assertTrue(task.isDone());
    }

    @Test
    @Timeout(1)
    void rejectsCommandsIfConsoleClosed() {
        TaskManager manager = new TaskManager();
        Task task = new Task(1, "clean room");
        manager.addTask(task);

        runConsole("exit\ncomplete\n1\n", manager);

        assertFalse(task.isDone());
    }

    @Test
    @Timeout(1)
    void reportsNotFoundWhenCompletingMissingTask() {
        TaskManager manager = new TaskManager();

        String actualOutput = runConsole("complete\n99\nexit\n", manager);

        assertFalse(actualOutput.contains("Task completed"));
        assertTrue(actualOutput.contains("Task not found"));
    }

    @Test
    @Timeout(1)
    void deletesSpecificTaskNotAffectingOnOthers() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "clean hall");
        manager.addTask(firstTask);
        manager.addTask(secondTask);

        String actualOutput = runConsole("delete\n2\nexit\n", manager);

        assertTrue(actualOutput.contains("Task deleted"));
        assertEquals(List.of(firstTask), manager.getTasks());
    }

    @Test
    @Timeout(1)
    void reportsNotFoundWhenDeletingMissingTask() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "clean hall");
        manager.addTask(firstTask);
        manager.addTask(secondTask);

        String actualOutput = runConsole("delete\n99\nexit\n", manager);

        assertTrue(actualOutput.contains("Task not found"));
        assertFalse(actualOutput.contains("Task deleted"));
        assertEquals(List.of(firstTask, secondTask), manager.getTasks());

    }


    @Test
    @Timeout(1)
    void reportsAboutUnexpectedCommandAndDeletesTaskCorrectlyAfter() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "clean hall");
        manager.addTask(firstTask);
        manager.addTask(secondTask);

        String actualOutput = runConsole("delete\nabc\ndelete\n2\nexit\n", manager);

        assertTrue(actualOutput.contains("Error: ID must be a number."));
        assertTrue(actualOutput.contains("Task deleted"));
        assertEquals(List.of(firstTask), manager.getTasks());
    }

    @Test
    @Timeout(1)
    void notReactingWhenLeavesAfterCallingDeleteMethod() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        manager.addTask(firstTask);

        String actualOutput = runConsole("delete\n", manager);

        assertFalse(actualOutput.contains("Error: ID must be a number."));
        assertFalse(actualOutput.contains("Task not found"));
        assertFalse(actualOutput.contains("Task deleted"));
        assertFalse(firstTask.isDone());
        assertEquals(List.of(firstTask), manager.getTasks());
    }

    @Test
    @Timeout(1)
    void notReactingWhenLeavesAfterCallingCompleteMethod() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        manager.addTask(firstTask);

        String actualOutput = runConsole("complete\n", manager);

        assertFalse(actualOutput.contains("Error: ID must be a number."));
        assertFalse(actualOutput.contains("Task not found"));
        assertFalse(actualOutput.contains("Task completed"));
        assertFalse(firstTask.isDone());
        assertEquals(List.of(firstTask), manager.getTasks());
    }

    @Test
    @Timeout(1)
    void renamesTaskCorrectly() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "clean hall");
        manager.addTask(firstTask);
        manager.addTask(secondTask);

        String actualOutput = runConsole("rename\n1\nwash dishes\nexit\n", manager);

        assertFalse(actualOutput.contains("Error: ID must be a number."));
        assertFalse(actualOutput.contains("Task not found"));
        assertTrue(actualOutput.contains("Enter new title:"));
        assertTrue(actualOutput.contains("Task renamed"));
        assertFalse(firstTask.isDone());
        assertFalse(secondTask.isDone());
        assertEquals("wash dishes", firstTask.getTitle());
        assertEquals("clean hall", secondTask.getTitle());
        assertEquals(List.of(firstTask, secondTask), manager.getTasks());
    }

    @Test
    @Timeout(1)
    void reportsAboutRenamingNotExistedTask() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "clean hall");
        manager.addTask(firstTask);
        manager.addTask(secondTask);

        String actualOutput = runConsole("rename\n99\nwash dishes\nexit\n", manager);

        assertFalse(actualOutput.contains("Error: ID must be a number."));
        assertTrue(actualOutput.contains("Task not found"));
        assertTrue(actualOutput.contains("Enter new title:"));
        assertFalse(actualOutput.contains("Task renamed"));
        assertFalse(firstTask.isDone());
        assertFalse(secondTask.isDone());
        assertEquals("clean room", firstTask.getTitle());
        assertEquals("clean hall", secondTask.getTitle());
        assertEquals(List.of(firstTask, secondTask), manager.getTasks());
    }

    @Test
    @Timeout(1)
    void reportsAboutRenamingTaskByBlankTitle() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "clean hall");
        manager.addTask(firstTask);
        manager.addTask(secondTask);

        String actualOutput = runConsole("rename\n1\n\nlist\nexit\n", manager);

        assertFalse(actualOutput.contains("Error: ID must be a number."));
        assertFalse(actualOutput.contains("Task not found"));
        assertTrue(actualOutput.contains("Enter new title:"));
        assertFalse(actualOutput.contains("Task renamed"));
        assertTrue(actualOutput.contains("Error: title must not be blank."));
        assertTrue(actualOutput.contains("clean room"));
        assertFalse(firstTask.isDone());
        assertFalse(secondTask.isDone());
        assertEquals("clean room", firstTask.getTitle());
        assertEquals("clean hall", secondTask.getTitle());
        assertEquals(List.of(firstTask, secondTask), manager.getTasks());
    }

    @Test
    @Timeout(1)
    void notReactingWhenLeavesAfterCallingRenameMethod() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        manager.addTask(firstTask);

        String actualOutput = runConsole("rename\n1\n", manager);

        assertFalse(actualOutput.contains("Error: ID must be a number."));
        assertFalse(actualOutput.contains("Error: title must not be blank."));
        assertFalse(actualOutput.contains("Task not found"));
        assertFalse(actualOutput.contains("Task renamed"));
        assertFalse(firstTask.isDone());
        assertEquals("clean room", firstTask.getTitle());
        assertEquals(List.of(firstTask), manager.getTasks());
    }

    @Test
    @Timeout(1)
    void addsTaskCorrectly() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        manager.addTask(firstTask);

        String actualOutput = runConsole("add\n2\nwash dishes\nexit\n", manager);

        assertFalse(actualOutput.contains("Error: ID must be a number."));
        assertFalse(actualOutput.contains("Task not found"));
        assertTrue(actualOutput.contains("Task added"));
        assertFalse(firstTask.isDone());
        Task secondTask = manager.getTasks().get(1);
        assertFalse(secondTask.isDone());
        assertEquals("wash dishes", secondTask.getTitle());
        assertEquals(2, secondTask.getId());
        assertEquals(List.of(firstTask, secondTask), manager.getTasks());
    }


    @Test
    @Timeout(1)
    void reportsAboutAddingTaskWithExistedId() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        manager.addTask(firstTask);

        String actualOutput = runConsole("add\n1\nwash dishes\nexit\n", manager);

        assertFalse(actualOutput.contains("Error: ID must be a number."));
        assertFalse(actualOutput.contains("Task not found"));
        assertFalse(actualOutput.contains("Task added"));
        assertFalse(firstTask.isDone());
        assertFalse(manager.getTasks().getFirst().isDone());
        assertTrue(actualOutput.contains("Error: Task with ID 1 already exists."));
        assertEquals("clean room", firstTask.getTitle());
        assertEquals(List.of(firstTask), manager.getTasks());
    }

    @Test
    @Timeout(1)
    void reportsAboutAddingTaskWithBlankTitle() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        manager.addTask(firstTask);

        String actualOutput = runConsole("add\n2\n    \nlist\nexit\n", manager);

        assertFalse(actualOutput.contains("Error: ID must be a number."));
        assertFalse(actualOutput.contains("Task not found"));
        assertFalse(actualOutput.contains("Task added"));
        assertFalse(firstTask.isDone());
        assertFalse(manager.getTasks().getFirst().isDone());
        assertTrue(actualOutput.contains("Title can't be empty"));
        assertTrue(actualOutput.contains("[ ] 1: clean room"));
        assertEquals(List.of(firstTask), manager.getTasks());
    }

    @Test
    @Timeout(1)
    void exportsFileOfTasksCorrectly(@TempDir Path tempDir) throws IOException {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        manager.addTask(firstTask);

        Path filePath = tempDir.resolve("report.txt");


        String actualOutput = runConsole("export\n" + filePath + "\nexit\n", manager);
        List<String> actualLines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

        assertTrue(actualOutput.contains("Report exported"));
        assertEquals(List.of("[ ] 1: clean room"), actualLines);
    }

    @Test
    @Timeout(1)
    void reportsAboutExportingWithInvalidPath() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        manager.addTask(firstTask);

        String invalidPath = "invalid\0path.txt";

        String actualOutput = runConsole("export\n" + invalidPath + "\nlist\nexit\n", manager);

        assertTrue(actualOutput.contains("Error: invalid path."));
        assertFalse(actualOutput.contains("Report exported"));
        assertTrue(actualOutput.contains("[ ] 1: clean room"));
    }

    @Test
    @Timeout(1)
    void reportsAboutExportingWithIOException(@TempDir Path tempDir){
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        manager.addTask(firstTask);

        String actualOutput = runConsole("export\n" + tempDir + "\nlist\nexit\n", manager);

        assertTrue(actualOutput.contains("Error: could not write report."));
        assertFalse(actualOutput.contains("Report exported"));
        assertTrue(actualOutput.contains("[ ] 1: clean room"));
    }


    @Test
    public void overwritesExistedReportCorrectly(@TempDir Path tempDir) throws IOException {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "помыть посуду");
        Task thirdTask = new Task(3, "vacuum hall");

        firstTask.complete();

        manager.addTask(firstTask);
        manager.addTask(secondTask);
        manager.addTask(thirdTask);

        Path filePath = tempDir.resolve("report.txt");
        Files.writeString(filePath, "old content");


        String actualOutput = runConsole("export\n" + filePath + "\nYeS  \nexit\n", manager);

        assertTrue(Files.exists(filePath));
        List<String> actualLines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        assertEquals(List.of("[x] 1: clean room",
                "[ ] 2: помыть посуду",
                "[ ] 3: vacuum hall"), actualLines);
        assertTrue(actualOutput.contains("File exists. Overwrite? (yes/no)"));
        assertTrue(actualOutput.contains("Report exported"));
    }


    @Test
    public void refusesOverwritingExistedReport(@TempDir Path tempDir) throws IOException {
        TaskManager manager = new TaskManager();

        Path filePath = tempDir.resolve("report.txt");
        Files.writeString(filePath, "old content");


        String actualOutput = runConsole("export\n" + filePath + "\nno \nlist\nexit\n", manager);

        assertTrue(Files.exists(filePath));
        String fileContent = Files.readString(filePath);

        assertEquals("old content", fileContent);
        assertTrue(actualOutput.contains("File exists. Overwrite? (yes/no)"));
        assertTrue(actualOutput.contains("Export cancelled."));
        assertFalse(actualOutput.contains("Report exported"));
        assertTrue(actualOutput.contains("There are no tasks yet"));
    }

    @Test
    @Timeout(1)
    void showsOnlyIncompleteTasks() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "помыть посуду");
        Task thirdTask = new Task(3, "vacuum hall");

        firstTask.complete();

        manager.addTask(firstTask);
        manager.addTask(secondTask);
        manager.addTask(thirdTask);

        String actualOutput = runConsole("incomplete\nexit\n", manager);

        assertTrue(actualOutput.contains("помыть посуду"));
        assertTrue(actualOutput.contains("vacuum hall"));
        assertFalse(actualOutput.contains("clean room"));
    }

    @Test
    @Timeout(1)
    void showsNonexistedIncompleteTasks() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "помыть посуду");
        Task thirdTask = new Task(3, "vacuum hall");

        firstTask.complete();
        secondTask.complete();
        thirdTask.complete();

        manager.addTask(firstTask);
        manager.addTask(secondTask);
        manager.addTask(thirdTask);

        String actualOutput = runConsole("incomplete\nlist\nexit\n", manager);

        assertTrue(actualOutput.contains("No incomplete tasks"));
        assertTrue(actualOutput.contains("помыть посуду"));
        assertTrue(actualOutput.contains("vacuum hall"));
        assertTrue(actualOutput.contains("clean room"));
    }

    @Test
    public void savesTwoCorrectTasks(@TempDir Path tempDir) throws IOException {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "помыть посуду");

        firstTask.complete();

        manager.addTask(firstTask);
        manager.addTask(secondTask);

        Path filePath = tempDir.resolve("report.txt");


        String actualOutput = runConsole("save\n" + filePath + "\nexit\n", manager);

        assertTrue(Files.exists(filePath));
        TaskFileStorage taskFileStorage = new TaskFileStorage();
        List<Task> loadedTasks = taskFileStorage.load(filePath);
        assertEquals(2, loadedTasks.size());

        assertEquals(
                new TaskSnapshot(1, "clean room", true),
                TaskSnapshot.fromTask(loadedTasks.get(0)));

        assertEquals(
                new TaskSnapshot(2, "помыть посуду", false),
                TaskSnapshot.fromTask(loadedTasks.get(1)));
        assertFalse(actualOutput.contains("File exists. Overwrite? (yes/no)"));
        assertTrue(actualOutput.contains("Tasks saved"));
    }


    @Test
    public void refusesOverwritingExistedSave(@TempDir Path tempDir) throws IOException {
        TaskManager manager = new TaskManager();

        Path filePath = tempDir.resolve("report.txt");
        Files.writeString(filePath, "old content");


        String actualOutput = runConsole("save\n" + filePath + "\nno \nlist\nexit\n", manager);

        assertTrue(Files.exists(filePath));
        String fileContent = Files.readString(filePath);

        assertEquals("old content", fileContent);

        assertTrue(Files.exists(filePath));
        assertTrue(actualOutput.contains("File exists. Overwrite? (yes/no)"));
        assertFalse(actualOutput.contains("Tasks saved"));
        assertTrue(actualOutput.contains("Save cancelled."));
        assertTrue(actualOutput.contains("There are no tasks yet"));
    }

    @Test
    public void loadsTwoTasksCorrectly(@TempDir Path tempDir) throws IOException {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(99, "clean room");

        firstTask.complete();

        manager.addTask(firstTask);

        Path filePath = tempDir.resolve("saved.txt");
        Files.writeString(filePath,
                """
                        count=2
                        task.0.id=1
                        task.0.done=true
                        task.0.title=Тест
                        task.1.id=2
                        task.1.done=false
                        task.1.title=Test
                        """,
                StandardCharsets.UTF_8);

        String actualOutput = runConsole("load\n" + filePath + "\nlist\nexit\n", manager);

        assertTrue(Files.exists(filePath));
        List<Task> actualTasks = manager.getTasks();
        assertEquals(2, actualTasks.size());

        assertEquals(
                new TaskSnapshot(1, "Тест", true),
                TaskSnapshot.fromTask(actualTasks.get(0)));

        assertEquals(
                new TaskSnapshot(2, "Test", false),
                TaskSnapshot.fromTask(actualTasks.get(1)));
        assertTrue(actualOutput.contains("Tasks loaded"));
        assertFalse(actualOutput.contains("Load cancelled."));
        assertTrue(actualOutput.contains("[x] 1: Тест"));
        assertTrue(actualOutput.contains("[ ] 2: Test"));
        assertFalse(actualOutput.contains("[x] 99: clean room"));
    }

    @Test
    public void refuseLoadingDueToIncorrectField(@TempDir Path tempDir) throws IOException {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(99, "clean room");

        firstTask.complete();

        manager.addTask(firstTask);

        Path filePath = tempDir.resolve("saved.txt");
        Files.writeString(filePath,
                """
                        count=2
                        task.0.id=1
                        task.0.done=true
                        task.0.title=Тест
                        task.1.id=2
                        task.1.done=maybe
                        task.1.title=Test
                        """,
                StandardCharsets.UTF_8);

        String actualOutput = runConsole("load\n" + filePath + "\nlist\nexit\n", manager);

        assertTrue(Files.exists(filePath));
        List<Task> actualTasks = manager.getTasks();
        assertEquals(1, actualTasks.size());

        assertEquals(
                new TaskSnapshot(99, "clean room", true),
                TaskSnapshot.fromTask(actualTasks.getFirst()));
        assertFalse(actualOutput.contains("Tasks loaded"));
        assertTrue(actualOutput.contains("Error: could not load tasks. Invalid boolean value for key: task.1.done"));
        assertFalse(actualOutput.contains("[x] 1: Тест"));
        assertFalse(actualOutput.contains("[ ] 2: Test"));
        assertTrue(actualOutput.contains("[x] 99: clean room"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"save", "load"})
    @Timeout(1)
    void leavesTasksUnchangedWhenPathInputEnds(String command) {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(99, "clean room");

        firstTask.complete();

        manager.addTask(firstTask);

        String actualOutput = runConsole(command + "\n", manager);

        assertEquals(1, manager.getTasks().size());

        assertEquals(
                new TaskSnapshot(99, "clean room", true),
                TaskSnapshot.fromTask(manager.getTasks().getFirst()));
        assertTrue(actualOutput.contains("Enter " + command + " path:"));
        assertFalse(actualOutput.contains("Tasks loaded"));
        assertFalse(actualOutput.contains("Error:"));
        assertFalse(actualOutput.contains("Tasks saved"));
    }


    @Test
    void showsHelpAndContinuesProcessingCommands() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(99, "clean room");

        firstTask.complete();

        manager.addTask(firstTask);

        String actualOutput = runConsole("help\nlist\nexit\n", manager);

        assertEquals(1, manager.getTasks().size());

        assertEquals(
                new TaskSnapshot(99, "clean room", true),
                TaskSnapshot.fromTask(manager.getTasks().getFirst()));
        assertTrue(actualOutput.contains("""   
                Available commands:
                add - Add a task; enter its ID and title when prompted.
                list - Show all tasks.
                incomplete - Show incomplete tasks.
                complete - Mark a task as completed; enter its ID when prompted.
                delete - Delete a task; enter its ID when prompted.
                rename - Rename a task; enter its ID and new title when prompted.
                export - Write a readable task report; enter the file path when prompted.
                save - Save tasks to a file for later loading; enter the file path when prompted.
                load - Replace current tasks with tasks from a file; enter the file path when prompted.
                help - Show this help.
                exit - Exit the program.
                clear-completed - Delete all completed tasks.
                """));
        assertFalse(actualOutput.contains("Unknown command"));
        assertFalse(actualOutput.contains("Error:"));
        assertTrue(actualOutput.contains("[x] 99: clean room"));
    }

    @Test
    void deletesCompletedTasksWithoutChangingExistingTasks() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "clean hall");
        Task thirdTask = new Task(3, "vacuum bedroom");
        Task fourthTask = new Task(4, "wash dishes");

        manager.addTask(firstTask);
        manager.addTask(secondTask);
        manager.addTask(thirdTask);
        manager.addTask(fourthTask);
        manager.completeTaskById(2);
        manager.completeTaskById(3);

        String actualOutput = runConsole("clear-completed\nlist\nexit\n", manager);

        assertTrue(actualOutput.contains("Completed tasks deleted: 2"));
        assertTrue(actualOutput.contains("[ ] 1: clean room"));
        assertFalse(actualOutput.contains("[x] 2: clean hall"));
        assertFalse(actualOutput.contains("[x] 3: vacuum bedroom"));
        assertTrue(actualOutput.contains("[ ] 4: wash dishes"));
        assertFalse(actualOutput.contains("Unknown command"));
        assertFalse(actualOutput.contains("Error:"));
        assertEquals(List.of(firstTask, fourthTask), manager.getTasks());
    }

    @Test
    void deletesCompletedTasksWhenTheyAreMissing() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "clean hall");

        manager.addTask(firstTask);
        manager.addTask(secondTask);

        String actualOutput = runConsole("clear-completed\nlist\nexit\n", manager);

        assertTrue(actualOutput.contains("Completed tasks deleted: 0"));
        assertTrue(actualOutput.contains("[ ] 1: clean room"));
        assertTrue(actualOutput.contains("[ ] 2: clean hall"));
        assertFalse(actualOutput.contains("Unknown command"));
        assertFalse(actualOutput.contains("Error:"));
        assertEquals(List.of(firstTask, secondTask), manager.getTasks());
    }

    @Test
    void restoresSavedTasksInNewConsoleSession (@TempDir Path tempDir) {
        TaskManager manager = new TaskManager();
        Path filePath = tempDir.resolve("saved.txt");
        String firstOutput = runConsole("add\n1\nread book\nadd\n2\nwash dishes\ncomplete\n1\nsave\n" + filePath + "\nexit\n", manager);

        TaskManager restoredManager = new TaskManager();
        String secondOutput = runConsole("load\n" + filePath + "\nlist\nexit\n", restoredManager);

        assertTrue(Files.exists(filePath));
        assertEquals(
                new TaskSnapshot(1, "read book", true),
                TaskSnapshot.fromTask(restoredManager.getTasks().getFirst()));
        assertEquals(
                new TaskSnapshot(2, "wash dishes", false),
                TaskSnapshot.fromTask(restoredManager.getTasks().getLast()));
        assertEquals(2, restoredManager.getTasks().size());
        assertFalse(firstOutput.contains("Unknown command"));
        assertFalse(firstOutput.contains("Error:"));
        assertFalse(secondOutput.contains("Unknown command"));
        assertFalse(secondOutput.contains("Error:"));
        assertTrue(firstOutput.contains("Tasks saved"));
        assertTrue(secondOutput.contains("Tasks loaded"));
        assertTrue(secondOutput.contains("[x] 1: read book"));
        assertTrue(secondOutput.contains("[ ] 2: wash dishes"));
    }


    private String runConsole(String input, TaskManager manager) {
        Scanner scanner = new Scanner(input);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        TaskConsole taskConsole = new TaskConsole(manager, scanner, output, new TaskReportWriter(), new TaskFileStorage());
        taskConsole.run();
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
