package org.example.console;

import org.example.model.Task;
import org.example.model.TaskManager;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class TaskConsole {
    private final TaskManager manager;
    private final Scanner scanner;
    private final PrintStream output;

    public TaskConsole(TaskManager manager, Scanner scanner, PrintStream output) {
        this.manager = manager;
        this.scanner = scanner;
        this.output = output;
    }


    public void run() {
        output.println("Enter command:");

        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().strip();

            switch (command) {
                case "list" -> showTasks();
                case "exit" -> {
                    return;
                }
                case "complete" -> completeTask();
                case "delete" -> deleteTask();
                case "rename" -> renameTask();
                case "add" -> addTask();
                default -> output.println("Unknown command");
            }

            output.println("Enter command:");
        }
    }

    private void showTasks() {
        List<Task> tasks = this.manager.getTasks();
        if (tasks.isEmpty()) {
            output.println("There are no tasks yet");
            return;
        }

        for (Task task : tasks) {
            if (task.isDone()) {
                output.println("[x] " + task.getId() + ": " + task.getTitle());
            } else {
                output.println("[ ] " + task.getId() + ": " + task.getTitle());
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


}
