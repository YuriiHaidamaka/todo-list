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
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.q.projections.DraftTasksView;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.examples.todolist.q.projections.MyListView;

import java.util.List;

import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;

/**
 * @author Illia Shepilov
 */
public class ObtainViewMode {

    private static final String HELP_COMMAND = "0:    Help.\n" +
            "1:    Obtain labelled tasks.\n" +
            "2:    Obtain my tasks.\n" +
            "3:    Obtain draft tasks.\n" +
            "exit: Exit from the mode.";
    private final TodoClient client;

    ObtainViewMode(TodoClient client) {
        this.client = client;
    }

    @Command(abbrev = "0")
    public void help() {
        System.out.println(HELP_COMMAND);
    }

    @Command(abbrev = "1")
    public void obtainLabelledTasksView() {
        final List<LabelledTasksView> labelledTasks = client.getLabelledTasksView();
        final String result = labelledTasks.toString();
        sendMessageToUser(result);
    }

    @Command(abbrev = "2")
    public void obtainMyListView() {
        final MyListView myListView = client.getMyListView();
        final String result = myListView.toString();
        sendMessageToUser(result);
    }

    @Command(abbrev = "3")
    public void obtainDraftTasksView() {
        final DraftTasksView draftTasksView = client.getDraftTasksView();
        final String result = draftTasksView.toString();
        sendMessageToUser(result);
    }
}
