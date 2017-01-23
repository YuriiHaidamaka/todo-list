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

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import jline.console.ConsoleReader;
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.validators.ApproveValidator;
import org.spine3.examples.todolist.validators.CommonValidator;
import org.spine3.examples.todolist.validators.DescriptionValidator;
import org.spine3.examples.todolist.validators.DueDateValidator;
import org.spine3.examples.todolist.validators.IdValidator;
import org.spine3.examples.todolist.validators.LabelColorValidator;
import org.spine3.examples.todolist.validators.TaskPriorityValidator;
import org.spine3.examples.todolist.validators.Validator;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.ENTER_ID_MESSAGE;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.ENTER_LABEL_ID_MESSAGE;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;

/**
 * @author Illia Shepilov
 */
abstract class Mode {

    private Validator priorityValidator;
    private Validator dueDateValidator;
    private Validator colorValidator;
    private Validator commonValidator;
    Validator idValidator;
    Validator descriptionValidator;
    Validator approveValidator;
    final TodoClient client;
    final ConsoleReader reader;

    Mode(TodoClient client, ConsoleReader reader) {
        this.client = client;
        this.reader = reader;
        initValidators();
    }

    abstract void start() throws IOException;

    String obtainLabelColorValue(String message) throws IOException {
        sendMessageToUser(message);
        String color = reader.readLine();
        color = color == null ? null : color.toUpperCase();
        final boolean isValid = colorValidator.validate(color);

        if (!isValid) {
            sendMessageToUser(colorValidator.getMessage());
            color = obtainLabelColorValue(message);
        }
        return color;
    }

    String obtainLabelTitle(String message) throws IOException {
        sendMessageToUser(message);
        String title = reader.readLine();
        boolean isValid = commonValidator.validate(title);

        if (!isValid) {
            sendMessageToUser(commonValidator.getMessage());
            title = obtainLabelTitle(message);
        }
        return title;
    }

    Timestamp constructPreviousPriority(SimpleDateFormat simpleDateFormat, String previousDueDateValue)
            throws ParseException {
        Timestamp previousDueDate = Timestamp.getDefaultInstance();
        if (!previousDueDateValue.isEmpty()) {
            final long previousDueDateInMS = simpleDateFormat.parse(previousDueDateValue)
                                                             .getTime();
            previousDueDate = Timestamps.fromMillis(previousDueDateInMS);
        }
        return previousDueDate;
    }

    String obtainDescriptionValue(String message, boolean isNew) throws IOException {
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

    String obtainPriorityValue(String message) throws IOException {
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

    String obtainDueDateValue(String message, boolean isNew) throws IOException, ParseException {
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

    String obtainLabelIdValue() throws IOException {
        sendMessageToUser(ENTER_LABEL_ID_MESSAGE);
        String labelIdInput = reader.readLine();
        final boolean isValid = idValidator.validate(labelIdInput);

        if (!isValid) {
            sendMessageToUser(idValidator.getMessage());
            labelIdInput = obtainLabelIdValue();
        }
        return labelIdInput;
    }

    String obtainTaskIdValue() throws IOException {
        sendMessageToUser(ENTER_ID_MESSAGE);
        String taskIdValue = reader.readLine();
        final boolean isValid = idValidator.validate(taskIdValue);
        if (!isValid) {
            sendMessageToUser(idValidator.getMessage());
            taskIdValue = obtainTaskIdValue();
        }
        return taskIdValue;
    }

    String obtainApproveValue(String message) throws IOException {
        sendMessageToUser(message);
        String approveValue = reader.readLine();
        final boolean isValid = approveValidator.validate(approveValue);
        if (!isValid) {
            sendMessageToUser(approveValidator.getMessage());
            approveValue = obtainApproveValue(message);
        }
        return approveValue;
    }

    private void initValidators() {
        descriptionValidator = new DescriptionValidator();
        dueDateValidator = new DueDateValidator();
        idValidator = new IdValidator();
        priorityValidator = new TaskPriorityValidator();
        commonValidator = new CommonValidator();
        colorValidator = new LabelColorValidator();
        approveValidator = new ApproveValidator();
    }
}
