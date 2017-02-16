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

package org.spine3.examples.todolist.c.aggregate.definition;

import com.google.common.base.Throwables;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.base.CommandContext;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskDueDateUpdateFailed;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.aggregate.TaskDefinitionPart;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.events.TaskDueDateUpdated;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskDueDate;
import org.spine3.examples.todolist.c.failures.Failures;
import org.spine3.protobuf.Timestamps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DUE_DATE;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static org.spine3.protobuf.AnyPacker.unpack;

/**
 * @author Illia Shepilov
 */
@DisplayName("UpdateTaskDueDate command should be interpreted by TaskDefinitionPart and")
public class UpdateTaskDueDateTest extends TaskDefinitionCommandTest<UpdateTaskDueDate> {

    private TaskDefinitionPart aggregate;
    private final CommandContext commandContext = createCommandContext();
    private TaskId taskId;

    @Override
    @BeforeEach
    protected void setUp() {
        taskId = createTaskId();
        aggregate = createTaskDefinitionPart(taskId);
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDueDate failure " +
            "when it is trying to update the due date for the completed task")
    public void cannotUpdateCompletedTaskDueDate() {
        dispatchCreateTaskCmd();

        final CompleteTask completeTaskCmd = completeTaskInstance(taskId);
        aggregate.dispatchForTest(completeTaskCmd, commandContext);

        try {
            final UpdateTaskDueDate updateTaskDueDateCmd = updateTaskDueDateInstance(taskId);
            aggregate.dispatchForTest(updateTaskDueDateCmd, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotUpdateTaskDueDate);
        }
    }

    @Test
    @DisplayName("throw CannotUpdateTaskDueDate failure when it is trying to update the due date for the deleted task")
    public void cannotUpdateDeletedTaskDueDate() {
        dispatchCreateTaskCmd();

        final DeleteTask deleteTaskCmd = deleteTaskInstance(taskId);
        aggregate.dispatchForTest(deleteTaskCmd, commandContext);

        try {
            final UpdateTaskDueDate updateTaskDueDateCmd = updateTaskDueDateInstance(taskId);
            aggregate.dispatchForTest(updateTaskDueDateCmd, commandContext);
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotUpdateTaskDueDate);
        }
    }

    @Test
    @DisplayName("produce TaskDueDateUpdated event")
    public void produceEvent() {
        final UpdateTaskDueDate updateTaskDueDateCmd = updateTaskDueDateInstance(taskId);
        final List<? extends Message> messageList =
                aggregate.dispatchForTest(updateTaskDueDateCmd, commandContext);

        assertEquals(1, messageList.size());
        assertEquals(TaskDueDateUpdated.class, messageList.get(0)
                                                          .getClass());
        final TaskDueDateUpdated taskDueDateUpdated = (TaskDueDateUpdated) messageList.get(0);

        assertEquals(taskId, taskDueDateUpdated.getTaskId());
        final Timestamp newDueDate = taskDueDateUpdated.getDueDateChange()
                                                       .getNewValue();
        assertEquals(DUE_DATE, newDueDate);
    }

    @Test
    @DisplayName("update the task due date")
    public void updateDueDate() {
        final Timestamp updatedDueDate = Timestamps.getCurrentTime();
        dispatchCreateTaskCmd();

        final UpdateTaskDueDate updateTaskDueDateCmd =
                updateTaskDueDateInstance(taskId, Timestamp.getDefaultInstance(), updatedDueDate);
        aggregate.dispatchForTest(updateTaskDueDateCmd, commandContext);
        final TaskDefinition state = aggregate.getState();

        assertEquals(taskId, state.getId());
        assertEquals(updatedDueDate, state.getDueDate());
    }

    @Test
    @DisplayName("produce CannotUpdateTaskDueDate failure")
    public void produceFailure() {
        dispatchCreateTaskCmd();

        final Timestamp expectedDueDate = Timestamps.getCurrentTime();
        final Timestamp newDueDate = Timestamps.getCurrentTime();

        try {
            final UpdateTaskDueDate updateTaskDueDate =
                    updateTaskDueDateInstance(taskId, expectedDueDate, newDueDate);
            final List<? extends com.google.protobuf.Message> messageList =
                    aggregate.dispatchForTest(updateTaskDueDate, commandContext);

            assertEquals(1, messageList.size());
        } catch (Throwable e) {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
            final Throwable cause = Throwables.getRootCause(e);
            assertTrue(cause instanceof CannotUpdateTaskDueDate);

            @SuppressWarnings("ConstantConditions")
            final Failures.CannotUpdateTaskDueDate cannotUpdateTaskDueDate =
                    ((CannotUpdateTaskDueDate) cause).getFailure();

            final TaskDueDateUpdateFailed dueDateUpdateFailed = cannotUpdateTaskDueDate.getUpdateFailed();
            final TaskId actualTaskId = dueDateUpdateFailed.getFailureDetails()
                                                           .getTaskId();
            assertEquals(taskId, actualTaskId);

            final ValueMismatch mismatch = dueDateUpdateFailed.getDueDateMismatch();

            assertEquals(newDueDate, unpack(mismatch.getNewValue()));
            assertEquals(expectedDueDate, unpack(mismatch.getExpected()));

            final Timestamp actualDueDate = Timestamp.getDefaultInstance();
            assertEquals(actualDueDate, unpack(mismatch.getActual()));
        }
    }

    private void dispatchCreateTaskCmd() {
        final CreateBasicTask createTaskCmd = createTaskInstance(taskId, DESCRIPTION);
        aggregate.dispatchForTest(createTaskCmd, commandContext);
    }
}
