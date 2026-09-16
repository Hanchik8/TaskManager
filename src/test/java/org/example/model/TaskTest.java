package org.example.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void rejectsZeroId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Task(0, "clean room")
        );
    }

    @Test
    void rejectsNegativeId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Task(-1, "clean room")
        );
    }
    @Test
    void rejectsNullTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Task(1, null)
        );
    }

    @Test
    void rejectsEmptyTitle() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Task(1, "    ")
        );
    }

    @Test
    void createsTaskWithValidData() {
        Task task = assertDoesNotThrow(
                () -> new Task(1, "clean room")
        );

        assertEquals(1, task.getId());
        assertEquals("clean room", task.getTitle());
    }

    @Test
    void newTaskStartsIncomplete() {
        Task task = new Task(1, "clean room");

        assertFalse(task.isDone());
    }

    @Test
    void completeMarksTaskAsDone() {
        Task task = new Task(1, "clean room");
        task.complete();
        assertTrue(task.isDone());
    }

    @Test
    void renamesTaskCorrectly() {
        Task task = new Task(1, "clean room");

        task.rename("wash dishes");

        assertEquals("wash dishes", task.getTitle());
    }

    @Test
    void rejectsBlankTitleWhenRenaming() {
        Task task = new Task(1, "clean room");

        assertThrows(
                IllegalArgumentException.class,
                () -> task.rename("   ")
        );

        assertEquals("clean room", task.getTitle());
    }


}