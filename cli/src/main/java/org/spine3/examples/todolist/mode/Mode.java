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

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import jline.console.ConsoleReader;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.validator.ApproveValidator;
import org.spine3.examples.todolist.validator.CommonValidator;
import org.spine3.examples.todolist.validator.DescriptionValidator;
import org.spine3.examples.todolist.validator.DueDateValidator;
import org.spine3.examples.todolist.validator.IdValidator;
import org.spine3.examples.todolist.validator.LabelColorValidator;
import org.spine3.examples.todolist.validator.TaskPriorityValidator;
import org.spine3.examples.todolist.validator.Validator;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.spine3.examples.todolist.mode.CommonMode.CommonModeConstants.ENTER_ID_MESSAGE;
import static org.spine3.examples.todolist.mode.GeneralMode.MainModeConstants.ENTER_LABEL_ID_MESSAGE;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.INCORRECT_INPUT;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.LABEL_COLOR_VALUE;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.TASK_PRIORITY_VALUE;

/**
 * @author Illia Shepilov
 */
abstract class Mode {

    private static final String CANCELED_INPUT = "canceled";
    private static final String INPUT_IS_CANCELED = "Input is canceled";
    private Validator priorityValidator;
    private Validator dueDateValidator;
    private Validator colorValidator;
    private Validator commonValidator;
    private Validator idValidator;
    private Validator descriptionValidator;
    private Validator approveValidator;
    private final Map<String, TaskPriority> priorityMap;
    private final Map<String, LabelColor> colorMap;
    final TodoClient client;
    final ConsoleReader reader;

    Mode(TodoClient client, ConsoleReader reader) {
        this.client = client;
        this.reader = reader;
        priorityMap = initPriorityMap();
        colorMap = initColorMap();
        initValidators();
    }

    abstract void start() throws IOException;

    LabelColor obtainLabelColor(String message) throws IOException, InputCancelledException {
        final String labelColorValue = obtainLabelColorValue(message);
        final LabelColor result = colorMap.get(labelColorValue);
        return result;
    }

    private String obtainLabelColorValue(String message) throws IOException, InputCancelledException {
        sendMessageToUser(message + LABEL_COLOR_VALUE);
        String color = reader.readLine();

        if (CANCELED_INPUT.equals(color)) {
            throw new InputCancelledException(INPUT_IS_CANCELED);
        }

        color = color == null ? null : color.toUpperCase();
        final boolean isValid = colorValidator.validate(color);

        if (!isValid) {
            sendMessageToUser(INCORRECT_INPUT);
            color = obtainLabelColorValue(message);
        }

        return color;
    }

    String obtainLabelTitle(String message) throws IOException, InputCancelledException {
        sendMessageToUser(message);
        String title = reader.readLine();

        if (CANCELED_INPUT.equals(title)) {
            throw new InputCancelledException(INPUT_IS_CANCELED);
        }

        boolean isValid = commonValidator.validate(title);

        if (!isValid) {
            sendMessageToUser(commonValidator.getMessage());
            title = obtainLabelTitle(message);
        }
        return title;
    }

    Timestamp constructPreviousDueDate(SimpleDateFormat simpleDateFormat, String previousDueDateValue)
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

    TaskPriority obtainTaskPriority(String message) throws IOException {
        final String priorityValue = obtainPriorityValue(message + TASK_PRIORITY_VALUE);
        final TaskPriority result = priorityMap.get(priorityValue);
        return result;
    }

    private String obtainPriorityValue(String message) throws IOException {
        sendMessageToUser(message);
        String priorityNumber = reader.readLine();
        priorityNumber = priorityNumber == null ? null : priorityNumber.toUpperCase();
        final boolean isValid = priorityValidator.validate(priorityNumber);

        if (!isValid) {
            sendMessageToUser(INCORRECT_INPUT);
            priorityNumber = obtainPriorityValue(message);
        }
        return priorityNumber;
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

    static TaskId createTaskId(String taskIdValue) {
        return TaskId.newBuilder()
                     .setValue(taskIdValue)
                     .build();
    }

    private void initValidators() {
        descriptionValidator = new DescriptionValidator();
        dueDateValidator = new DueDateValidator();
        idValidator = new IdValidator();
        priorityValidator = new TaskPriorityValidator(priorityMap);
        commonValidator = new CommonValidator();
        colorValidator = new LabelColorValidator(colorMap);
        approveValidator = new ApproveValidator();
    }

    private static Map<String, TaskPriority> initPriorityMap() {
        final Map<String, TaskPriority> priorityMap = newHashMap();
        priorityMap.put("0", TaskPriority.TP_UNDEFINED);
        priorityMap.put("1", TaskPriority.LOW);
        priorityMap.put("2", TaskPriority.NORMAL);
        priorityMap.put("3", TaskPriority.HIGH);
        return priorityMap;
    }

    private static Map<String, LabelColor> initColorMap() {
        final Map<String, LabelColor> colorMap = newHashMap();
        colorMap.put("0", LabelColor.LC_UNDEFINED);
        colorMap.put("1", LabelColor.GRAY);
        colorMap.put("2", LabelColor.RED);
        colorMap.put("3", LabelColor.GREEN);
        colorMap.put("4", LabelColor.BLUE);
        return colorMap;
    }

    void sendMessageToUser(String message) throws IOException {
        System.out.println(message);
    }

    static class ModeConstants {
        static final String LINE_SEPARATOR = System.lineSeparator();
        static final String INCORRECT_INPUT = "Incorrect input.";
        static final String TASK_PRIORITY_VALUE = LINE_SEPARATOR +
                "Valid task priority:" + LINE_SEPARATOR +
                "1: LOW;" + LINE_SEPARATOR +
                "2: NORMAL;" + LINE_SEPARATOR +
                "3: HIGH.";
        static final String LABEL_COLOR_VALUE = LINE_SEPARATOR +
                "Valid label colors:" + LINE_SEPARATOR +
                "1: GRAY;" + LINE_SEPARATOR +
                "2: RED;" + LINE_SEPARATOR +
                "3: GREEN;" + LINE_SEPARATOR +
                "4: BLUE.";
        static final String BACK_TO_THE_MENU_MESSAGE = "back: Back to the previous menu.";
        static final String BACK = "back";
        static final String POSITIVE_ANSWER = "y";
        static final String NEGATIVE_ANSWER = "n";
        static final String INCORRECT_COMMAND = "Incorrect command.";

        private ModeConstants() {
        }
    }
}
