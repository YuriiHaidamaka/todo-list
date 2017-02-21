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

package org.spine3.examples.todolist.c.aggregate;

import com.google.common.base.Throwables;
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.spine3.base.Command;
import org.spine3.base.CommandContext;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.c.events.LabelAssignedToTask;
import org.spine3.examples.todolist.c.events.LabelRemovedFromTask;
import org.spine3.examples.todolist.c.failures.CannotAssignLabelToTask;
import org.spine3.examples.todolist.c.failures.CannotRemoveLabelFromTask;
import org.spine3.examples.todolist.context.TodoListBoundedContext;
import org.spine3.examples.todolist.testdata.TestResponseObserver;
import org.spine3.server.BoundedContext;
import org.spine3.server.command.CommandBus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Commands.createCommand;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestCommandContextFactory.createCommandContext;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.completeTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelsCommandFactory.removeLabelFromTaskInstance;

/**
 * @author Illia Shepilov
 */
public class TaskLabelsPartTest {

    private static final CommandContext COMMAND_CONTEXT = createCommandContext();

    private TestResponseObserver responseObserver;
    private CommandBus commandBus;
    private TaskLabelsPart taskLabelsPart;
    private TaskId taskId;
    private LabelId labelId;

    @BeforeEach
    public void setUp() {
        final BoundedContext boundedContext = TodoListBoundedContext.createTestInstance();
        TaskAggregateRoot.injectBoundedContext(boundedContext);
        commandBus = boundedContext.getCommandBus();
        responseObserver = new TestResponseObserver();
        taskId = createTaskId();
        labelId = createLabelId();
        taskLabelsPart = createTaskLabelsPart(taskId);
    }

    private static LabelId createLabelId() {
        final LabelId result = LabelId.newBuilder()
                                      .setValue(newUuid())
                                      .build();
        return result;
    }

    private static TaskId createTaskId() {
        final TaskId result = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        return result;
    }

    private static TaskLabelsPart createTaskLabelsPart(TaskId taskId) {
        return new TaskLabelsPart(taskId);
    }

    @Nested
    @DisplayName("AssignLabelToTask command should be interpreted by TaskLabelsPart and")
    class AssignLabelToTaskCommand {

        @Test
        @DisplayName("produce LabelAssignedToTask event")
        public void produceEvent() {
            final AssignLabelToTask assignLabelToTaskCmd = assignLabelToTaskInstance(taskId, labelId);
            final List<? extends Message> messageList =
                    taskLabelsPart.dispatchForTest(assignLabelToTaskCmd, COMMAND_CONTEXT);

            assertEquals(1, messageList.size());
            assertEquals(LabelAssignedToTask.class, messageList.get(0)
                                                               .getClass());
            final LabelAssignedToTask labelAssignedToTask = (LabelAssignedToTask) messageList.get(0);

            assertEquals(taskId, labelAssignedToTask.getTaskId());
            assertEquals(labelId, labelAssignedToTask.getLabelId());
        }

        @Test
        @DisplayName("throw CannotAssignLabelToTask failure upon an attempt to assign the label to the deleted task")
        public void cannotAssignLabelToDeletedTask() {
            try {
                final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
                final Command createTaskCmd = createCommand(createTask, COMMAND_CONTEXT);
                commandBus.post(createTaskCmd, responseObserver);

                final DeleteTask deleteTask = deleteTaskInstance(taskId);
                final Command deleteTaskCmd = createCommand(deleteTask, COMMAND_CONTEXT);
                commandBus.post(deleteTaskCmd, responseObserver);

                final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
                final Command assignLabelToTaskCmd = createCommand(assignLabelToTask, COMMAND_CONTEXT);
                commandBus.post(assignLabelToTaskCmd, responseObserver);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotAssignLabelToTask);
            }
        }

        @Test
        @DisplayName("throw CannotAssignLabelToTask failure " +
                "upon an attempt to assign the label to the completed task")
        public void cannotAssignLabelToCompletedTask() {
            try {
                final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
                final Command createTaskCmd = createCommand(createTask, COMMAND_CONTEXT);
                commandBus.post(createTaskCmd, responseObserver);

                final CompleteTask completeTask = completeTaskInstance(taskId);
                final Command completeTaskCmd = createCommand(completeTask, COMMAND_CONTEXT);
                commandBus.post(completeTaskCmd, responseObserver);

                final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
                final Command assignLabelToTaskCmd = createCommand(assignLabelToTask, COMMAND_CONTEXT);
                commandBus.post(assignLabelToTaskCmd, responseObserver);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotAssignLabelToTask);
            }
        }
    }

    @Nested
    @DisplayName("RemoveLabelFromTask command should be interpreted by TaskLabelsPart and")
    class RemoveLabelFromTaskCommand {

        @Test
        @DisplayName("produce LabelRemovedFromTask event")
        public void produceEvent() {
            final RemoveLabelFromTask removeLabelFromTaskCmd = removeLabelFromTaskInstance(taskId, labelId);
            final List<? extends Message> messageList =
                    taskLabelsPart.dispatchForTest(removeLabelFromTaskCmd, COMMAND_CONTEXT);

            assertEquals(1, messageList.size());
            assertEquals(LabelRemovedFromTask.class, messageList.get(0)
                                                                .getClass());
            final LabelRemovedFromTask labelRemovedFromTask = (LabelRemovedFromTask) messageList.get(0);

            assertEquals(taskId, labelRemovedFromTask.getTaskId());
            assertEquals(labelId, labelRemovedFromTask.getLabelId());
        }

        @Test
        @DisplayName("throw CannotRemoveLabelFromTask failure " +
                "upon an attempt to remove the label from the completed task")
        public void cannotRemoveLabelFromCompletedTask() {
            try {
                final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
                final Command createTaskCmd = createCommand(createTask, COMMAND_CONTEXT);
                commandBus.post(createTaskCmd, responseObserver);

                final CompleteTask completeTask = completeTaskInstance(taskId);
                final Command completeTaskCmd = createCommand(completeTask, COMMAND_CONTEXT);
                commandBus.post(completeTaskCmd, responseObserver);

                final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(taskId, labelId);
                final Command removeLabelFromTaskCmd = createCommand(removeLabelFromTask, COMMAND_CONTEXT);
                commandBus.post(removeLabelFromTaskCmd, responseObserver);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotRemoveLabelFromTask);
            }
        }

        @Test
        @DisplayName("throw CannotRemoveLabelFromTask failure " +
                "upon an attempt to remove the label from the deleted task")
        public void cannotRemoveLabelFromDeletedTask() {
            try {
                final CreateBasicTask createTask = createTaskInstance(taskId, DESCRIPTION);
                final Command createTaskCmd = createCommand(createTask, COMMAND_CONTEXT);
                commandBus.post(createTaskCmd, responseObserver);

                final DeleteTask deleteTask = deleteTaskInstance(taskId);
                final Command deleteTaskCmd = createCommand(deleteTask, COMMAND_CONTEXT);
                commandBus.post(deleteTaskCmd, responseObserver);

                final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(taskId, labelId);
                final Command removeLabelFromTaskCmd = createCommand(removeLabelFromTask, COMMAND_CONTEXT);
                commandBus.post(removeLabelFromTaskCmd, responseObserver);
            } catch (Throwable e) {
                @SuppressWarnings("ThrowableResultOfMethodCallIgnored") // Need it for checking.
                final Throwable cause = Throwables.getRootCause(e);
                assertTrue(cause instanceof CannotRemoveLabelFromTask);
            }
        }
    }
}
