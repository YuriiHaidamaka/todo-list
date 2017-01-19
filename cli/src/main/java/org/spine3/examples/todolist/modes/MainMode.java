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
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.c.commands.ReopenTask;
import org.spine3.examples.todolist.c.commands.RestoreDeletedTask;
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.validator.IdValidator;
import org.spine3.examples.todolist.validator.Validator;

import java.io.BufferedReader;
import java.io.IOException;

import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.CREATE_LABEL_PROMPT;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.CREATE_LABEL_TITLE;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.CREATE_TASK_PROMPT;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.CREATE_TASK_TITLE;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.DRAFT_TASK_PROMPT;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.DRAFT_TASK_TITLE;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.ENTER_LABEL_ID_MESSAGE;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.ENTER_TASK_ID_MESSAGE;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.HELP_MESSAGE;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.OBTAIN_VIEWS_PROMPT;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.OBTAIN_VIEW_TITLE;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.UPDATE_LABEL_PROMPT;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.UPDATE_LABEL_TITLE;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.UPDATE_TASK_PROMPT;
import static org.spine3.examples.todolist.modes.MainMode.MainModeConstants.UPDATE_TASK_TITLE;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
public class MainMode implements ShellDependent {

    private final TodoClient client;
    private final BufferedReader reader;
    private Validator idValidator;
    private Shell shell;

    public MainMode(TodoClient client, BufferedReader reader) {
        this.client = client;
        this.reader = reader;
        initValidators();
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
    public void updateTask() throws IOException {
        ShellFactory.createSubshell(UPDATE_TASK_PROMPT, shell, UPDATE_TASK_TITLE, new UpdateTaskMode(client, reader))
                    .commandLoop();
    }

    @Command(abbrev = "3")
    public void createLabel() throws IOException {
        ShellFactory.createSubshell(CREATE_LABEL_PROMPT, shell, CREATE_LABEL_TITLE, new CreateLabelMode(client, reader))
                    .commandLoop();
    }

    @Command(abbrev = "4")
    public void updateLabel() throws IOException {
        ShellFactory.createSubshell(UPDATE_LABEL_PROMPT, shell, UPDATE_LABEL_TITLE, new UpdateLabelMode(client, reader))
                    .commandLoop();
    }

    @Command(abbrev = "5")
    public void assignLabel() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final String labelIdValue = obtainLabelIdValue();
        final TaskLabelId labelId = TaskLabelId.newBuilder()
                                               .setValue(labelIdValue)
                                               .build();
        final AssignLabelToTask assignLabelToTask = AssignLabelToTask.newBuilder()
                                                                     .setId(taskId)
                                                                     .setLabelId(labelId)
                                                                     .build();
        client.assignLabel(assignLabelToTask);
    }

