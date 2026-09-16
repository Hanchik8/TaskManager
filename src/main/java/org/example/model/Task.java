package org.example.model;

public class Task {
    private final int id;
    private String title;
    private boolean isDone = false;


    public Task(int id, String title) {

        if (id <= 0) throw new IllegalArgumentException("ID must be positive: " + id);
        this.id = id;
        this.title = validateTitle(title);
    }

    public void complete() {
        isDone = true;
    }

    public void rename(String newTitle) {
        this.title = validateTitle(newTitle);
    }

    private static String validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title can't be empty");
        }
        return title;
    }

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean isDone() {
        return isDone;
    }

    @Override
    public String toString() {
        return this.id + " " + this.title + " " + this.isDone ;
    }

}
