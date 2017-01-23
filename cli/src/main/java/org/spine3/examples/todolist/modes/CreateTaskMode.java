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
import com.google.common.collect.Maps;
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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.DateHelper.getDateFormat;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.CREATED_DRAFT_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.CREATED_TASK_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.CREATE_TASK_PROMPT;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.CREATE_TASK_TITLE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.DRAFT_FINALIZED_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.EMPTY;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.HELP_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.NEED_TO_FINALIZE_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.SET_DESCRIPTION_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.SET_DUE_DATE_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateTaskMode.CreateTaskModeConstants.SET_PRIORITY_MESSAGE;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.HELP_ADVICE;
import static org.spine3.examples.todolist.modes.ModeHelper.constructUserFriendlyDate;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;

/**
 * @author Illia Shepilov
 */
public class CreateTaskMode extends Mode {

    private static final String SET_PRIORITY_APPROVE_MESSAGE = "Do you want to set the task priority?(y/n)";
    private static final String SET_DUE_DATE_APPROVE_MESSAGE = "Do you want to set the task due date?(y/n)";
    private static final String CREATE_TASK_APPROVE_MESSAGE = "Do you want to create one more task?(y/n)";
    private static final String NEGATIVE_ANSWER = "n";
    private Timestamp dueDate = Timestamp.getDefaultInstance();
    private TaskPriority priority = TaskPriority.TP_UNDEFINED;
    private String description;
    private Map<String, Mode> map = Maps.newHashMap();

    CreateTaskMode(TodoClient client, ConsoleReader reader) {
        super(client, reader);
        map.put("0", new HelpMode(client, reader, HELP_MESSAGE));
        map.put("1", new CreateDraftDM(client, reader));
        map.put("2", new CreateTaskFM(client, reader));
    }

    @Override
    void start() throws IOException {
        sendMessageToUser(CREATE_TASK_TITLE);
        reader.setPrompt(CREATE_TASK_PROMPT);
        String line = "";
        while ((line = reader.readLine()) != null) {
            if (line.equals("back")) {
                reader.setPrompt("todo>");
                return;
            }
            final Mode mode = map.get(line);
            if (mode != null) {
                mode.start();
                reader.setPrompt("todo>");
            }
            sendMessageToUser("Incorrect command.");
        }
    }

    @Command(abbrev = "0")
    public void help() {
        sendMessageToUser(HELP_MESSAGE);
    }

    private void createTaskDraft() throws IOException {
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        createTaskDraft(taskId);
        updateTaskValuesIfNeeded(taskId);

        final String userFriendlyDate = constructUserFriendlyDate(Timestamps.toMillis(dueDate));
        final String idValue = taskId.getValue();
        final String result = String.format(CREATED_DRAFT_MESSAGE, idValue, description, priority, userFriendlyDate);
        sendMessageToUser(result);

        finalizeDraftIfNeeded(taskId);
        clearValues();
    }

    private void createTask() throws IOException {
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        createTask(taskId);
        updateTaskValuesIfNeeded(taskId);

        final String userFriendlyDate = constructUserFriendlyDate(Timestamps.toMillis(dueDate));
        final String idValue = taskId.getValue();
        final String result = String.format(CREATED_TASK_MESSAGE, idValue, description, priority, userFriendlyDate);
        sendMessageToUser(result);

        clearValues();
    }

