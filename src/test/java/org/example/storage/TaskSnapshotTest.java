package org.example.storage;

import org.example.model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskSnapshotTest {

    @Test
    public void testTaskSnapshot() {
        Task task = new Task(1, "wash dishes");
        TaskSnapshot taskSnapshot = TaskSnapshot.fromTask(task);
        task.complete();
        task.rename("clean floor");
        assertEquals("wash dishes", taskSnapshot.title());
        assertEquals(1, taskSnapshot.id());
        assertFalse(taskSnapshot.done());
        assertEquals("clean floor", task.getTitle());
    }

    @Test
    public void restoresCompletedTaskAsNewObject() {
        Task task = new Task(1, "wash dishes");
        task.complete();
        TaskSnapshot taskSnapshot = TaskSnapshot.fromTask(task);
        Task restoredTask = taskSnapshot.toTask();
        assertNotSame(task, restoredTask);
        assertEquals("wash dishes", restoredTask.getTitle());
        assertEquals(1, restoredTask.getId());
        assertTrue(restoredTask.isDone());

    }
}
