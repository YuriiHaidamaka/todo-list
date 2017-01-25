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

import com.google.common.collect.Maps;
import jline.console.ConsoleReader;
import org.spine3.examples.todolist.client.TodoClient;

import java.io.IOException;
import java.util.Map;

import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.EXIT;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.HELP_MESSAGE;
import static org.spine3.examples.todolist.modes.Mode.ModeConstants.INCORRECT_COMMAND;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;

/**
 * @author Illia Shepilov
 */
public class GeneralMode extends Mode {

    private final Map<String, Mode> modeMap = Maps.newHashMap();

    public GeneralMode(TodoClient client, ConsoleReader reader) {
        super(client, reader);
        modeMap.put("0", new HelpMode(client, reader, HELP_MESSAGE));
        modeMap.put("1", new CreateTaskMode(client, reader));
        modeMap.put("2", new CreateLabelMode(client, reader));
        modeMap.put("3", new DraftTasksMode(client, reader));
        modeMap.put("4", new LabelledTasksMode(client, reader));
        modeMap.put("5", new MyTasksMode(client, reader));
    }

    @Override
    void start() throws IOException {
        sendMessageToUser(HELP_MESSAGE);
        String line = "";
        while (!line.equals(EXIT)) {
            line = reader.readLine();

            final Mode mode = modeMap.get(line);

            if (mode == null) {
                sendMessageToUser(INCORRECT_COMMAND);
                continue;
            }

            mode.start();
            sendMessageToUser(HELP_MESSAGE);
        }
    }

    public static class MainModeConstants {
        static final String EXIT = "exit";
        public static final String HELP_ADVICE = "Enter 'help' or '0' to view all commands.\n";
        static final String CREATE_LABEL_MODE = "********************Create label menu*******************\n";
        static final String CREATE_LABEL_TITLE = CREATE_LABEL_MODE + HELP_ADVICE +
                CreateLabelMode.CreateLabelModeConstants.HELP_MESSAGE;
        static final String LABELLED_TASKS_MODE = "*******************Labelled tasks menu*******************\n";
        static final String LABELLED_TASKS_TITLE = LABELLED_TASKS_MODE + HELP_ADVICE +
                LabelledTasksMode.LabelledTasksModeConstants.HELP_MESSAGE;
        static final String DRAFT_TASKS_MODE = "*********************Draft tasks menu********************\n";
        static final String DRAFT_TASKS_TITLE = DRAFT_TASKS_MODE + HELP_ADVICE +
                DraftTasksMode.DraftTasksModeConstants.HELP_MESSAGE;
        static final String MY_TASKS_MODE = "***********************My tasks menu*********************\n";
        static final String MY_TASKS_TITLE = MY_TASKS_MODE + HELP_ADVICE +
                MyTasksMode.MyTasksModeConstants.HELP_MESSAGE;
        static final String ENTER_TASK_ID_MESSAGE = "Please enter the task id: ";
        static final String ENTER_LABEL_ID_MESSAGE = "Please enter the label id: ";
        static final String CREATE_LABEL_PROMPT = "create-label";
        static final String DRAFT_TASKS_PROMPT = "draft-tasks";
        static final String LABELLED_TASKS_PROMPT = "labelled-tasks";
        static final String MY_TASKS_PROMPT = "my-tasks";
        public static final String HELP_MESSAGE = "0:    Help.\n" +
                "1:    Create the task.\n" +
                "2:    Create the label.\n" +
                "3:    Show the tasks in the draft state.\n" +
                "4:    Show the labelled tasks.\n" +
                "5:    Show my tasks.\n" +
                "exit: Exit from the application.";

        private MainModeConstants() {
        }
    }
}
