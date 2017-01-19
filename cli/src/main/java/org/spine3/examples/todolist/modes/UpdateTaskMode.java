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
import org.spine3.examples.todolist.validator.DescriptionValidator;
import org.spine3.examples.todolist.validator.DueDateValidator;
import org.spine3.examples.todolist.validator.IdValidator;
import org.spine3.examples.todolist.validator.TaskPriorityValidator;
import org.spine3.examples.todolist.validator.Validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.spine3.examples.todolist.CommonHelper.getDateFormat;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.DEFAULT_VALUE;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.EMPTY;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.ENTER_ID_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.ENTER_NEW_DATE_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.ENTER_NEW_DESCRIPTION_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.ENTER_NEW_PRIORITY_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.ENTER_PREVIOUS_DATE_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.ENTER_PREVIOUS_DESCRIPTION_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.ENTER_PREVIOUS_PRIORITY_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.HELP_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.UPDATED_DESCRIPTION_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.UPDATED_DUE_DATE_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateTaskMode.UpdateTaskModeConstants.UPDATED_PRIORITY_MESSAGE;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
public class UpdateTaskMode {

    private final TodoClient client;
    private final BufferedReader reader;
    private Validator idValidator;
    private Validator descriptionValidator;
    private Validator priorityValidator;
    private Validator dueDateValidator;

    UpdateTaskMode(TodoClient client, BufferedReader reader) {
        this.client = client;
        this.reader = reader;
        initValidators();
    }

    @Command(abbrev = "0")
    public void help() {
        System.out.println(HELP_MESSAGE);
    }

    @Command(abbrev = "1")
    public void updateTaskDescription() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();

