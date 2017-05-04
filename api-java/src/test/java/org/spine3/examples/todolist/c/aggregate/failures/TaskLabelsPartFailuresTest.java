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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.base.CommandContext;
import org.spine3.examples.todolist.FailedTaskCommandDetails;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.c.failures.CannotAssignLabelToTask;
import org.spine3.examples.todolist.c.failures.CannotRemoveLabelFromTask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.spine3.examples.todolist.c.aggregate.failures.TaskLabelsPartFailures.throwCannotAssignLabelToTaskFailure;
import static org.spine3.examples.todolist.c.aggregate.failures.TaskLabelsPartFailures.throwCannotRemoveLabelFromTaskFailure;
import static org.spine3.test.Tests.assertHasPrivateParameterlessCtor;

/**
 * @author Illia Shepilov
 */
@DisplayName("TaskLabelsPartFailures should")
class TaskLabelsPartFailuresTest {

    private final TaskId taskId = TaskId.getDefaultInstance();
    private final LabelId labelId = LabelId.getDefaultInstance();

    @Test
    @DisplayName("have the private constructor")
    void havePrivateConstructor() {
        assertHasPrivateParameterlessCtor(TaskLabelsPartFailures.class);
    }

    @Test
    @DisplayName("throw CannotRemoveLabelFromTask failure")
    void throwCannotRemoveLabelFromTask() {
        final RemoveLabelFromTask cmd = RemoveLabelFromTask.newBuilder()
                                                           .setId(taskId)
                                                           .setLabelId(labelId)
                                                           .build();
        final CommandContext ctx = CommandContext.getDefaultInstance();

        final CannotRemoveLabelFromTask failure =
                assertThrows(CannotRemoveLabelFromTask.class,
                             () -> throwCannotRemoveLabelFromTaskFailure(cmd, ctx));
        final TaskId actual = failure.getFailureMessage()
                                     .getRemoveLabelFailed()
                                     .getFailureDetails()
                                     .getTaskId();
        assertEquals(taskId, actual);
    }

    @Test
    @DisplayName("throw CannotAssignLabelToTask failure")
    void throwCannotAssignLabelToTask() {
        final AssignLabelToTask cmd = AssignLabelToTask.newBuilder()
                                                       .setLabelId(labelId)
                                                       .setId(taskId)
                                                       .build();
        final CommandContext ctx = CommandContext.getDefaultInstance();

        final CannotAssignLabelToTask failure =
                assertThrows(CannotAssignLabelToTask.class,
                             () -> throwCannotAssignLabelToTaskFailure(cmd, ctx));
        final FailedTaskCommandDetails failedCommand = failure.getFailureMessage()
                                                              .getAssignLabelFailed()
                                                              .getFailureDetails();
        final TaskId actualId = failedCommand.getTaskId();
        assertEquals(taskId, actualId);
    }
}
