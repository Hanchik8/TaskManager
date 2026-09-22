package org.example.console;

import org.example.model.Task;
import org.example.model.TaskManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
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

    private String runConsole(String input, TaskManager manager) {
        Scanner scanner = new Scanner(input);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        TaskConsole taskConsole = new TaskConsole(manager, scanner, output);
        taskConsole.run();
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
