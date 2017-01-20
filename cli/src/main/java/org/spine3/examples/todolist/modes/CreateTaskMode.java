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
import jline.console.ConsoleReader;
import org.spine3.change.StringChange;
import org.spine3.change.TimestampChange;
import org.spine3.examples.todolist.PriorityChange;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.FinalizeDraft;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.client.TodoClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.DateHelper.getDateFormat;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.CHANGED_DESCRIPTION_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.CHANGED_DUE_DATE_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.CHANGED_PRIORITY_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.CREATED_DRAFT_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.CREATED_TASK_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.DRAFT_FINALIZED_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.EMPTY;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.HELP_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.NEED_TO_FINALIZE_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.POSITIVE_ANSWER;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.SET_DESCRIPTION_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.SET_DUE_DATE_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.SET_PRIORITY_MESSAGE;
import static org.spine3.examples.todolist.modes.ModeHelper.constructUserFriendlyDate;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
public class CreateTaskMode extends Mode {

    private Timestamp dueDate = Timestamp.getDefaultInstance();
    private TaskPriority priority = TaskPriority.TP_UNDEFINED;
    private String description;

    CreateTaskMode(TodoClient client, ConsoleReader reader) {
        super(client, reader);
    }

    @Override
    void start() throws IOException {
        String line;
        while ((line = reader.readLine())!=null){
            if(line.equals("back")){
                return;
            }

        }
    }

    @Command(abbrev = "0")
    public void help() {
        sendMessageToUser(HELP_MESSAGE);
    }

    @Command(abbrev = "1")
    public void setTaskDescription() throws IOException {
        final String description = obtainDescriptionValue(SET_DESCRIPTION_MESSAGE, true);
        this.description = description;
        final String message = CHANGED_DESCRIPTION_MESSAGE + description;
        sendMessageToUser(message);
    }

    @Command(abbrev = "2")
    public void setTaskDueDate() throws IOException, ParseException {
        final String dueDateValue = obtainDueDateValue(SET_DUE_DATE_MESSAGE, true);
        final SimpleDateFormat simpleDateFormat = getDateFormat();
        final long dueDateInMS = simpleDateFormat.parse(dueDateValue)
                                                 .getTime();
        final Timestamp dueDate = Timestamps.fromMillis(dueDateInMS);
        this.dueDate = dueDate;
        final String message = CHANGED_DUE_DATE_MESSAGE + constructUserFriendlyDate(dueDateInMS);
        sendMessageToUser(message);
    }

    @Command(abbrev = "3")
    public void setTaskPriority() throws IOException {
        final String priority = obtainPriorityValue(SET_PRIORITY_MESSAGE);
        this.priority = TaskPriority.valueOf(priority.toUpperCase());
        final String message = CHANGED_PRIORITY_MESSAGE + priority;
        sendMessageToUser(message);
    }

    @Command(abbrev = "4")
    public void createTaskDraft() throws IOException {
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        createTaskDraft(taskId);
        updatePriorityIfNeeded(taskId);
        updateDueDateIfNeeded(taskId);
        final String userFriendlyDate = constructUserFriendlyDate(Timestamps.toMillis(dueDate));
        final String idValue = taskId.getValue();
        final String result = String.format(CREATED_DRAFT_MESSAGE, idValue, description, priority, userFriendlyDate);
        sendMessageToUser(result);
        finalizeDraftIfNeeded(taskId);
        clearValues();
    }

    @Command(abbrev = "5")
    public void createTask() throws IOException {
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        createTask(taskId);
        updatePriorityIfNeeded(taskId);
        updateDueDateIfNeeded(taskId);
        final String userFriendlyDate = constructUserFriendlyDate(Timestamps.toMillis(dueDate));
        final String idValue = taskId.getValue();
        final String result = String.format(CREATED_TASK_MESSAGE, idValue, description, priority, userFriendlyDate);
        sendMessageToUser(result);
        clearValues();
    }

