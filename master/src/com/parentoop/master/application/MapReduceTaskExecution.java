package com.parentoop.master.application;

import com.google.common.collect.Lists;
import com.parentoop.core.loader.Task;
import com.parentoop.master.application.phases.MappingPhase;
import com.parentoop.master.application.phases.ReducingPhase;
import com.parentoop.master.application.phases.SetupPhase;
import com.parentoop.master.execution.TaskExecution;

import java.nio.file.Path;

public class MapReduceTaskExecution extends TaskExecution<Path> {

    public MapReduceTaskExecution(Path inputPath, Task task, TaskExecutionListener<Path> listener) {
        super(task, Lists.newArrayList(new SetupPhase(), new MappingPhase(inputPath), new ReducingPhase()), listener);
    }
}
