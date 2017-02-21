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
import org.spine3.base.CommandContext;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.aggregate.TaskDefinitionPart;
import org.spine3.examples.todolist.testdata.TestCommandContextFactory;
import org.spine3.test.CommandTest;

import static org.spine3.base.Identifiers.newUuid;

/**
 * The parent class for the {@link TaskDefinitionPart} test classes.
 * Provides the common methods for testing.
 *
 * @author Illia Shepilov
 */
abstract class TaskDefinitionCommandTest<C extends Message> extends CommandTest<C> {

    public static TaskId createTaskId() {
        final TaskId result = TaskId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        return result;
    }

    CommandContext createCommandContext() {
        return TestCommandContextFactory.createCommandContext();
    }

    TaskDefinitionPart createTaskDefinitionPart(TaskId taskId) {
        return new TaskDefinitionPart(taskId);
    }
}
