/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.spine3.examples.todolist;

import com.beust.jcommander.Parameter;
import com.google.protobuf.Timestamp;
import org.spine3.examples.todolist.converters.ColorConverter;
import org.spine3.examples.todolist.converters.PriorityConverter;
import org.spine3.examples.todolist.converters.TaskIdConverter;
import org.spine3.examples.todolist.converters.TaskLabelIdConverter;
import org.spine3.examples.todolist.converters.TimeConverter;

/**
 * @author Illia Shepilov
 */
public class Settings {

    @Parameter(names = "--task-id", description = "A task identifier", converter = TaskIdConverter.class)
    private TaskId taskId;

    @Parameter(names = "--label-id", description = "A label identifier", converter = TaskLabelIdConverter.class)
    private TaskLabelId labelId;

    @Parameter(names = "--description", description = "A description for the task")
    private String description;

    @Parameter(names = "--priority", description = "A priority for the task", converter = PriorityConverter.class)
    private TaskPriority priority;

    @Parameter(names = "--due-date", description = "A task due date", converter = TimeConverter.class)
    private Timestamp dueDate;

    @Parameter(names = "--title", description = "A label title")
    private String title;

    @Parameter(names = "--color", description = "A label color", converter = ColorConverter.class)
    private LabelColor color;

    public TaskId getTaskId() {
        return taskId;
    }

    public TaskLabelId getLabelId() {
        return labelId;
    }

    public String getDescription() {
        return description;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public Timestamp getDueDate() {
        return dueDate;
    }

    public String getTitle() {
        return title;
    }

    public LabelColor getColor() {
        return color;
    }
}
