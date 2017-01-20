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
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import asg.cliche.ShellFactory;
import org.spine3.examples.todolist.client.TodoClient;

import java.io.BufferedReader;
import java.io.IOException;

import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.CREATE_LABEL_PROMPT;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.CREATE_LABEL_TITLE;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.CREATE_TASK_PROMPT;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.CREATE_TASK_TITLE;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.DRAFT_TASKS_PROMPT;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.DRAFT_TASKS_TITLE;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.HELP_MESSAGE;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.LABELLED_TASKS_PROMPT;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.LABELLED_TASKS_TITLE;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.MY_TASKS_PROMPT;
import static org.spine3.examples.todolist.modes.GeneralMode.MainModeConstants.MY_TASKS_TITLE;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
public class GeneralMode implements ShellDependent {

    private final TodoClient client;
    private final BufferedReader reader;
    private Shell shell;

    public GeneralMode(TodoClient client, BufferedReader reader) {
        this.client = client;
        this.reader = reader;
    }

    @Override
    public void cliSetShell(Shell theShell) {
        this.shell = theShell;
    }

    @Command(abbrev = "0")
    public String help() throws IOException {
        return HELP_MESSAGE;
    }

    @Command(abbrev = "1")
    public void createTask() throws IOException {
        ShellFactory.createSubshell(CREATE_TASK_PROMPT, shell, CREATE_TASK_TITLE, new CreateTaskMode(client, reader))
                    .commandLoop();
    }

    @Command(abbrev = "2")
    public void createLabel() throws IOException {
        ShellFactory.createSubshell(CREATE_LABEL_PROMPT, shell, CREATE_LABEL_TITLE, new CreateLabelMode(client, reader))
                    .commandLoop();
    }

    @Command(abbrev = "3")
    public void showDraftTasks() throws IOException {
        ShellFactory.createSubshell(DRAFT_TASKS_PROMPT, shell, DRAFT_TASKS_TITLE, new DraftTasksMode(client, reader))
                    .commandLoop();
    }

    @Command(abbrev = "4")
    public void showLabelledTasks() throws IOException {
        ShellFactory.createSubshell(LABELLED_TASKS_PROMPT, shell, LABELLED_TASKS_TITLE, new LabelledTasksMode(client,
                                                                                                              reader))
                    .commandLoop();
    }

    @Command(abbrev = "5")
    public void showMyTasks() throws IOException {
        ShellFactory.createSubshell(MY_TASKS_PROMPT, shell, MY_TASKS_TITLE, new MyTasksMode(client, reader))
                    .commandLoop();
    }

    public static class MainModeConstants {
        public static final String HELP_ADVICE = "Enter 'help' or '0' to view all commands.\n";
        static final String CREATE_TASK_MODE = "********************Create task menu********************\n";
        static final String CREATE_TASK_TITLE = CREATE_TASK_MODE + HELP_ADVICE +
                CreateTaskMode.CreateTaskModeConstants.HELP_MESSAGE;
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
        static final String CREATE_TASK_PROMPT = "create-task";
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
                "exit: Exit from the mode.";
    }
}