        final String newDescription = obtainDescriptionValue(ENTER_NEW_DESCRIPTION_MESSAGE, true);
        final String previousDescription = obtainDescriptionValue(ENTER_PREVIOUS_DESCRIPTION_MESSAGE, false);
        final StringChange change = StringChange.newBuilder()
                                                .setNewValue(newDescription)
                                                .setPreviousValue(previousDescription)
                                                .build();
        final UpdateTaskDescription updateTaskDescription = UpdateTaskDescription.newBuilder()
                                                                                 .setDescriptionChange(change)
                                                                                 .setId(taskId)
                                                                                 .build();
        client.update(updateTaskDescription);
        final String userFriendlyPrevDescr = previousDescription.isEmpty() ? DEFAULT_VALUE : previousDescription;
        final String message = String.format(UPDATED_DESCRIPTION_MESSAGE, userFriendlyPrevDescr, newDescription);
        sendMessageToUser(message);
    }

    @Command(abbrev = "2")
    public void updateTaskPriority() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final String priorityValue = obtainPriorityValue(ENTER_NEW_PRIORITY_MESSAGE);
        final TaskPriority newTaskPriority = TaskPriority.valueOf(priorityValue);
        final String previousPriorityValue = obtainPriorityValue(ENTER_PREVIOUS_PRIORITY_MESSAGE);
        final TaskPriority previousTaskPriority = TaskPriority.valueOf(previousPriorityValue);
        final PriorityChange change = PriorityChange.newBuilder()
                                                    .setPreviousValue(previousTaskPriority)
                                                    .setNewValue(newTaskPriority)
                                                    .build();
        final UpdateTaskPriority updateTaskPriority = UpdateTaskPriority.newBuilder()
                                                                        .setPriorityChange(change)
                                                                        .setId(taskId)
                                                                        .build();
        client.update(updateTaskPriority);
        final String message = String.format(UPDATED_PRIORITY_MESSAGE, previousPriorityValue, newTaskPriority);
        sendMessageToUser(message);
    }

    @Command(abbrev = "3")
    public void updateTaskDueDate() throws IOException, ParseException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final SimpleDateFormat simpleDateFormat = getDateFormat();
        final String newDueDateValue = obtainDueDateValue(ENTER_NEW_DATE_MESSAGE, true);
        final long newDueDateInMS = simpleDateFormat.parse(newDueDateValue)
                                                    .getTime();
        final Timestamp newDueDate = Timestamps.fromMillis(newDueDateInMS);
        final String previousDueDateValue = obtainDueDateValue(ENTER_PREVIOUS_DATE_MESSAGE, false);
        Timestamp previousDueDate = constructPreviousPriority(simpleDateFormat, previousDueDateValue);
        final TimestampChange change = TimestampChange.newBuilder()
                                                      .setPreviousValue(previousDueDate)
                                                      .setNewValue(newDueDate)
                                                      .build();
        final UpdateTaskDueDate updateTaskDueDate = UpdateTaskDueDate.newBuilder()
                                                                     .setDueDateChange(change)
                                                                     .setId(taskId)
                                                                     .build();
        client.update(updateTaskDueDate);
        final boolean isEmpty = previousDueDateValue.equals(EMPTY);
        final String previousDueDateForUser = isEmpty ? DEFAULT_VALUE : previousDueDateValue;
        final String message = String.format(UPDATED_DUE_DATE_MESSAGE, previousDueDateForUser, newDueDateValue);
        sendMessageToUser(message);
    }

    private Timestamp constructPreviousPriority(SimpleDateFormat simpleDateFormat, String previousDueDateValue)
            throws ParseException {
        Timestamp previousDueDate = Timestamp.getDefaultInstance();
        if (!previousDueDateValue.isEmpty()) {
            final long previousDueDateInMS = simpleDateFormat.parse(previousDueDateValue)
                                                             .getTime();
            previousDueDate = Timestamps.fromMillis(previousDueDateInMS);
        }
        return previousDueDate;
    }

    private String obtainTaskIdValue() throws IOException {
        sendMessageToUser(ENTER_ID_MESSAGE);
        String taskIdValue = reader.readLine();
        final boolean isValid = idValidator.validate(taskIdValue);
        if (!isValid) {
            sendMessageToUser(idValidator.getMessage());
            taskIdValue = obtainTaskIdValue();
        }
        return taskIdValue;
    }

    private String obtainDescriptionValue(String message, boolean isNew) throws IOException {
        sendMessageToUser(message);
        String description = reader.readLine();

        if (description.isEmpty() && !isNew) {
            return description;
        }

        final boolean isValid = descriptionValidator.validate(description);

        if (!isValid) {
            sendMessageToUser(descriptionValidator.getMessage());
            description = obtainDescriptionValue(message, isNew);
        }
        return description;
    }

    private String obtainPriorityValue(String message) throws IOException {
        sendMessageToUser(message);
        String priority = reader.readLine();
        priority = priority == null ? null : priority.toUpperCase();
        final boolean isValid = priorityValidator.validate(priority);

        if (!isValid) {
            sendMessageToUser(priorityValidator.getMessage());
            priority = obtainPriorityValue(message);
        }
        return priority;
    }

    private String obtainDueDateValue(String message, boolean isNew) throws IOException, ParseException {
        sendMessageToUser(message);
        String dueDate = reader.readLine();

        if (dueDate.isEmpty() && !isNew) {
            return dueDate;
        }

        final boolean isValid = dueDateValidator.validate(dueDate);

        if (!isValid) {
            sendMessageToUser(dueDateValidator.getMessage());
            dueDate = obtainDueDateValue(message, isNew);
        }
        return dueDate;
    }

    private void initValidators() {
        descriptionValidator = new DescriptionValidator();
        dueDateValidator = new DueDateValidator();
        idValidator = new IdValidator();
        priorityValidator = new TaskPriorityValidator();
    }

    static class UpdateTaskModeConstants {
        static final String EMPTY = "";
        final static String DEFAULT_VALUE = "default";
        static final String UPDATED_DESCRIPTION_MESSAGE = "The task description updated. %s --> %s";
        static final String UPDATED_PRIORITY_MESSAGE = "The task priority updated. %s --> %s";
        static final String UPDATED_DUE_DATE_MESSAGE = "The task due date updated. %s --> %s";
        static final String ENTER_NEW_DESCRIPTION_MESSAGE = "Please enter the new task description: ";
        static final String ENTER_PREVIOUS_DESCRIPTION_MESSAGE = "Please enter the previous task description: ";
        static final String ENTER_ID_MESSAGE = "Please enter the task id: ";
        static final String ENTER_NEW_PRIORITY_MESSAGE = "Please enter the new task priority: ";
        static final String ENTER_PREVIOUS_PRIORITY_MESSAGE = "Please enter the previous task priority: ";
        static final String ENTER_NEW_DATE_MESSAGE = "Please enter the new task due date: ";
        static final String ENTER_PREVIOUS_DATE_MESSAGE = "Please enter the previous task due date: ";
        static final String HELP_MESSAGE = "0:    Help.\n" +
                "1:    Update the task description.\n" +
                "2:    Update the task priority.\n" +
                "3:    Update the task due date.\n" +
                "exit: Exit from the mode.";
    }
}
