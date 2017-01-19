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

import com.google.protobuf.util.Timestamps;
import org.spine3.examples.todolist.q.projections.DraftTasksView;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.examples.todolist.q.projections.MyListView;
import org.spine3.examples.todolist.q.projections.TaskView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.spine3.examples.todolist.CommonHelper.getDateFormat;

/**
 * @author Illia Shepilov
 */
class ModeHelper {

    private static final String NEW_LINE = "\n";

    private ModeHelper() {
    }

    static void sendMessageToUser(String message) {
        System.out.println(message);
    }

    static String constructUserFriendlyDate(long millis) {
        final SimpleDateFormat simpleDateFormat = getDateFormat();
        final String date = simpleDateFormat.format(new Date(millis));
        return date;
    }

    static String constructUserFriendlyMyList(MyListView myListView) {
        final StringBuilder builder = new StringBuilder();
        final List<TaskView> viewList = myListView.getMyList()
                                                  .getItemsList();
        builder.append("My list tasks");
        builder.append(NEW_LINE);
        for (TaskView view : viewList) {
            constructUserFriendlyTaskView(builder, view);
        }
        return builder.toString();
    }

    static String constructUserFriendlyDraftTasks(DraftTasksView draftTasksView) {
        final StringBuilder builder = new StringBuilder();
        final List<TaskView> viewList = draftTasksView.getDraftTasks()
                                                      .getItemsList();
        builder.append("Draft tasks");
        builder.append(NEW_LINE);
        for (TaskView view : viewList) {
            constructUserFriendlyTaskView(builder, view);
        }
        return builder.toString();
    }

    static String constructUserFriendlyLabelledTasks(List<LabelledTasksView> labelledTasksView) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Labelled tasks");
        builder.append(NEW_LINE);
        for (LabelledTasksView labelledView : labelledTasksView) {
            constructLabelledView(builder, labelledView);
        }

        return builder.toString();
    }

    private static void constructLabelledView(StringBuilder builder, LabelledTasksView labelledView) {
        builder.append("Label id: ");
        builder.append(NEW_LINE);
        builder.append("Label title: ");
        builder.append(labelledView.getLabelTitle());
        builder.append(NEW_LINE);
        builder.append("Label color: ");
        builder.append(labelledView.getLabelColor());
        builder.append(NEW_LINE);
        final List<TaskView> viewList = labelledView.getLabelledTasks()
                                                    .getItemsList();
        for (TaskView view : viewList) {
            constructUserFriendlyTaskView(builder, view);
        }
    }

    private static void constructUserFriendlyTaskView(StringBuilder builder, TaskView view) {
        builder.append("Task: ");
        builder.append(NEW_LINE);
        builder.append("Task id: ");
        builder.append(view.getId()
                           .getValue());
        builder.append(NEW_LINE);
        builder.append("Description: ");
        builder.append(view.getDescription());
        builder.append(NEW_LINE);
        builder.append("Priority: ");
        builder.append(view.getPriority());
        builder.append(NEW_LINE);
        builder.append("Due date: ");
        final String date = constructUserFriendlyDate(Timestamps.toMillis(view.getDueDate()));
        builder.append(date);
        builder.append(NEW_LINE);
        builder.append("Label id: ");
        builder.append(view.getLabelId());
        builder.append(NEW_LINE);
    }
}
