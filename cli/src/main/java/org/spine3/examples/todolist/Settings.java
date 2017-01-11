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

/**
 * @author Illia Shepilov
 */
public class Settings {

    @Parameter(names = "-taskId", description = "A task identifier")
    private TaskId taskId;

    @Parameter(names = "-labelId", description = "A label identifier")
    private TaskLabelId labelId;

    @Parameter(names = {"-description"}, description = "A description for the task", required = true)
    private String description;

    @Parameter(names = "-priority", description = "A priority for the task", required = true, converter = PriorityConverter.class)
    private TaskPriority priority;

    @Parameter(names = "-dueDate", description = "A task due date", required = true, converter = TimeConverter.class)
    private Timestamp dueDate;

    @Parameter(names = "-title", description = "A label title", required = true)
    private String title;

    @Parameter(names = "-color", description = "A label color", converter = ColorConverter.class)
    private LabelColor color;
}