    private void updateTaskValuesIfNeeded(TaskId taskId) throws IOException {
        updatePriorityIfNeeded(taskId);
        //TODO
        try {
            updateDueDateIfNeeded(taskId);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void createTaskDraft(TaskId taskId) throws IOException {
        final String description = obtainDescriptionValue(SET_DESCRIPTION_MESSAGE, true);

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
        this.description = description;
    }

    private void createTask(TaskId taskId) throws IOException {
        final String description = obtainDescriptionValue(SET_DESCRIPTION_MESSAGE, true);

        final CreateBasicTask createTask = CreateBasicTask.newBuilder()
                                                          .setId(taskId)
                                                          .setDescription(description)
                                                          .build();
        client.create(createTask);
        this.description = description;
    }

    private void finalizeDraftIfNeeded(TaskId taskId) throws IOException {
        final String approveValue = obtainApproveValue(NEED_TO_FINALIZE_MESSAGE);
        if (approveValue.equals(NEGATIVE_ANSWER)) {
            return;
        }
        final FinalizeDraft finalizeDraft = FinalizeDraft.newBuilder()
                                                         .setId(taskId)
                                                         .build();
        client.finalize(finalizeDraft);
        sendMessageToUser(DRAFT_FINALIZED_MESSAGE);
    }

    private void updateDueDateIfNeeded(TaskId taskId) throws IOException, ParseException {
        final String approveValue = obtainApproveValue(SET_DUE_DATE_APPROVE_MESSAGE);
        if (approveValue.equals(NEGATIVE_ANSWER)) {
            return;
        }

        final String dueDateValue = obtainDueDateValue(SET_DUE_DATE_MESSAGE, true);
        final SimpleDateFormat simpleDateFormat = getDateFormat();
        final long newDueDateInMS = simpleDateFormat.parse(dueDateValue)
                                                    .getTime();
        final Timestamp dueDate = Timestamps.fromMillis(newDueDateInMS);

        final TimestampChange change = TimestampChange.newBuilder()
                                                      .setNewValue(dueDate)
                                                      .build();
        final UpdateTaskDueDate updateTaskDueDate = UpdateTaskDueDate.newBuilder()
                                                                     .setId(taskId)
                                                                     .setDueDateChange(change)
                                                                     .build();
        client.update(updateTaskDueDate);
        this.dueDate = dueDate;
    }

    private void updatePriorityIfNeeded(TaskId taskId) throws IOException {
        final String approveValue = obtainApproveValue(SET_PRIORITY_APPROVE_MESSAGE);
        if (approveValue.equals(NEGATIVE_ANSWER)) {
            return;
        }

        final String priorityValue = obtainPriorityValue(SET_PRIORITY_MESSAGE);
        final TaskPriority priority = TaskPriority.valueOf(priorityValue);

        final PriorityChange change = PriorityChange.newBuilder()
                                                    .setNewValue(priority)
                                                    .build();
        final UpdateTaskPriority updateTaskPriority = UpdateTaskPriority.newBuilder()
                                                                        .setId(taskId)
                                                                        .setPriorityChange(change)
                                                                        .build();
        client.update(updateTaskPriority);
        this.priority = priority;
    }

    private void clearValues() {
        this.description = "";
        this.priority = TaskPriority.TP_UNDEFINED;
        this.dueDate = Timestamp.getDefaultInstance();
    }

    class CreateTaskFM extends Mode {

        CreateTaskFM(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            sendMessageToUser(CREATE_TASK_TITLE);
            reader.setPrompt(CREATE_TASK_PROMPT);
            String line = "";
            while (line != null) {
                if (line.equals("back")) {
                    return;
                }
                createTask();
                final String approveValue = obtainApproveValue(CREATE_TASK_APPROVE_MESSAGE);
                if (approveValue.equals(NEGATIVE_ANSWER)) {
                    return;
                }
                line = reader.readLine();
            }
        }
    }

    class CreateDraftDM extends Mode {

        CreateDraftDM(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            sendMessageToUser(CREATE_TASK_TITLE);
            reader.setPrompt(CREATE_TASK_PROMPT);
            String line = "";
            while (line != null) {
                if (line.equals("back")) {
                    return;
                }
                createTaskDraft();
                final String approveValue = obtainApproveValue(CREATE_TASK_APPROVE_MESSAGE);
                if (approveValue.equals(NEGATIVE_ANSWER)) {
                    return;
                }
                line = reader.readLine();
            }
        }
    }

    static class CreateTaskModeConstants {
        static final String CREATE_TASK_PROMPT = "create-task>";
        static final String CREATE_TASK_MODE = "******************** Create task menu ********************\n";
        static final String CREATE_TASK_TITLE = CREATE_TASK_MODE + HELP_ADVICE +
                CreateTaskMode.CreateTaskModeConstants.HELP_MESSAGE;
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
                "1:    Create the task with specified parameters[description is required].\n" +
                "2:    Create the task with specified parameters[description is required][FAST MODE].\n" +
                "back: Back to the previous menu.";
        static final String CREATED_DRAFT_MESSAGE = "Created task draft with parameters:" +
                "\nid: %s\ndescription: %s\npriority: %s\ndue date: %s";
        static final String CREATED_TASK_MESSAGE = "Created task with parameters:" +
                "\nid: %s\ndescription: %s\npriority: %s\ndue date: %s";
    }
}
