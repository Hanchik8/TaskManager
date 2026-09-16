package org.example;

import org.example.model.Task;
import org.example.model.TaskManager;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();
        manager.addTask(new Task(1, "clean room"));
        manager.addTask(new Task(2, "clean bathroom"));
        System.out.println(manager.completeTaskById(2));
        System.out.println(manager.completeTaskById(99));
        manager.addTask(new Task(3, "clean hall"));
        showTasks(manager.getTasks());
        System.out.println(manager.deleteTaskById(3));
        showTasks(manager.getTasks());

    }

    private static void showTasks(List<Task> tasks) {
        for (Task task : tasks) {
            System.out.println(task);
        }
    }
}
