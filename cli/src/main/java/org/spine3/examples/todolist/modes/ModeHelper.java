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

import static org.spine3.examples.todolist.DateHelper.getDateFormat;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.DEFAULT_VALUE;
import static org.spine3.examples.todolist.modes.Mode.ModeConstants.LINE_SEPARATOR;

/**
 * @author Illia Shepilov
 */
class ModeHelper {

    private static final String MY_LIST_TASKS = "My list tasks";
    private static final String DRAFT_TASKS = "Draft tasks";
    private static final String LABELLED_TASKS = "Labelled tasks";
    private static final String TASK = "Task: ";
    private static final String LABEL_ID_VALUE = "Label id: ";
    private static final String TASK_ID_VALUE = "Task id: ";
    private static final String LABEL_TITLE_VALUE = "Label title: ";
    private static final String LABEL_COLOR_VALUE = "Label color: ";
    private static final String DESCRIPTION_VALUE = "Description: ";
    private static final String PRIORITY_VALUE = "Priority: ";
    private static final String DUE_DATE_VALUE = "Due date: ";

    private ModeHelper() {
    }

    static void sendMessageToUser(String message) {
        System.out.println(message);
    }

    static String constructUserFriendlyDate(long millis) {
        final SimpleDateFormat simpleDateFormat = getDateFormat();
        final String date = millis == 0 ? DEFAULT_VALUE : simpleDateFormat.format(new Date(millis));
        return date;
    }

    static String constructUserFriendlyMyList(MyListView myListView) {
        final StringBuilder builder = new StringBuilder();
        final List<TaskView> viewList = myListView.getMyList()
                                                  .getItemsList();
        builder.append(MY_LIST_TASKS);
        builder.append(LINE_SEPARATOR);
        for (TaskView view : viewList) {
            constructUserFriendlyTaskView(builder, view);
        }
        return builder.toString();
    }

    static String constructUserFriendlyDraftTasks(DraftTasksView draftTasksView) {
        final StringBuilder builder = new StringBuilder();
        final List<TaskView> viewList = draftTasksView.getDraftTasks()
                                                      .getItemsList();
        builder.append(DRAFT_TASKS);
        builder.append(LINE_SEPARATOR);
        for (TaskView view : viewList) {
            constructUserFriendlyTaskView(builder, view);
        }
        return builder.toString();
    }

    static String constructUserFriendlyLabelledTasks(List<LabelledTasksView> labelledTasksView) {
        final StringBuilder builder = new StringBuilder();
        builder.append(LABELLED_TASKS);
        builder.append(LINE_SEPARATOR);
        for (LabelledTasksView labelledView : labelledTasksView) {
            constructLabelledView(builder, labelledView);
        }

        return builder.toString();
    }

    private static void constructLabelledView(StringBuilder builder, LabelledTasksView labelledView) {
        builder.append(LABEL_ID_VALUE);
        builder.append(LINE_SEPARATOR);
        builder.append(LABEL_TITLE_VALUE);
        builder.append(labelledView.getLabelTitle());
        builder.append(LINE_SEPARATOR);
        builder.append(LABEL_COLOR_VALUE);
        builder.append(labelledView.getLabelColor());
        builder.append(LINE_SEPARATOR);
        final List<TaskView> viewList = labelledView.getLabelledTasks()
                                                    .getItemsList();
        for (TaskView view : viewList) {
            constructUserFriendlyTaskView(builder, view);
        }
    }

    private static void constructUserFriendlyTaskView(StringBuilder builder, TaskView view) {
        builder.append(TASK);
        builder.append(LINE_SEPARATOR);
        builder.append(TASK_ID_VALUE);
        final String taskIdValue = view.getId()
                                       .getValue();
        builder.append(taskIdValue);
        builder.append(LINE_SEPARATOR);
        builder.append(DESCRIPTION_VALUE);
        builder.append(view.getDescription());
        builder.append(LINE_SEPARATOR);
        builder.append(PRIORITY_VALUE);
        builder.append(view.getPriority());
        builder.append(LINE_SEPARATOR);
        builder.append(DUE_DATE_VALUE);
        final String date = constructUserFriendlyDate(Timestamps.toMillis(view.getDueDate()));
        builder.append(date);
        builder.append(LINE_SEPARATOR);
        builder.append(LABEL_ID_VALUE);
        builder.append(view.getLabelId());
        builder.append(LINE_SEPARATOR);
    }
}
