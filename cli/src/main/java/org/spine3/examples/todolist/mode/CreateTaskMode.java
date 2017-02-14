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

package org.spine3.examples.todolist.mode;

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
import static org.spine3.examples.todolist.DateHelper.DATE_FORMAT;
import static org.spine3.examples.todolist.DateHelper.getDateFormat;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.BACK_TO_THE_PREVIOUS_MENU_QUESTION;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.CREATED_DRAFT_MESSAGE;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.CREATED_TASK_MESSAGE;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.CREATE_ONE_MORE_TASK_QUESTION;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.CREATE_TASK_PROMPT;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.CREATE_TASK_TITLE;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.DRAFT_FINALIZED_MESSAGE;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.EMPTY;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.HELP_MESSAGE;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.NEED_TO_FINALIZE_MESSAGE;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.SET_DESCRIPTION_MESSAGE;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.SET_DUE_DATE_MESSAGE;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.SET_DUE_DATE_QUESTION;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.SET_PRIORITY_MESSAGE;
import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.SET_PRIORITY_QUESTION;
import static org.spine3.examples.todolist.mode.GeneralMode.MainModeConstants.HELP_ADVICE;
import static org.spine3.examples.todolist.mode.GeneralMode.MainModeConstants.TODO_PROMPT;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.BACK;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.INCORRECT_COMMAND;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.LINE_SEPARATOR;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.POSITIVE_ANSWER;
import static org.spine3.examples.todolist.mode.ModeHelper.constructUserFriendlyDate;

/**
 * @author Illia Shepilov
 */
class CreateTaskMode extends Mode {

    private static final String NEGATIVE_ANSWER = "n";
    private Timestamp dueDate = Timestamp.getDefaultInstance();
    private TaskPriority priority = TaskPriority.TP_UNDEFINED;
    private String description;
    private final Map<String, Mode> map = Maps.newHashMap();

    CreateTaskMode(TodoClient client, ConsoleReader reader) {
        super(client, reader);
        initModeMap();
    }

    private void initModeMap() {
        map.put("0", new HelpMode(client, reader, HELP_MESSAGE));
        map.put("1", new CreateTaskDM(client, reader));
        map.put("2", new CreateTaskFM(client, reader));
    }

    @Override
    void start() throws IOException {
        sendMessageToUser(CREATE_TASK_TITLE);
        reader.setPrompt(CREATE_TASK_PROMPT);
        String line = "";

        while (!line.equals(BACK)) {
            line = reader.readLine();
            final Mode mode = map.get(line);

            if (mode == null) {
                sendMessageToUser(INCORRECT_COMMAND);
                continue;
            }

            mode.start();
            final String approve = obtainApproveValue(BACK_TO_THE_PREVIOUS_MENU_QUESTION);
            if (approve.equals(NEGATIVE_ANSWER)) {
                sendMessageToUser(HELP_MESSAGE);
            }

            if (approve.equals(POSITIVE_ANSWER)) {
                line = BACK;
            }
        }

        reader.setPrompt(TODO_PROMPT);
    }

    private void updateDueDateIfNeeded(TaskId taskId) throws IOException, ParseException {
        final String approveValue = obtainApproveValue(SET_DUE_DATE_QUESTION);
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
        final String approveValue = obtainApproveValue(SET_PRIORITY_QUESTION);
        if (approveValue.equals(NEGATIVE_ANSWER)) {
            return;
        }

        final TaskPriority priority = obtainTaskPriority(SET_PRIORITY_MESSAGE);
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

    private void updateTaskValuesIfNeeded(TaskId taskId) throws IOException {
        try {
            updatePriorityIfNeeded(taskId);
            updateDueDateIfNeeded(taskId);
        } catch (ParseException e) {
            throw new ParseDateException(e);
        }
    }

    private void clearValues() {
        this.description = "";
        this.priority = TaskPriority.TP_UNDEFINED;
        this.dueDate = Timestamp.getDefaultInstance();
    }

    class CreateTaskFM extends Mode {

        private CreateTaskFM(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            String line = "";
            while (!line.equals(BACK)) {
                createTask();
                final String approveValue = obtainApproveValue(CREATE_ONE_MORE_TASK_QUESTION);
                if (approveValue.equals(NEGATIVE_ANSWER)) {
                    return;
                }
                line = reader.readLine();
            }
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

        private void createTask(TaskId taskId) throws IOException {
            final String description = obtainDescriptionValue(SET_DESCRIPTION_MESSAGE, true);

            final CreateBasicTask createTask = CreateBasicTask.newBuilder()
                                                              .setId(taskId)
                                                              .setDescription(description)
                                                              .build();
            client.create(createTask);
            CreateTaskMode.this.description = description;
        }
    }

    class CreateTaskDM extends Mode {

        private CreateTaskDM(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            String line = "";
            while (!line.equals(BACK)) {
                createTaskDraft();
                final String approveValue = obtainApproveValue(CREATE_ONE_MORE_TASK_QUESTION);
                if (approveValue.equals(NEGATIVE_ANSWER)) {
                    return;
                }
                line = reader.readLine();
            }
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
            CreateTaskMode.this.description = description;
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
    }

    static class CreateTaskModeConstants {
        static final String SET_PRIORITY_QUESTION = "Do you want to set the task priority?(y/n)";
        static final String SET_DUE_DATE_QUESTION = "Do you want to set the task due date?(y/n)";
        static final String CREATE_ONE_MORE_TASK_QUESTION = "Do you want to create one more task?(y/n)";
        static final String BACK_TO_THE_PREVIOUS_MENU_QUESTION = "Do you want go back to the main menu?(y/n)";
        static final String CREATE_TASK_PROMPT = "create-task>";
        private static final String CREATE_TASK_MODE = "******************** Create task menu ********************" +
                LINE_SEPARATOR;
        static final String EMPTY = "";
        static final String NEED_TO_FINALIZE_MESSAGE = "Do you want to finalize the created task draft?(y/n)";
        static final String DRAFT_FINALIZED_MESSAGE = "Task draft finalized.";
        static final String SET_DESCRIPTION_MESSAGE = "Please enter the task description " +
                "(should contain at least 3 symbols): ";
        static final String SET_DUE_DATE_MESSAGE = "Please enter the task due date." + LINE_SEPARATOR +
                "The correct format is: " + DATE_FORMAT;
        static final String SET_PRIORITY_MESSAGE = "Please enter the task priority.";
        static final String HELP_MESSAGE = "0:    Help." + LINE_SEPARATOR +
                "1:    Create the task with specified parameters[description is required]." + LINE_SEPARATOR +
                "2:    Create the task with specified parameters[description is required][FAST MODE]." +
                LINE_SEPARATOR + BACK_TO_THE_MENU_MESSAGE;
        static final String TASK_PARAMS_DESCRIPTION = "id: %s" + LINE_SEPARATOR +
                "description: %s" + LINE_SEPARATOR +
                "priority: %s" + LINE_SEPARATOR +
                "due date: %s";
        static final String CREATED_DRAFT_MESSAGE = "Created task draft with parameters:" + LINE_SEPARATOR +
                TASK_PARAMS_DESCRIPTION;
        static final String CREATED_TASK_MESSAGE = "Created task with parameters:" + LINE_SEPARATOR +
                TASK_PARAMS_DESCRIPTION;
        static final String CREATE_TASK_TITLE = CREATE_TASK_MODE + HELP_ADVICE + HELP_MESSAGE;

        private CreateTaskModeConstants() {
        }
    }
}
