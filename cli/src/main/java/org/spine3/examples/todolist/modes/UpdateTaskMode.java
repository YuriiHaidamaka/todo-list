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
import org.spine3.change.StringChange;
import org.spine3.change.TimestampChange;
import org.spine3.examples.todolist.PriorityChange;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.client.TodoClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;

import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;

/**
 * @author Illia Shepilov
 */
public class UpdateTaskMode {

    private static final String ENTER_NEW_DESCRIPTION_MESSAGE = "Please enter the new task description: ";
    private static final String ENTER_PREVIOUS_DESCRIPTION_MESSAGE = "Please enter the previous task description: ";
    private static final String ENTER_ID_MESSAGE = "Please enter the task id: ";
    private static final String ENTER_NEW_PRIORITY_MESSAGE = "Please enter the new task priority: ";
    private static final String ENTER_PREVIOUS_PRIORITY_MESSAGE = "Please enter the previous task priority: ";
    private static final String ENTER_NEW_DATE_MESSAGE = "Please enter the new task due date: ";
    private static final String ENTER_PREVIOUS_DATE_MESSAGE = "Please enter the previous task due date: ";
    private final TodoClient client;
    private static final String HELP_MESSAGE = "0:    Help.\n" +
            "1:    Update the task description.\n" +
            "2:    Update the task priority.\n" +
            "3:    Update the task due date.\n" +
            "exit: Exit from the mode.";
    private BufferedReader reader;

    UpdateTaskMode(TodoClient client, BufferedReader reader) {
        this.client = client;
        this.reader = reader;
    }

    @Command(abbrev = "0")
    public void help() {
        System.out.println(HELP_MESSAGE);
    }

    @Command(abbrev = "1")
    public void updateTaskDescription() throws IOException {
        sendMessageToUser(ENTER_ID_MESSAGE);
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(reader.readLine())
                                    .build();
        sendMessageToUser(ENTER_NEW_DESCRIPTION_MESSAGE);
        final String newDescription = reader.readLine();
        sendMessageToUser(ENTER_PREVIOUS_DESCRIPTION_MESSAGE);
        final String previousDescription = reader.readLine();

        final StringChange change = StringChange.newBuilder()
                                                .setNewValue(newDescription)
                                                .setPreviousValue(previousDescription)
                                                .build();
        final UpdateTaskDescription updateTaskDescription = UpdateTaskDescription.newBuilder()
                                                                                 .setDescriptionChange(change)
                                                                                 .setId(taskId)
                                                                                 .build();
        client.update(updateTaskDescription);
    }

    @Command(abbrev = "2")
    public void updateTaskPriority() throws IOException {
        sendMessageToUser(ENTER_ID_MESSAGE);
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(reader.readLine())
                                    .build();
        sendMessageToUser(ENTER_NEW_PRIORITY_MESSAGE);
        final TaskPriority newTaskPriority = TaskPriority.valueOf(reader.readLine());
        sendMessageToUser(ENTER_PREVIOUS_PRIORITY_MESSAGE);
        final TaskPriority previousTaskPriority = TaskPriority.valueOf(reader.readLine());

        final PriorityChange change = PriorityChange.newBuilder()
                                                    .setPreviousValue(previousTaskPriority)
                                                    .setNewValue(newTaskPriority)
                                                    .build();
        final UpdateTaskPriority updateTaskPriority = UpdateTaskPriority.newBuilder()
                                                                        .setPriorityChange(change)
                                                                        .setId(taskId)
                                                                        .build();
        client.update(updateTaskPriority);
    }

    @Command(abbrev = "3")
    public void updateTaskDueDate() throws IOException, ParseException {
        sendMessageToUser(ENTER_ID_MESSAGE);
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(reader.readLine())
                                    .build();
        sendMessageToUser(ENTER_NEW_DATE_MESSAGE);
        final Timestamp newDueDate = Timestamps.parse(reader.readLine());
        sendMessageToUser(ENTER_PREVIOUS_DATE_MESSAGE);
        final Timestamp previousDueDate = Timestamps.parse(reader.readLine());

        final TimestampChange change = TimestampChange.newBuilder()
                                                      .setPreviousValue(previousDueDate)
                                                      .setNewValue(newDueDate)
                                                      .build();
        final UpdateTaskDueDate updateTaskDueDate = UpdateTaskDueDate.newBuilder()
                                                                     .setDueDateChange(change)
                                                                     .setId(taskId)
                                                                     .build();
        client.update(updateTaskDueDate);
    }
}
