package org.example.console;

import org.example.export.TaskReportWriter;
import org.example.model.Task;
import org.example.model.TaskManager;
import org.example.presentation.TaskFormatter;
import org.example.storage.TaskFileStorage;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class TaskConsole {
    private final TaskManager manager;
    private final Scanner scanner;
    private final PrintStream output;
    private final TaskReportWriter taskReportWriter;
    private final TaskFileStorage taskFileStorage;

    public TaskConsole(TaskManager manager, Scanner scanner, PrintStream output, TaskReportWriter taskReportWriter, TaskFileStorage taskFileStorage) {
        this.manager = manager;
        this.scanner = scanner;
        this.output = output;
        this.taskReportWriter = taskReportWriter;
        this.taskFileStorage = taskFileStorage;
    }


    public void run() {
        output.println("Enter command:");

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().strip();

            switch (command) {
                case "list" -> showTasks(manager.getTasks(), "There are no tasks yet");
                case "incomplete" -> showTasks(manager.getIncompleteTasks(), "No incomplete tasks");
                case "exit" -> {
                    return;
                }
                case "complete" -> completeTask();
                case "delete" -> deleteTask();
                case "rename" -> renameTask();
                case "add" -> addTask();
                case "export" -> exportTasks();
                case "save" -> saveTasks();
                case "load" -> loadTasks();
                case "help" -> showHelp();
                case "clear-completed" -> deleteCompletedTasks();
                default -> output.println("Unknown command");
            }

            output.println("Enter command:");
        }
    }

    private void showTasks(List<Task> tasks, String emptyMessage) {
        if (tasks.isEmpty()) {
            output.println(emptyMessage);
        } else {
            for (Task task : tasks) {
                output.println(TaskFormatter.format(task));
            }
        }
    }

    private void completeTask() {
        Optional<Integer> id = readTaskId();
        if (id.isPresent()) {
            if (this.manager.completeTaskById(id.orElseThrow())) {
                output.println("Task completed");
            } else {
                output.println("Task not found");
            }
        }
    }

    private void deleteTask() {
        Optional<Integer> id = readTaskId();
        if (id.isPresent()) {
            if (this.manager.deleteTaskById(id.orElseThrow())) {
                output.println("Task deleted");
            } else {
                output.println("Task not found");
            }
        }
    }

    private Optional<Integer> readTaskId() {
        output.println("Enter task ID:");
        if (scanner.hasNextLine()) {
            String input = scanner.nextLine().trim();
            try {
                return Optional.of(Integer.parseInt(input));
            } catch (NumberFormatException e) {
                output.println("Error: ID must be a number.");
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private void renameTask() {
        Optional<Integer> id = readTaskId();
        if (id.isPresent()) {
            output.println("Enter new title:");
            if (scanner.hasNextLine()) {
                String newTitle = scanner.nextLine();
                try {
                    if (this.manager.renameTaskById(id.get(), newTitle)) {
                        output.println("Task renamed");
                    } else {
                        output.println("Task not found");
                    }
                } catch (IllegalArgumentException e) {
                    output.println("Error: title must not be blank.");
                }
            }
        }
    }

    private void addTask() {
        Optional<Integer> id = readTaskId();
        if (id.isPresent()) {
            output.println("Enter title:");
            if (scanner.hasNextLine()) {
                String title = scanner.nextLine();
                try {
                    this.manager.addTask(new Task(id.get(), title));
                    output.println("Task added");
                } catch (IllegalArgumentException e) {
                    output.println("Error: " + e.getMessage());
                }
            }
        }
    }

    private void exportTasks() {
        try {
            Optional<Path> path = readPath("Enter report path:");
            if (path.isEmpty()) {
                return;
            }
            Path reportPath = path.orElseThrow();
            if (Files.isRegularFile(reportPath)) {
                if (!confirmOverwrite("Export cancelled.")) {
                    return;
                }
            }
            taskReportWriter.write(reportPath, manager.getTasks());
            output.println("Report exported");
        } catch (IOException e) {
            output.println("Error: could not write report.");
        }
    }

    private Optional<Path> readPath(String prompt) {
        output.println(prompt);
        if (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            try {
                Path path = Path.of(input);
                return Optional.of(path);
            } catch (InvalidPathException e) {
                output.println("Error: invalid path.");
            }
        }
        return Optional.empty();
    }

    private boolean confirmOverwrite(String cancellationMessage) {
        output.println("File exists. Overwrite? (yes/no)");
        if (scanner.hasNextLine()) {
            String answer = scanner.nextLine().strip();
            if (!answer.equalsIgnoreCase("yes")) {
                output.println(cancellationMessage);
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private void saveTasks() {
        Optional<Path> path = readPath("Enter save path:");
        if (path.isEmpty()) {
            return;
        }
        Path savePath = path.orElseThrow();
        if (Files.isRegularFile(savePath)) {
            if (!confirmOverwrite("Save cancelled.")) {
                return;
            }
        }
        try {
            taskFileStorage.save(savePath, manager.getTasks());
            output.println("Tasks saved");
        } catch (IOException e) {
            output.println("Error: could not save tasks.");
        }
    }

    private void loadTasks() {
        Optional<Path> path = readPath("Enter load path:");
        if (path.isEmpty()) {
            return;
        }
        try {
            Path loadPath = path.orElseThrow();
            List<Task> loadedTasks = taskFileStorage.load(loadPath);
            manager.replaceTasks(loadedTasks);
            output.println("Tasks loaded");
        } catch (IOException e) {
           output.println("Error: could not load tasks. " + e.getMessage());
        }
    }

    private void showHelp() {
        output.println("""
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
                """);
    }

    private void deleteCompletedTasks() {
        output.println("Completed tasks deleted: " +manager.deleteCompletedTasks());
    }

}
