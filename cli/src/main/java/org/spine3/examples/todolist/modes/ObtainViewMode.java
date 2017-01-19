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

import static org.spine3.examples.todolist.modes.ModeHelper.constructUserFriendlyDraftTasks;
import static org.spine3.examples.todolist.modes.ModeHelper.constructUserFriendlyLabelledTasks;
import static org.spine3.examples.todolist.modes.ModeHelper.constructUserFriendlyMyList;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;
import static org.spine3.examples.todolist.modes.ObtainViewMode.ObtainVewModeConstants.EMPTY_DRAFT_TASKS;
import static org.spine3.examples.todolist.modes.ObtainViewMode.ObtainVewModeConstants.EMPTY_LABELLED_TASKS;
import static org.spine3.examples.todolist.modes.ObtainViewMode.ObtainVewModeConstants.EMPTY_MY_LIST_TASKS;
import static org.spine3.examples.todolist.modes.ObtainViewMode.ObtainVewModeConstants.HELP_MESSAGE;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
public class ObtainViewMode {

    private final TodoClient client;

    ObtainViewMode(TodoClient client) {
        this.client = client;
    }

    @Command(abbrev = "0")
    public void help() {
        System.out.println(HELP_MESSAGE);
    }

    @Command(abbrev = "1")
    public void obtainLabelledTasksView() {
        final List<LabelledTasksView> labelledTasks = client.getLabelledTasksView();
        final String message = labelledTasks.isEmpty() ? EMPTY_LABELLED_TASKS :
                               constructUserFriendlyLabelledTasks(labelledTasks);
        sendMessageToUser(message);
    }

    @Command(abbrev = "2")
    public void obtainMyListView() {
        final MyListView myListView = client.getMyListView();
        final int itemsCount = myListView.getMyList()
                                         .getItemsCount();
        final boolean isEmpty = itemsCount == 0;
        final String message = isEmpty ? EMPTY_MY_LIST_TASKS : constructUserFriendlyMyList(myListView);
        sendMessageToUser(message);
    }

    @Command(abbrev = "3")
    public void obtainDraftTasksView() {
        final DraftTasksView draftTasksView = client.getDraftTasksView();
        final int itemsCount = draftTasksView.getDraftTasks()
                                             .getItemsCount();
        final boolean isEmpty = itemsCount == 0;
        final String message = isEmpty ? EMPTY_DRAFT_TASKS : constructUserFriendlyDraftTasks(draftTasksView);
        sendMessageToUser(message);
    }

    static class ObtainVewModeConstants {
        static final String EMPTY_LABELLED_TASKS = "No labelled tasks.";
        static final String EMPTY_MY_LIST_TASKS = "No tasks in the my list.";
        static final String EMPTY_DRAFT_TASKS = "No draft tasks.";
        static final String HELP_MESSAGE = "0:    Help.\n" +
                "1:    Obtain labelled tasks.\n" +
                "2:    Obtain my tasks.\n" +
                "3:    Obtain draft tasks.\n" +
                "exit: Exit from the mode.";
    }
}
