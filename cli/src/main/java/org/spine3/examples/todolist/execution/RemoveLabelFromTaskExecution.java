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

package org.spine3.examples.todolist.execution;

import org.spine3.examples.todolist.RemoveLabelFromTask;
import org.spine3.examples.todolist.Parameters;
import org.spine3.examples.todolist.client.TodoClient;

/**
 * @author Illia Shepilov
 */
public class RemoveLabelFromTaskExecution implements Executable {

    private static final String LABEL_REMOVED_FROM_TASK_MESSAGE = "Label removed from task";
    private final TodoClient client;

    public RemoveLabelFromTaskExecution(TodoClient client) {
        this.client = client;
    }

    @Override
    public String execute(Parameters params) {
        final RemoveLabelFromTask removeLabelFromTask = RemoveLabelFromTask.newBuilder()
                                                                           .setLabelId(params.getLabelId())
                                                                           .setId(params.getTaskId())
                                                                           .build();
        client.removeLabel(removeLabelFromTask);
        return LABEL_REMOVED_FROM_TASK_MESSAGE;
    }
}
