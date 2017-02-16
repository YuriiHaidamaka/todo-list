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

package org.spine3.examples.todolist.c.aggregate.failures;

import org.spine3.examples.todolist.AssignLabelToTaskFailed;
import org.spine3.examples.todolist.FailedTaskCommandDetails;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.RemoveLabelFromTaskFailed;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.aggregate.TaskLabelsPart;
import org.spine3.examples.todolist.c.failures.CannotAssignLabelToTask;
import org.spine3.examples.todolist.c.failures.CannotRemoveLabelFromTask;

/**
 * Utility class for working with {@link TaskLabelsPart} failures.
 *
 * @author Illia Shepilov
 */
public class TaskLabelsPartFailures {

    private TaskLabelsPartFailures() {
    }

    /**
     * Constructs and throws the {@link CannotAssignLabelToTask} failure according to the passed parameters.
     *
     * @param taskId  the ID of the task
     * @param labelId the ID of the label
     * @throws CannotAssignLabelToTask the failure to throw
     */
    public static void throwCannotAssignLabelToTaskFailure(TaskId taskId, LabelId labelId)
            throws CannotAssignLabelToTask {
        final FailedTaskCommandDetails commandFailed =
                FailedTaskCommandDetails.newBuilder()
                                        .setTaskId(taskId)
                                        .build();
        final AssignLabelToTaskFailed assignLabelToTaskFailed =
                AssignLabelToTaskFailed.newBuilder()
                                       .setFailureDetails(commandFailed)
                                       .setLabelId(labelId)
                                       .build();
        throw new CannotAssignLabelToTask(assignLabelToTaskFailed);
    }

    /**
     * Constructs and throws the {@link CannotRemoveLabelFromTask} failure according to the passed parameters.
     *
     * @param taskId the ID of the task
     * @throws CannotRemoveLabelFromTask the failure to throw
     */
    public static void throwCannotRemoveLabelFromTaskFailure(LabelId labelId, TaskId taskId)
            throws CannotRemoveLabelFromTask {
        final FailedTaskCommandDetails commandFailed =
                FailedTaskCommandDetails.newBuilder()
                                        .setTaskId(taskId)
                                        .build();
        final RemoveLabelFromTaskFailed removeLabelFromTaskFailed =
                RemoveLabelFromTaskFailed.newBuilder()
                                         .setLabelId(labelId)
                                         .setFailureDetails(commandFailed)
                                         .build();
        throw new CannotRemoveLabelFromTask(removeLabelFromTaskFailed);
    }
}
