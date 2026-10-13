package org.example.export;

import org.example.model.Task;
import org.example.presentation.TaskFormatter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TaskReportWriter {
    public void write(Path path, List<Task> tasks) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(TaskFormatter.format(task));
        }
        Files.write(path, lines, StandardCharsets.UTF_8);
    }
}
