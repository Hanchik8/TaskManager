package org.example.model;

import java.lang.reflect.Array;
import java.util.*;

public class TaskManager {
    private final List<Task> tasks = new ArrayList<>();

    public boolean completeTaskById(int id) {
        Optional<Task> foundTask = findTaskById(id);
        if (foundTask.isEmpty())
            return false;
        foundTask.orElseThrow().complete();
        return true;
    }

    public boolean renameTaskById(int id, String newTitle) {
        Optional<Task> foundTask = findTaskById(id);
        if (foundTask.isEmpty())
            return false;
        foundTask.orElseThrow().rename(newTitle);
        return true;
    }

    public List<Task> getIncompleteTasks() {
        List<Task> incompleteTasks = new ArrayList<>();
        for (Task task : this.tasks) {
            if (!task.isDone()) {
                incompleteTasks.add(task);
            }
        }
        return List.copyOf(incompleteTasks);
    }

    public void addTask(Task task) {
        if (task == null) throw new IllegalArgumentException("Task can't be null");
        int newTaskId = task.getId();
        for (Task existingTask : this.tasks) {
            if (existingTask.getId() == newTaskId) {
                throw new IllegalArgumentException("Task with ID " + newTaskId + " already exists.");
            }
        }
        this.tasks.add(task);
    }

    public List<Task> getTasks() {
        return List.copyOf(this.tasks);
    }

    public boolean deleteTaskById(int id) {
        Iterator<Task> it = this.tasks.iterator();
        while (it.hasNext()) {
            int taskId = it.next().getId();
            if (taskId == id) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public Optional<Task> findTaskById(int id) {
        for (Task task : this.tasks) {
            if (task.getId() == id) {
                return Optional.of(task);
            }
        }
        return Optional.empty();
    }

    public void replaceTasks(List<Task> newTasks) {
        if (newTasks == null) {
            throw new IllegalArgumentException("New List can't be null");
        }
        TaskManager validatedTasks = new TaskManager();
        for (Task task : newTasks) {
            validatedTasks.addTask(task);
        }
        this.tasks.clear();
        this.tasks.addAll(validatedTasks.getTasks());
    }

    public int deleteCompletedTasks() {
        int initialSize = tasks.size();
        tasks.removeIf(Task::isDone);
        return initialSize - tasks.size();
    }

}
