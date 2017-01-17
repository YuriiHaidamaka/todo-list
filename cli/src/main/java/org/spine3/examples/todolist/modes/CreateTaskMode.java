/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.modes;

import asg.cliche.Command;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import org.spine3.change.TimestampChange;
import org.spine3.examples.todolist.PriorityChange;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.client.TodoClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;

import static org.spine3.base.Identifiers.newUuid;

/**
 * @author Illia Shepilov
 */
public class CreateTaskMode {

    private static final String HELP_COMMAND = "0:  Help\n" +
            "1:  Enter the task description\n" +
            "2:  Enter the task due date\n" +
            "3:  Enter the task priority\n" +
            "4:  Create task with specified parameters[description is required]";
    private static final String ENTER_DESCRIPTION_MESSAGE = "Please enter the task description";
    private static final String ENTER_DUE_DATE_MESSAGE = "Please enter the task due date";
    private static final String ENTER_PRIORITY_MESSAGE = "Please enter the one of the available task priority:\n" +
            "LOW\nNORMAL\nHIGH";
    private String description;
    private Timestamp dueDate;
    private TaskPriority priority;
    private TodoClient client;
    private BufferedReader reader;

    public CreateTaskMode(TodoClient client, BufferedReader reader) {
        this.client = client;
        this.reader = reader;
    }

    @Command(abbrev = "0")
    public void help() {
        System.out.println(HELP_COMMAND);
    }

    @Command(abbrev = "1")
    public void enterTaskDescription() throws IOException {
        System.out.println(ENTER_DESCRIPTION_MESSAGE);
        final String description = reader.readLine();
        this.description = description;
    }

    @Command(abbrev = "2")
    public void enterTaskDueDate() throws IOException, ParseException {
        System.out.println(ENTER_DUE_DATE_MESSAGE);
        final String dueDate = reader.readLine();
        final Timestamp result = Timestamps.parse(dueDate);
        this.dueDate = result;
    }

    @Command(abbrev = "3")
    public void enterTaskPriority() throws IOException {
        System.out.println(ENTER_PRIORITY_MESSAGE);
        final String priority = reader.readLine();
        this.priority = TaskPriority.valueOf(priority.toUpperCase());
    }

    @Command(abbrev = "4")
    public void createTask() {
        setDefaultParameterValues();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        final CreateBasicTask createTask = CreateBasicTask.newBuilder()
                                                          .setId(taskId)
                                                          .setDescription(description)
                                                          .build();
        client.create(createTask);

        if (priority != null) {
            final PriorityChange change = PriorityChange.newBuilder()
                                                        .setNewValue(priority)
                                                        .build();
            final UpdateTaskPriority updateTaskPriority = UpdateTaskPriority.newBuilder()
                                                                            .setId(taskId)
                                                                            .setPriorityChange(change)
                                                                            .build();
            client.update(updateTaskPriority);
        }

        if (dueDate != null) {
            final TimestampChange change = TimestampChange.newBuilder()
                                                          .setNewValue(dueDate)
                                                          .build();
            final UpdateTaskDueDate updateTaskDueDate = UpdateTaskDueDate.newBuilder()
                                                                         .setId(taskId)
                                                                         .setDueDateChange(change)
                                                                         .build();
            client.update(updateTaskDueDate);
        }
        final String result = String.format("Created task with parameters:\nid: %s\ndescription: %s\npriority: %s\ndue date: %s",
                                            taskId.getValue(), description, priority, dueDate);
        System.out.println(result);
    }

    private void setDefaultParameterValues() {
        description = "";
        priority = TaskPriority.TP_UNDEFINED;
        dueDate = Timestamp.getDefaultInstance();
    }
}