    @Command(abbrev = "6")
    public void removeLabel() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final String labelIdValue = obtainLabelIdValue();
        final TaskLabelId labelId = TaskLabelId.newBuilder()
                                               .setValue(labelIdValue)
                                               .build();
        final RemoveLabelFromTask removeLabelFromTask = RemoveLabelFromTask.newBuilder()
                                                                           .setId(taskId)
                                                                           .setLabelId(labelId)
                                                                           .build();
        client.removeLabel(removeLabelFromTask);
    }

    @Command(abbrev = "7")
    public void deleteTask() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final DeleteTask deleteTask = DeleteTask.newBuilder()
                                                .setId(taskId)
                                                .build();
        client.delete(deleteTask);
    }

    @Command(abbrev = "8")
    public void reopenTask() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final ReopenTask reopenTask = ReopenTask.newBuilder()
                                                .setId(taskId)
                                                .build();
        client.reopen(reopenTask);
    }

    @Command(abbrev = "9")
    public void restoreTask() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final RestoreDeletedTask restoreDeletedTask = RestoreDeletedTask.newBuilder()
                                                                        .setId(taskId)
                                                                        .build();
        client.restore(restoreDeletedTask);
    }

    @Command(abbrev = "10")
    public void completeTask() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final CompleteTask completeTask = CompleteTask.newBuilder()
                                                      .setId(taskId)
                                                      .build();
        client.complete(completeTask);
    }

    @Command(abbrev = "11")
    public void obtainViews() throws IOException {
        ShellFactory.createSubshell(OBTAIN_VIEWS_PROMPT, shell, OBTAIN_VIEW_TITLE, new ObtainViewMode(client))
                    .commandLoop();
    }

    @Command(abbrev = "12")
    public void draftTaskMode() throws IOException {
        ShellFactory.createSubshell(DRAFT_TASK_PROMPT, shell, DRAFT_TASK_TITLE, new DraftTaskMode(client, reader))
                    .commandLoop();
    }

    private String obtainLabelIdValue() throws IOException {
        sendMessageToUser(ENTER_LABEL_ID_MESSAGE);
        String labelIdInput = reader.readLine();
        final boolean isValid = idValidator.validate(labelIdInput);

        if (!isValid) {
            sendMessageToUser(idValidator.getMessage());
            labelIdInput = obtainLabelIdValue();
        }
        return labelIdInput;
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

    public static class MainModeConstants {
        public static final String HELP_ADVICE = "Enter 'help' or '0' to view all commands.\n";
        public static final String HELP_MESSAGE = "0:    Help.\n" +
                "1:    Create the task mode.\n" +
                "2:    Update the task mode.\n" +
                "3:    Create the label mode.\n" +
                "4:    Update the label mode.\n" +
                "5:    Assign label to task mode.\n" +
                "6:    Remove label from task mode.\n" +
                "7:    Delete task mode.\n" +
                "8:    Reopen task mode.\n" +
                "9:    Restore task mode.\n" +
                "10:   Complete task mode.\n" +
                "11:   Obtain views mode.\n" +
                "12:   Draft task mode.\n" +
                "exit: Exit from the mode.";
        static final String CREATE_TASK_MODE = "******************Create task mode******************\n";
        static final String CREATE_TASK_TITLE = CREATE_TASK_MODE + HELP_ADVICE +
                CreateTaskMode.CreateTaskModeConstants.HELP_MESSAGE;
        static final String UPDATE_TASK_MODE = "******************Update task mode******************\n";
        static final String UPDATE_TASK_TITLE = UPDATE_TASK_MODE + HELP_ADVICE +
                UpdateTaskMode.UpdateTaskModeConstants.HELP_MESSAGE;
        static final String CREATE_LABEL_MODE = "******************Create label mode*****************\n";
        static final String CREATE_LABEL_TITLE = CREATE_LABEL_MODE + HELP_ADVICE +
                CreateLabelMode.CreateLabelModeConstants.HELP_MESSAGE;
        static final String UPDATE_LABEL_MODE = "******************Update label mode*****************\n";
        static final String UPDATE_LABEL_TITLE = UPDATE_LABEL_MODE + HELP_ADVICE +
                UpdateLabelMode.UpdateLabelModeConstants.HELP_MESSAGE;
        static final String OBTAIN_VIEW_MODE = "******************Obtain view mode******************\n";
        static final String OBTAIN_VIEW_TITLE = OBTAIN_VIEW_MODE + HELP_ADVICE +
                ObtainViewMode.ObtainVewModeConstants.HELP_MESSAGE;
        static final String DRAFT_TASK_MODE = "******************Draft task mode*******************\n";
        static final String DRAFT_TASK_TITLE = DRAFT_TASK_MODE + HELP_ADVICE +
                DraftTaskMode.DraftTaskModeConstants.HELP_MESSAGE;
        static final String ENTER_TASK_ID_MESSAGE = "Please enter the task id: ";
        static final String ENTER_LABEL_ID_MESSAGE = "Please enter the label id: ";
        static final String CREATE_TASK_PROMPT = "create-task";
        static final String UPDATE_TASK_PROMPT = "update-task";
        static final String CREATE_LABEL_PROMPT = "create-label";
        static final String UPDATE_LABEL_PROMPT = "update-label";
        static final String OBTAIN_VIEWS_PROMPT = "obtain-views";
        static final String DRAFT_TASK_PROMPT = "draft-task";
    }
}
