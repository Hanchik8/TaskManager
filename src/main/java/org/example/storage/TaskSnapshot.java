package org.example.storage;

import org.example.model.Task;

public record TaskSnapshot(int id, String title, boolean done) {


    public static TaskSnapshot fromTask(Task task) {
        if (task == null) throw new IllegalArgumentException("Task can't be null");
        return new TaskSnapshot(task.getId(), task.getTitle(), task.isDone());
    }

    public Task toTask() {
        Task task = new Task(this.id(), this.title());
        if (this.done) task.complete();
        return task;
    }
}
