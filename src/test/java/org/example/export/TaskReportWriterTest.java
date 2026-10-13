package org.example.export;

import org.example.model.Task;
import org.example.presentation.TaskFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TaskReportWriterTest {

    @Test
    public void testTaskReportWriter(@TempDir Path tempDir) throws IOException {
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "помыть посуду");
        firstTask.complete();

        List<Task> tasks = new ArrayList<>();
        tasks.add(firstTask);
        tasks.add(secondTask);

        TaskReportWriter taskReportWriter = new TaskReportWriter();
        Path filePath = tempDir.resolve("report.txt");

        taskReportWriter.write(filePath, tasks);

        List<String> actualLines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

        List<String> expectedLines = List.of(
                "[x] 1: clean room",
                "[ ] 2: помыть посуду"
        );

        assertEquals(expectedLines, actualLines);
    }

    @Test
    public void testTaskReportWriterWithBlankTasks(@TempDir Path tempDir) throws IOException {
        Task firstTask = new Task(1, "clean room");
        Task secondTask = new Task(2, "помыть посуду");
        firstTask.complete();

        List<Task> tasks = new ArrayList<>();
        tasks.add(firstTask);
        tasks.add(secondTask);

        TaskReportWriter taskReportWriter = new TaskReportWriter();
        Path filePath = tempDir.resolve("report.txt");
        taskReportWriter.write(filePath, tasks);

        List<Task> blankTasks = new ArrayList<>();
        taskReportWriter.write(filePath, blankTasks);

        List<String> actualLines = Files.readAllLines(filePath, StandardCharsets.UTF_8);

        assertEquals(List.of(), actualLines);
    }


}
