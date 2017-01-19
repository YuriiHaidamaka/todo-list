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
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.FinalizeDraft;
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.validator.IdValidator;
import org.spine3.examples.todolist.validator.Validator;

import java.io.BufferedReader;
import java.io.IOException;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.modes.DraftTaskMode.DraftTaskModeConstants.DRAFT_CREATED_MESSAGE;
import static org.spine3.examples.todolist.modes.DraftTaskMode.DraftTaskModeConstants.DRAFT_FINALIZED_MESSAGE;
import static org.spine3.examples.todolist.modes.DraftTaskMode.DraftTaskModeConstants.HELP_MESSAGE;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.ENTER_TASK_ID_MESSAGE;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
public class DraftTaskMode {

    private Validator idValidator;
    private final BufferedReader reader;
    private final TodoClient client;

    public DraftTaskMode(TodoClient client, BufferedReader reader) {
        this.client = client;
        this.reader = reader;
        initValidators();
    }

    @Command(abbrev = "0")
    public void help() {
        sendMessageToUser(HELP_MESSAGE);
    }

    @Command(abbrev = "1")
    public void createDraft() throws IOException {
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        final CreateDraft createDraft = CreateDraft.newBuilder()
                                                   .setId(taskId)
                                                   .build();
        client.create(createDraft);
        final String result = DRAFT_CREATED_MESSAGE + taskId.getValue();
        sendMessageToUser(result);
    }

    @Command(abbrev = "2")
    public void finalizeDraft() throws IOException {
        final String idValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(idValue)
                                    .build();
        final FinalizeDraft finalizeDraft = FinalizeDraft.newBuilder()
                                                         .setId(taskId)
                                                         .build();
        client.finalize(finalizeDraft);
        final String message = String.format(DRAFT_FINALIZED_MESSAGE, idValue);
        sendMessageToUser(message);
    }

    private String obtainTaskIdValue() throws IOException {
        sendMessageToUser(ENTER_TASK_ID_MESSAGE);
        String taskIdInput = reader.readLine();
        final boolean isValid = idValidator.validate(taskIdInput);

        if (!isValid) {
            sendMessageToUser(idValidator.getMessage());
            taskIdInput = obtainTaskIdValue();
        }
        return taskIdInput;
    }

    private void initValidators() {
        idValidator = new IdValidator();
    }

    static class DraftTaskModeConstants {
        static final String DRAFT_FINALIZED_MESSAGE = "Task with id value: %s finalized.";
        static final String DRAFT_CREATED_MESSAGE = "Created task draft with id: ";
        static final String HELP_MESSAGE = "0:    Help.\n" +
                "1:    Create task draft.\n" +
                "2:    Finalize task draft.\n" +
                "exit: Exit from the mode.";
    }
}