    private void createTaskDraft(TaskId taskId) throws IOException {
        final boolean isValid = descriptionValidator.validate(description);
        if (!isValid) {
            setTaskDescription();
        }

        final CreateDraft createTask = CreateDraft.newBuilder()
                                                  .setId(taskId)
                                                  .build();
        client.create(createTask);
        final StringChange change = StringChange.newBuilder()
                                                .setPreviousValue(EMPTY)
                                                .setNewValue(description)
                                                .build();
        final UpdateTaskDescription updateTaskDescription = UpdateTaskDescription.newBuilder()
                                                                                 .setId(taskId)
                                                                                 .setDescriptionChange(change)
                                                                                 .build();
        client.update(updateTaskDescription);
    }

    private void createTask(TaskId taskId) throws IOException {
        final boolean isValid = descriptionValidator.validate(description);
        if (!isValid) {
            setTaskDescription();
        }

        final CreateBasicTask createTask = CreateBasicTask.newBuilder()
                                                          .setId(taskId)
                                                          .setDescription(description)
                                                          .build();
        client.create(createTask);
    }

    private void finalizeDraftIfNeeded(TaskId taskId) throws IOException {
        sendMessageToUser(NEED_TO_FINALIZE_MESSAGE);
        final String input = reader.readLine();
        final boolean isValid = approveValidator.validate(input);
        if (input.equals(POSITIVE_ANSWER)) {
            final FinalizeDraft finalizeDraft = FinalizeDraft.newBuilder()
                                                             .setId(taskId)
                                                             .build();
            client.finalize(finalizeDraft);
            sendMessageToUser(DRAFT_FINALIZED_MESSAGE);
        }
    }

    private void updateDueDateIfNeeded(TaskId taskId) {
        if (dueDate != Timestamp.getDefaultInstance()) {
            final TimestampChange change = TimestampChange.newBuilder()
                                                          .setNewValue(dueDate)
                                                          .build();
            final UpdateTaskDueDate updateTaskDueDate = UpdateTaskDueDate.newBuilder()
                                                                         .setId(taskId)
                                                                         .setDueDateChange(change)
                                                                         .build();
            client.update(updateTaskDueDate);
        }
    }

    private void updatePriorityIfNeeded(TaskId taskId) {
        if (priority != TaskPriority.TP_UNDEFINED) {
            final PriorityChange change = PriorityChange.newBuilder()
                                                        .setNewValue(priority)
                                                        .build();
            final UpdateTaskPriority updateTaskPriority = UpdateTaskPriority.newBuilder()
                                                                            .setId(taskId)
                                                                            .setPriorityChange(change)
                                                                            .build();
            client.update(updateTaskPriority);
        }
    }

    private void clearValues() {
        this.description = "";
        this.priority = TaskPriority.TP_UNDEFINED;
        this.dueDate = Timestamp.getDefaultInstance();
    }

    static class CreateTaskModeConstants {
        static final String EMPTY = "";
        static final String NEED_TO_FINALIZE_MESSAGE = "Do you want to finalize the created task draft?(y/n)";
        static final String DRAFT_FINALIZED_MESSAGE = "Task draft finalized.";
        static final String POSITIVE_ANSWER = "y";
        static final String CHANGED_PRIORITY_MESSAGE = "Set the task priority. Value: ";
        static final String CHANGED_DUE_DATE_MESSAGE = "Set the task due date. Value: ";
        static final String CHANGED_DESCRIPTION_MESSAGE = "Set the task description. Value: ";
        static final String SET_DESCRIPTION_MESSAGE = "Please enter the task description: ";
        static final String SET_DUE_DATE_MESSAGE = "Please enter the task due date: ";
        static final String SET_PRIORITY_MESSAGE = "Please enter the task priority";
        static final String HELP_MESSAGE = "0:    Help.\n" +
                "1:    Set the task description.\n" +
                "2:    Set the task due date.\n" +
                "3:    Set the task priority.\n" +
                "4:    Create the task with specified parameters[description is required].\n" +
                "5:    Create the task with specified parameters[description is required][FAST MODE].\n" +
                "exit: Exit from the mode.";
        static final String CREATED_DRAFT_MESSAGE = "Created task draft with parameters:" +
                "\nid: %s\ndescription: %s\npriority: %s\ndue date: %s";
        static final String CREATED_TASK_MESSAGE = "Created task with parameters:" +
                "\nid: %s\ndescription: %s\npriority: %s\ndue date: %s";
    }
}
