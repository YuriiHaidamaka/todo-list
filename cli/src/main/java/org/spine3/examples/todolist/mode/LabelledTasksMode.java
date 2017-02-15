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

import jline.console.ConsoleReader;
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;

import java.io.IOException;
import java.util.List;

import static org.spine3.examples.todolist.mode.GeneralMode.MainModeConstants.TODO_PROMPT;
import static org.spine3.examples.todolist.mode.LabelledTasksMode.LabelledTasksModeConstants.EMPTY_LABELLED_TASKS;
import static org.spine3.examples.todolist.mode.LabelledTasksMode.LabelledTasksModeConstants.HELP_MESSAGE;
import static org.spine3.examples.todolist.mode.LabelledTasksMode.LabelledTasksModeConstants.LABELLED_TASKS_MENU;
import static org.spine3.examples.todolist.mode.LabelledTasksMode.LabelledTasksModeConstants.LABELLED_TASKS_PROMPT;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.BACK;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.BACK_TO_THE_MENU_MESSAGE;
import static org.spine3.examples.todolist.mode.Mode.ModeConstants.LINE_SEPARATOR;
import static org.spine3.examples.todolist.mode.ModeHelper.constructUserFriendlyLabelledTasks;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
public class LabelledTasksMode extends CommonMode {

    private final TodoClient client;
    private final ConsoleReader reader;

    LabelledTasksMode(TodoClient client, ConsoleReader reader) {
        super(client, reader);
        this.client = client;
        this.reader = reader;
    }

    @Override
    void start() throws IOException {
        reader.setPrompt(LABELLED_TASKS_PROMPT);
        sendMessageToUser(LABELLED_TASKS_MENU);

        final ShowLabelledTasksMode showLabelledTasksMode = new ShowLabelledTasksMode(client, reader);
        initModeMap(showLabelledTasksMode);

        showLabelledTasksMode.start();
        sendMessageToUser(HELP_MESSAGE);
        String line = "";

        while (!line.equals(BACK)) {
            line = reader.readLine();
            final Mode mode = modeMap.get(line);
            if (mode != null) {
                mode.start();
            }
        }
        reader.setPrompt(TODO_PROMPT);
    }

    private void initModeMap(ShowLabelledTasksMode labelledTasksMode) {
        modeMap.put("1", labelledTasksMode);
    }

    private class ShowLabelledTasksMode extends Mode {

        private ShowLabelledTasksMode(TodoClient client, ConsoleReader reader) {
            super(reader);
        }

        @Override
        void start() throws IOException {
            final List<LabelledTasksView> labelledTasks = client.getLabelledTasksView();
            final String message = labelledTasks.isEmpty() ? EMPTY_LABELLED_TASKS :
                                   constructUserFriendlyLabelledTasks(labelledTasks);
            sendMessageToUser(message);
        }
    }

    static class LabelledTasksModeConstants {
        static final String LABELLED_TASKS_MENU = "***************** Labelled tasks menu ****************" +
                LINE_SEPARATOR;
        static final String LABELLED_TASKS_PROMPT = "labelled-tasks>";
        static final String EMPTY_LABELLED_TASKS = "No labelled tasks.";
        static final String HELP_MESSAGE = "0:    Help." + LINE_SEPARATOR +
                "1:    Show the labelled tasks." + LINE_SEPARATOR +
                CommonMode.CommonModeConstants.HELP_MESSAGE +
                BACK_TO_THE_MENU_MESSAGE;

        private LabelledTasksModeConstants() {
        }
    }
}
