package org.example.presentation;

import org.example.model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskFormatterTest {


    @Test
    public void testIncompletedFormatTask() {
    Task task = new Task(1, "clean room");
    assertEquals("[ ] 1: clean room", TaskFormatter.format(task));
    }


    @Test
    public void testCompletedFormatTask() {
        Task task = new Task(1, "clean room");
        task.complete();
        assertEquals("[x] 1: clean room", TaskFormatter.format(task));
    }

}
