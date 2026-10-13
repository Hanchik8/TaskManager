package org.example;

import org.example.console.TaskConsole;
import org.example.export.TaskReportWriter;
import org.example.model.Task;
import org.example.model.TaskManager;
import org.example.storage.TaskFileStorage;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        manager.addTask(new Task(1, "clean room"));
        manager.addTask(new Task(2, "clean bathroom"));
        manager.completeTaskById(2);
        manager.addTask(new Task(3, "clean hall"));
        TaskConsole console = new TaskConsole(manager, new Scanner(System.in), System.out, new TaskReportWriter(), new TaskFileStorage());
        console.run();

    }
}
