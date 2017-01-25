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
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.q.projections.MyListView;

import java.io.IOException;

import static org.spine3.examples.todolist.modes.Mode.ModeConstants.BACK;
import static org.spine3.examples.todolist.modes.ModeHelper.constructUserFriendlyMyList;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;
import static org.spine3.examples.todolist.modes.MyTasksMode.MyTasksModeConstants.EMPTY_MY_LIST_TASKS;
import static org.spine3.examples.todolist.modes.MyTasksMode.MyTasksModeConstants.HELP_MESSAGE;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("unused")
public class MyTasksMode extends CommonMode {

    MyTasksMode(TodoClient client, ConsoleReader reader) {
        super(client, reader);
    }

    @Override
    void start() throws IOException {
        final ShowMyTasksMode showMyTasksMode = new ShowMyTasksMode(client, reader);
        modeMap.put("1", showMyTasksMode);
        showMyTasksMode.start();
        sendMessageToUser(HELP_MESSAGE);
        String line = reader.readLine();
        while (!line.equals(BACK)) {
            final Mode mode = modeMap.get(line);
            if (mode != null) {
                mode.start();
            }

            line = reader.readLine();
        }
    }

    private static class ShowMyTasksMode extends Mode {

        private ShowMyTasksMode(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            final MyListView myListView = client.getMyListView();
            final int itemsCount = myListView.getMyList()
                                             .getItemsCount();
            final boolean isEmpty = itemsCount == 0;
            final String message = isEmpty ? EMPTY_MY_LIST_TASKS : constructUserFriendlyMyList(myListView);
            sendMessageToUser(message);

        }
    }

    static class MyTasksModeConstants {
        static final String EMPTY_MY_LIST_TASKS = "No tasks in the my list.";
        static final String HELP_MESSAGE = "0:    Help.\n" +
                "1:    Show all my tasks.\n" +
                CommonMode.CommonModeConstants.HELP_MESSAGE +
                "exit: Exit";

        private MyTasksModeConstants() {
        }
    }
}
