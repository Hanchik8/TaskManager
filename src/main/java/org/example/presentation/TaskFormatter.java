package org.example.presentation;

import org.example.model.Task;

public class TaskFormatter {
    public static String format(Task task) {
        if (task.isDone()) return "[x] " + task.getId() + ": " + task.getTitle();
        return "[ ] " + task.getId() + ": " + task.getTitle();
    }
}
