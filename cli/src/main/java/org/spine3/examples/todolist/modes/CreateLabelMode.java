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

import java.io.BufferedReader;
import java.io.IOException;

import static org.spine3.base.Identifiers.newUuid;

/**
 * @author Illia Shepilov
 */
public class CreateLabelMode {

    private final BufferedReader reader;
    private final TodoClient client;
    private String title;
    private LabelColor color;
    private static final String ENTER_COLOR_MESSAGE = "Please enter the label color: ";
    private static final String ENTER_TITLE_MESSAGE = "Please enter the label title: ";
    private final static String HELP_MESSAGE = "0:  Help\n" +
            "1:  Enter the label title\n" +
            "2:  Enter the label color\n" +
            "3:  Create basic label [title required]";

    public CreateLabelMode(TodoClient client, BufferedReader reader) {
        this.client = client;
        this.reader = reader;
    }

    @Command(abbrev = "0")
    public void help() {
        System.out.println(HELP_MESSAGE);
    }

    @Command(abbrev = "1")
    public void enterTitle() throws IOException {
        System.out.println(ENTER_TITLE_MESSAGE);
        final String title = reader.readLine();
        this.title = title;
    }

    @Command(abbrev = "2")
    public void enterColor() throws IOException {
        System.out.println(ENTER_COLOR_MESSAGE);
        final LabelColor color = LabelColor.valueOf(reader.readLine());
        this.color = color;
    }

    @Command(abbrev = "3")
    public void createLabel() {
        final TaskLabelId labelId = TaskLabelId.newBuilder()
                                               .setValue(newUuid())
                                               .build();
        final CreateBasicLabel createBasicLabel = CreateBasicLabel.newBuilder()
                                                                  .setLabelTitle(title)
                                                                  .setLabelId(labelId)
                                                                  .build();
        client.create(createBasicLabel);
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
