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

package org.spine3.examples.todolist.mode;

import com.google.protobuf.Timestamp;
import org.spine3.change.StringChange;
import org.spine3.change.TimestampChange;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsChange;
import org.spine3.examples.todolist.PriorityChange;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.FinalizeDraft;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;

import static org.spine3.examples.todolist.mode.CreateTaskMode.CreateTaskModeConstants.EMPTY;

/**
 * Serves as utility class which provides methods for commands constructing.
 *
 * @author Illia Shepilov
 */
class TodoListCommands {

    private TodoListCommands() {
    }

    static LabelDetailsChange createLabelDetailsChange(LabelDetails newLabelDetails,
                                                       LabelDetails previousLabelDetails) {
        final LabelDetailsChange result = LabelDetailsChange.newBuilder()
                                                            .setNewDetails(newLabelDetails)
                                                            .setPreviousDetails(previousLabelDetails)
                                                            .build();
        return result;
    }

    static LabelDetailsChange createLabelDetailsChange(LabelDetails newLabelDetails) {
        final LabelDetailsChange result = LabelDetailsChange.newBuilder()
                                                            .setNewDetails(newLabelDetails)
                                                            .build();
        return result;
    }

    static LabelDetails createLabelDetails(String title, LabelColor labelColor) {
        final LabelDetails result = LabelDetails.newBuilder()
                                                .setColor(labelColor)
                                                .setTitle(title)
                                                .build();
        return result;
    }

    static CreateBasicLabel createBasicLabelCmd(TaskLabelId labelId, String title) {
        final CreateBasicLabel result = CreateBasicLabel.newBuilder()
                                                        .setLabelTitle(title)
                                                        .setLabelId(labelId)
                                                        .build();
        return result;
    }

    static UpdateLabelDetails createUpdateLabelDetailsCmd(TaskLabelId labelId,
                                                          LabelDetailsChange labelDetailsChange) {
        final UpdateLabelDetails result = UpdateLabelDetails.newBuilder()
                                                            .setLabelDetailsChange(labelDetailsChange)
                                                            .setId(labelId)
                                                            .build();
        return result;
    }

    static UpdateTaskDescription createUpdateTaskDescriptionCmd(TaskId taskId, StringChange change) {
        final UpdateTaskDescription result = UpdateTaskDescription.newBuilder()
                                                                  .setId(taskId)
                                                                  .setDescriptionChange(change)
                                                                  .build();
        return result;
    }

    static StringChange createStringChange(String newDescription, String previousDescription) {
        final StringChange result = StringChange.newBuilder()
                                                .setNewValue(newDescription)
                                                .setPreviousValue(previousDescription)
                                                .build();
        return result;
    }

    static StringChange createStringChange(String description) {
        final StringChange result = StringChange.newBuilder()
                                                .setPreviousValue(EMPTY)
                                                .setNewValue(description)
                                                .build();
        return result;
    }

    static FinalizeDraft createFinalizeDraftCmd(TaskId taskId) {
        final FinalizeDraft result = FinalizeDraft.newBuilder()
                                                  .setId(taskId)
                                                  .build();
        return result;
    }

    static UpdateTaskDueDate createUpdateTaskDueDateCmd(TaskId taskId, TimestampChange change) {
        final UpdateTaskDueDate result = UpdateTaskDueDate.newBuilder()
                                                          .setId(taskId)
                                                          .setDueDateChange(change)
                                                          .build();
        return result;
    }

    static TimestampChange createTimestampChange(Timestamp dueDate) {
        final TimestampChange result = TimestampChange.newBuilder()
                                                      .setNewValue(dueDate)
                                                      .build();
        return result;
    }

    static TimestampChange createTimestampChangeMode(Timestamp newDueDate, Timestamp previousDueDate) {
        final TimestampChange result = TimestampChange.newBuilder()
                                                      .setPreviousValue(previousDueDate)
                                                      .setNewValue(newDueDate)
                                                      .build();
        return result;
    }

    static UpdateTaskPriority createUpdateTaskPriorityCmd(TaskId taskId, PriorityChange change) {
        final UpdateTaskPriority result = UpdateTaskPriority.newBuilder()
                                                            .setId(taskId)
                                                            .setPriorityChange(change)
                                                            .build();
        return result;
    }

    static PriorityChange createPriorityChange(TaskPriority priority) {
        final PriorityChange result = PriorityChange.newBuilder()
                                                    .setNewValue(priority)
                                                    .build();
        return result;
    }

    static PriorityChange createPriorityChange(TaskPriority newTaskPriority, TaskPriority previousTaskPriority) {
        final PriorityChange result = PriorityChange.newBuilder()
                                                    .setPreviousValue(previousTaskPriority)
                                                    .setNewValue(newTaskPriority)
                                                    .build();
        return result;
    }
}
