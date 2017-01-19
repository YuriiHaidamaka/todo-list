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
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.validator.CommonValidator;
import org.spine3.examples.todolist.validator.LabelColorValidator;
import org.spine3.examples.todolist.validator.Validator;

import java.io.BufferedReader;
import java.io.IOException;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.modes.CreateLabelMode.CreateLabelModeConstants.CHANGED_COLOR_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateLabelMode.CreateLabelModeConstants.CHANGED_TITLE_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateLabelMode.CreateLabelModeConstants.HELP_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateLabelMode.CreateLabelModeConstants.SET_COLOR_MESSAGE;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
public class CreateLabelMode {

    private String title;
    private LabelColor color = LabelColor.LC_UNDEFINED;
    private Validator commonValidator;
    private Validator colorValidator;
    private final TodoClient client;
    private final BufferedReader reader;

    CreateLabelMode(TodoClient client, BufferedReader reader) {
        this.client = client;
        this.reader = reader;
        initValidators();
    }

    @Command(abbrev = "0")
    public void help() {
        sendMessageToUser(HELP_MESSAGE);
    }

    @Command(abbrev = "1")
    public void setTitle() throws IOException {
        final String title = obtainLabelTitle();
        this.title = title;
        final String message = CHANGED_TITLE_MESSAGE + title;
        sendMessageToUser(message);
    }

    @Command(abbrev = "2")
    public void setColor() throws IOException {
        final String colorValue = obtainLabelColor();
        final LabelColor color = LabelColor.valueOf(colorValue);
        this.color = color;
        final String message = CHANGED_COLOR_MESSAGE + color;
        sendMessageToUser(message);
    }

    @Command(abbrev = "3")
    public void createLabel() throws IOException {
        final TaskLabelId labelId = TaskLabelId.newBuilder()
                                               .setValue(newUuid())
                                               .build();
        final Validator commonValidator = new CommonValidator();
        final boolean isValidTitle = commonValidator.validate(title);
        if (!isValidTitle) {
            setTitle();
        }

        final CreateBasicLabel createBasicLabel = CreateBasicLabel.newBuilder()
                                                                  .setLabelTitle(title)
                                                                  .setLabelId(labelId)
                                                                  .build();
        client.create(createBasicLabel);

        updateLabelDetailsIfNeeded(labelId);
        final String message = String.format("Created label with id: %s, title: %s, color: %s", labelId, title, color);
        sendMessageToUser(message);
        clearValues();
    }

    private void updateLabelDetailsIfNeeded(TaskLabelId labelId) {
        if (color != LabelColor.LC_UNDEFINED) {
            final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                             .setColor(color)
                                                             .setTitle(title)
                                                             .build();
            final LabelDetailsChange labelDetailsChange = LabelDetailsChange.newBuilder()
                                                                            .setNewDetails(newLabelDetails)
                                                                            .build();
            final UpdateLabelDetails updateLabelDetails = UpdateLabelDetails.newBuilder()
                                                                            .setLabelDetailsChange(labelDetailsChange)
                                                                            .setId(labelId)
                                                                            .build();
            client.update(updateLabelDetails);
        }
    }

    private String obtainLabelTitle() throws IOException {
        sendMessageToUser(CHANGED_TITLE_MESSAGE);
        String title = reader.readLine();
        final boolean isValid = commonValidator.validate(title);

        if (!isValid) {
            sendMessageToUser(commonValidator.getMessage());
            title = obtainLabelTitle();
        }
        return title;
    }

    private String obtainLabelColor() throws IOException {
        sendMessageToUser(SET_COLOR_MESSAGE);
        String input = reader.readLine();

        final boolean isValid = colorValidator.validate(input);

        if (!isValid) {
            sendMessageToUser(colorValidator.getMessage());
            input = obtainLabelColor();
        }
        return input;
    }

    private void clearValues() {
        color = LabelColor.LC_UNDEFINED;
        title = "";
    }

    private void initValidators() {
        commonValidator = new CommonValidator();
        colorValidator = new LabelColorValidator();
    }

    static class CreateLabelModeConstants {
        static final String CHANGED_COLOR_MESSAGE = "Set the label color. Value: ";
        static final String CHANGED_TITLE_MESSAGE = "Set the label title. Value: ";
        static final String SET_COLOR_MESSAGE = "Please enter the label color: ";
        static final String SET_TITLE_MESSAGE = "Please enter the label title: ";
        static final String HELP_MESSAGE = "0:    Help.\n" +
                "1:    Set the label title.\n" +
                "2:    Set the label color.\n" +
                "3:    Create basic label [title is required].\n" +
                "exit: Exit from the mode.";
    }
}
