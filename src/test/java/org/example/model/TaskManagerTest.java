package org.example.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTest {

    @Test
    void addsTaskAndMakesItAvailable() {
        TaskManager manager = new TaskManager();
        Task task = new Task(1, "clean room");

        manager.addTask(task);

        assertEquals(List.of(task), manager.getTasks());
    }

    @Test
    void rejectsSameTaskId() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(1, "clean hall");

        manager.addTask(firstTask);

        assertThrows(IllegalArgumentException.class, () -> manager.addTask(secondTask));
    }

   @Test
    void completeTaskById() {
       TaskManager manager = new TaskManager();
       Task firstTask = new Task(2, "clean room");

       manager.addTask(firstTask);

       assertTrue(manager.completeTaskById(2));
       assertTrue(firstTask.isDone());
       assertFalse(manager.completeTaskById(99));
   }

   @Test
    void deleteTaskById() {
       TaskManager manager = new TaskManager();
       Task firstTask = new Task(1, "clean room");

       manager.addTask(firstTask);

       assertTrue(manager.deleteTaskById(1));
       assertFalse(manager.deleteTaskById(99));
   }

   @Test
    void findNonexistentTask() {
       TaskManager manager = new TaskManager();

       assertTrue(manager.findTaskById(1).isEmpty());
   }

   @Test
    void findExistentTask() {
       TaskManager manager = new TaskManager();
       Task firstTask = new Task(1, "clean room");

       manager.addTask(firstTask);

       Task task = manager.findTaskById(1).orElseThrow();

       assertSame(firstTask, task);
   }

    @Test
    void renamesExistedTaskByIdCorrectly() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");

        manager.addTask(firstTask);

        assertTrue(manager.renameTaskById(1, "wash dishes"));
        assertEquals("wash dishes", firstTask.getTitle());
    }

    @Test
    void tryRenameByIdNonexistedTask() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");

        manager.addTask(firstTask);

        assertFalse(manager.renameTaskById(2, "wash dishes"));
        assertEquals("clean room", firstTask.getTitle());
    }

    @Test
    void rejectsBlankTitleWhenRenamingById() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");

        manager.addTask(firstTask);

        assertThrows(IllegalArgumentException.class, () -> manager.renameTaskById(1, "     "));
        assertEquals("clean room", firstTask.getTitle());
    }

    @Test
    void returnListWithIncompletedTasks() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "clean hall");
        Task thirdTask = new Task(3, "vacuum the room");

        manager.addTask(firstTask);
        manager.addTask(secondTask);
        manager.addTask(thirdTask);

        manager.completeTaskById(2);

        List<Task> result = manager.getIncompleteTasks();

        assertEquals(List.of(firstTask, secondTask, thirdTask), manager.getTasks());
        assertEquals(List.of(firstTask, thirdTask), result);
    }

    @Test
    void returnEmptyListOfTasks() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "clean hall");

        manager.addTask(firstTask);
        manager.addTask(secondTask);

        manager.completeTaskById(1);
        manager.completeTaskById(2);

        assertTrue(manager.getIncompleteTasks().isEmpty());
    }

    @Test
    void rejectsChangingListOfIncompletedTasks() {
        TaskManager manager = new TaskManager();
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "clean hall");

        manager.addTask(firstTask);
        manager.addTask(secondTask);

        List<Task> result = manager.getIncompleteTasks();

        assertThrows(UnsupportedOperationException.class, () -> {
            result.add(new Task(99, "new task"));
        });
    }
}

