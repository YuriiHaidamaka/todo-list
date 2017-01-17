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

package org.spine3.examples.todolist;

import asg.cliche.Command;
import asg.cliche.Shell;
import asg.cliche.ShellDependent;
import asg.cliche.ShellFactory;
import com.google.common.base.Charsets;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.client.CommandLineTodoClient;
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.modes.CreateLabelMode;
import org.spine3.examples.todolist.modes.CreateTaskMode;
import org.spine3.examples.todolist.modes.UpdateLabelMode;
import org.spine3.examples.todolist.modes.UpdateTaskMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.spine3.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;

/**
 * @author Illia Shepilov
 */
public class EntryPoint implements ShellDependent {

    public static final String HELP_ADVICE = "Enter 'help' to view all commands";
    private Shell shell;
    private TodoClient client = new CommandLineTodoClient("localhost", DEFAULT_CLIENT_SERVICE_PORT);
    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8));

    private static final String CHOOSE_THE_COMMAND = "Choose the command:\n" +
            "1:  Create task mode\n" +
            "2:  Update task mode\n" +
            "3:  Create label mode\n" +
            "4:  Update label mode\n" +
            "5:  Assign label to task\n" +
            "6:  Remove label from task\n";

    @Override
    public void cliSetShell(Shell theShell) {
        this.shell = theShell;
    }

    @Command(abbrev = "0")
    public String help() throws IOException {
        return CHOOSE_THE_COMMAND;
    }

    @Command(abbrev = "1")
    public void createTask() throws IOException {
        ShellFactory.createSubshell("create-task", shell, "Create task mode\n" + HELP_ADVICE, new CreateTaskMode(client, reader))
                    .commandLoop();
    }

    @Command(abbrev = "2")
    public void updateTask() throws IOException {
        ShellFactory.createSubshell("update-task", shell, "Update task mode\n" + HELP_ADVICE, new UpdateTaskMode(client, reader))
                    .commandLoop();
    }

    @Command(abbrev = "3")
    public void createLabel() throws IOException {
        ShellFactory.createSubshell("create-label", shell, "Create label mode\n" + HELP_ADVICE, new CreateLabelMode(client, reader))
                    .commandLoop();
    }

    @Command(abbrev = "4")
    public void updateLabel() throws IOException {
        ShellFactory.createSubshell("update-label", shell, "Update label mode\n" + HELP_ADVICE, new UpdateLabelMode(client, reader))
                    .commandLoop();
    }

    @Command(abbrev = "5")
    public void assignLabel() throws IOException {
        System.out.println("Please enter the task id: ");
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(reader.readLine())
                                    .build();
        System.out.println("Please enter the label id: ");
        final TaskLabelId labelId = TaskLabelId.newBuilder()
                                               .setValue(reader.readLine())
                                               .build();
        final AssignLabelToTask assignLabelToTask = AssignLabelToTask.newBuilder()
                                                                     .setId(taskId)
                                                                     .setLabelId(labelId)
                                                                     .build();
        client.assignLabel(assignLabelToTask);
    }

    @Command(abbrev = "6")
    public void removeLabel() throws IOException {
        System.out.println("Please enter the task id: ");
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(reader.readLine())
                                    .build();
        System.out.println("Please enter the label id: ");
        final TaskLabelId labelId = TaskLabelId.newBuilder()
                                               .setValue(reader.readLine())
                                               .build();
        final RemoveLabelFromTask removeLabelFromTask = RemoveLabelFromTask.newBuilder()
                                                                           .setId(taskId)
                                                                           .setLabelId(labelId)
                                                                           .build();
        client.removeLabel(removeLabelFromTask);
    }

    @Command(abbrev = "7")
    public void deleteTask() throws IOException {
        System.out.println("Please enter the task id: ");
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(reader.readLine())
                                    .build();
        final DeleteTask deleteTask = DeleteTask.newBuilder()
                                                .setId(taskId)
                                                .build();
        client.delete(deleteTask);
    }

    public static void main(String[] args) throws IOException {
        ShellFactory.createConsoleShell("todo", "Enter 'help' to view all commands", new EntryPoint())
                    .commandLoop();
    }
}
