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
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsChange;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.validator.CommonValidator;
import org.spine3.examples.todolist.validator.IdValidator;
import org.spine3.examples.todolist.validator.LabelColorValidator;
import org.spine3.examples.todolist.validator.Validator;

import java.io.BufferedReader;
import java.io.IOException;

import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.ENTER_LABEL_ID_MESSAGE;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;
import static org.spine3.examples.todolist.modes.UpdateLabelMode.UpdateLabelModeConstants.ENTER_NEW_COLOR_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateLabelMode.UpdateLabelModeConstants.ENTER_NEW_TITLE_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateLabelMode.UpdateLabelModeConstants.ENTER_PREVIOUS_COLOR_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateLabelMode.UpdateLabelModeConstants.ENTER_PREVIOUS_TITLE_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateLabelMode.UpdateLabelModeConstants.HELP_MESSAGE;
import static org.spine3.examples.todolist.modes.UpdateLabelMode.UpdateLabelModeConstants.UPDATED_LABLE_DETAILS_MESSAGE;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
public class UpdateLabelMode {

    private Validator idValidator;
    private Validator colorValidator;
    private Validator commonValidator;
    private final TodoClient client;
    private final BufferedReader reader;

    UpdateLabelMode(TodoClient client, BufferedReader reader) {
        this.client = client;
        this.reader = reader;
        initValidators();
    }

    @Command(abbrev = "0")
    public void help() {
        System.out.println(HELP_MESSAGE);
    }

    @Command(abbrev = "1")
    public void updateLabelDetails() throws IOException {
        final String labelIdValue = obtainLabelIdValue();
        final TaskLabelId labelId = TaskLabelId.newBuilder()
                                               .setValue(labelIdValue)
                                               .build();
        final String newTitle = obtainLabelTitle(ENTER_NEW_TITLE_MESSAGE);
        final String previousTitle = obtainLabelTitle(ENTER_PREVIOUS_TITLE_MESSAGE);
        final String labelColorValue = obtainLabelColorValue(ENTER_NEW_COLOR_MESSAGE);
        final LabelColor newColor = LabelColor.valueOf(labelColorValue);
        final String previousColorValue = obtainLabelColorValue(ENTER_PREVIOUS_COLOR_MESSAGE);
        final LabelColor previousColor = LabelColor.valueOf(previousColorValue);

        final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                         .setTitle(newTitle)
                                                         .setColor(newColor)
                                                         .build();
        final LabelDetails previousLabelDetails = LabelDetails.newBuilder()
                                                              .setTitle(previousTitle)
                                                              .setColor(previousColor)
                                                              .build();
        final LabelDetailsChange change = LabelDetailsChange.newBuilder()
                                                            .setNewDetails(newLabelDetails)
                                                            .setPreviousDetails(previousLabelDetails)
                                                            .build();
        final UpdateLabelDetails updateLabelDetails = UpdateLabelDetails.newBuilder()
                                                                        .setId(labelId)
                                                                        .setLabelDetailsChange(change)
                                                                        .setId(labelId)
                                                                        .build();
        client.update(updateLabelDetails);
        final String message = String.format(UPDATED_LABLE_DETAILS_MESSAGE,
                                             previousColor, newColor, previousTitle, newTitle);
        sendMessageToUser(message);
    }

    private String obtainLabelColorValue(String message) throws IOException {
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

    private String obtainLabelTitle(String message) throws IOException {
        sendMessageToUser(message);
        String title = reader.readLine();
        boolean isValid = commonValidator.validate(title);

        if (!isValid) {
            sendMessageToUser(commonValidator.getMessage());
            title = obtainLabelTitle(message);
        }
        return title;
    }

    private String obtainLabelIdValue() throws IOException {
        sendMessageToUser(ENTER_LABEL_ID_MESSAGE);
        String labelIdValue = reader.readLine();
        boolean isValid = idValidator.validate(labelIdValue);

        if (!isValid) {
            sendMessageToUser(idValidator.getMessage());
            labelIdValue = obtainLabelIdValue();
        }
        return labelIdValue;
    }

    private void initValidators() {
        idValidator = new IdValidator();
        colorValidator = new LabelColorValidator();
        commonValidator = new CommonValidator();
    }

    static class UpdateLabelModeConstants {
        static final String UPDATED_LABLE_DETAILS_MESSAGE = "The label details updated.\n" +
                "The label color: %s --> %s.\nThe label title: %s --> %s";
        static final String ENTER_NEW_TITLE_MESSAGE = "Please enter the new label title: ";
        static final String ENTER_PREVIOUS_TITLE_MESSAGE = "Please enter the previous label title: ";
        static final String ENTER_NEW_COLOR_MESSAGE = "Please enter the new label color: ";
        static final String ENTER_PREVIOUS_COLOR_MESSAGE = "Please enter the previous label color: ";
        static final String HELP_MESSAGE = "0:    Help.\n" +
                "1:    Update the label details.\n" +
                "exit: Exit from the mode.";
    }
}
