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

import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.base.CommandContext;
import org.spine3.examples.todolist.TaskDefinition;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskStatus;
import org.spine3.examples.todolist.c.aggregate.TaskDefinitionPart;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.events.TaskCreated;
import org.spine3.examples.todolist.c.failures.CannotCreateTaskWithInappropriateDescription;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.createTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("CreateBasicTask command should be interpreted by TaskDefinitionPart and")
public class CreateBasicTaskTest extends TaskDefinitionCommandTest<CreateBasicTask> {

    private final CommandContext commandContext = createCommandContext();
    private TaskDefinitionPart aggregate;
    private TaskId taskId;

    @Override
    @BeforeEach
    public void setUp() {
        taskId = createTaskId();
        aggregate = createTaskDefinitionPart(taskId);
    }

    @Test
    @DisplayName("produce TaskCreated event")
    public void produceEvent() {
        final CreateBasicTask createTaskCmd = createTaskInstance(taskId, DESCRIPTION);
        final List<? extends Message> messageList =
                aggregate.dispatchForTest(createTaskCmd, commandContext);

        assertNotNull(aggregate.getState()
                               .getCreated());
        assertNotNull(aggregate.getId());
        assertEquals(1, messageList.size());
        assertEquals(TaskCreated.class, messageList.get(0)
                                                   .getClass());
        final TaskCreated taskCreated = (TaskCreated) messageList.get(0);

        assertEquals(taskId, taskCreated.getId());
        assertEquals(DESCRIPTION, taskCreated.getDetails()
                                             .getDescription());
    }

    @Test
    @DisplayName("create the task")
    public void createTask() {
        final CreateBasicTask createBasicTask = createTaskInstance();
        aggregate.dispatchForTest(createBasicTask, commandContext);

        final TaskDefinition state = aggregate.getState();
        assertEquals(state.getId(), createBasicTask.getId());
        assertEquals(state.getTaskStatus(), TaskStatus.FINALIZED);
    }

    @Test
    @DisplayName("throw CannotCreateTaskWithInappropriateDescription failure " +
            "upon an attempt to create task with too short description")
    public void notCreateTask() {
        final CreateBasicTask createBasicTask = createTaskInstance(taskId, "D");
        try {
            aggregate.dispatchForTest(createBasicTask, commandContext);
        } catch (IllegalStateException ex) {
            final CannotCreateTaskWithInappropriateDescription failure =
                    (CannotCreateTaskWithInappropriateDescription) ex.getCause();
            final TaskId actualId = failure.getFailure()
                                           .getCreateTaskFailed()
                                           .getFailureDetails()
                                           .getTaskId();
            assertEquals(this.taskId, actualId);
        }
    }
}
