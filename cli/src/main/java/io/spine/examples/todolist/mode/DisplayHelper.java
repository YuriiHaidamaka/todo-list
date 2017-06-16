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

package io.spine.examples.todolist.mode;

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.q.projection.TaskView;

import java.util.Date;

import static com.google.protobuf.util.Timestamps.toMillis;
import static io.spine.examples.todolist.mode.Mode.ModeConstants.DEFAULT_VALUE;
import static io.spine.examples.todolist.mode.Mode.getDateFormat;

/**
 * Serves as utility class for creating user friendly representation of the information.
 *
 * @author Illia Shepilov
 */
public class DisplayHelper {

    private static final String TASK = "Task: ";
    private static final String LABEL_ID_VALUE = "Label id: ";
    private static final String TASK_ID_VALUE = "Task id: ";
    private static final String DESCRIPTION_VALUE = "Description: ";
    private static final String PRIORITY_VALUE = "Priority: ";
    private static final String DUE_DATE_VALUE = "Due date: ";

    private DisplayHelper() {
    }

    static String constructUserFriendlyDate(Timestamp timestamp) {
        final long millis = toMillis(timestamp);
        return millis == 0
               ? DEFAULT_VALUE
               : getDateFormat().format(new Date(millis));
    }

    public static String constructUserFriendlyTaskView(TaskView view) {
        final StringBuilder builder = new StringBuilder();
        final String date = constructUserFriendlyDate(view.getDueDate());
        final String taskIdValue = view.getId()
                                       .getValue();
        builder.append(TASK)
               .append(Mode.ModeConstants.LINE_SEPARATOR)
               .append(TASK_ID_VALUE)
               .append(taskIdValue)
               .append(Mode.ModeConstants.LINE_SEPARATOR)
               .append(DESCRIPTION_VALUE)
               .append(view.getDescription())
               .append(Mode.ModeConstants.LINE_SEPARATOR)
               .append(PRIORITY_VALUE)
               .append(view.getPriority())
               .append(Mode.ModeConstants.LINE_SEPARATOR)
               .append(DUE_DATE_VALUE)
               .append(date)
               .append(Mode.ModeConstants.LINE_SEPARATOR)
               .append(LABEL_ID_VALUE)
               .append(view.getLabelId())
               .append(Mode.ModeConstants.LINE_SEPARATOR);
        return builder.toString();
    }
}
