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

import jline.console.ConsoleReader;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsChange;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.client.TodoClient;

import java.io.IOException;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.modes.CreateLabelMode.CreateLabelModeConstants.CREATE_ONE_MORE_LABEL_QUESTION;
import static org.spine3.examples.todolist.modes.CreateLabelMode.CreateLabelModeConstants.LABEL_CREATED_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateLabelMode.CreateLabelModeConstants.SET_COLOR_MESSAGE;
import static org.spine3.examples.todolist.modes.CreateLabelMode.CreateLabelModeConstants.SET_LABEL_COLOR_QUESTION;
import static org.spine3.examples.todolist.modes.CreateLabelMode.CreateLabelModeConstants.SET_TITLE_MESSAGE;
import static org.spine3.examples.todolist.modes.Mode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;
import static org.spine3.examples.todolist.modes.Mode.ModeConstants.NEGATIVE_ANSWER;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
class CreateLabelMode extends Mode {

    private String title;
    private LabelColor color = LabelColor.LC_UNDEFINED;
    private final Map<String, Mode> modeMap = newHashMap();

    CreateLabelMode(TodoClient client, ConsoleReader reader) {
        super(client, reader);
    }

    @Override
    void start() throws IOException {
        String line = "";
        while (!line.equals(NEGATIVE_ANSWER)) {
            createLabel();
            line = obtainApproveValue(CREATE_ONE_MORE_LABEL_QUESTION);
        }
    }

    private void createLabel() throws IOException {
        final TaskLabelId labelId = TaskLabelId.newBuilder()
                                               .setValue(newUuid())
                                               .build();
        final String title = obtainLabelTitle(SET_TITLE_MESSAGE);
        final CreateBasicLabel createBasicLabel = CreateBasicLabel.newBuilder()
                                                                  .setLabelTitle(title)
                                                                  .setLabelId(labelId)
                                                                  .build();
        client.create(createBasicLabel);

        updateLabelDetailsIfNeeded(labelId);
        final String message = String.format(LABEL_CREATED_MESSAGE, labelId.getValue(), title, color);
        sendMessageToUser(message);
        clearValues();
    }

    private void updateLabelDetailsIfNeeded(TaskLabelId labelId) throws IOException {
        final String approveValue = obtainApproveValue(SET_LABEL_COLOR_QUESTION);
        if (approveValue.equals(NEGATIVE_ANSWER)) {
            return;
        }

        final String colorValue = obtainLabelColorValue(SET_COLOR_MESSAGE);
        final LabelColor color = LabelColor.valueOf(colorValue);
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
        this.color = color;
    }

    private void clearValues() {
        color = LabelColor.LC_UNDEFINED;
        title = "";
    }

    static class CreateLabelModeConstants {
        static final String CREATE_ONE_MORE_LABEL_QUESTION = "Do you want to create one more label?(y/n)";
        static final String SET_LABEL_COLOR_QUESTION = "Do you want to set the label color?(y/n)";
        static final String LABEL_CREATED_MESSAGE = "Created label with id: %s, title: %s, color: %s";
        static final String CHANGED_COLOR_MESSAGE = "Set the label color. Value: ";
        static final String CHANGED_TITLE_MESSAGE = "Set the label title. Value: ";
        static final String SET_COLOR_MESSAGE = "Please enter the label color.\n" +
                "Valid label colors:\nBLUE;\nGRAY;\nGREEN;\nRED.";
        static final String SET_TITLE_MESSAGE = "Please enter the label title: ";
        static final String HELP_MESSAGE = "0:    Help.\n" +
                "1:    Set the label title.\n" +
                "2:    Set the label color.\n" +
                "3:    Create the label [title is required].\n" +
                BACK_TO_THE_MENU_MESSAGE;

        private CreateLabelModeConstants() {
        }
    }
}
