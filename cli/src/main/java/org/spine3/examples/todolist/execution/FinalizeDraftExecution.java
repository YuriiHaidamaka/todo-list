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

import org.spine3.examples.todolist.FinalizeDraft;
import org.spine3.examples.todolist.Parameters;
import org.spine3.examples.todolist.client.TodoClient;

/**
 * @author Illia Shepilov
 */
public class FinalizeDraftExecution implements Executable {

    private static final String TASK_DRAFT_FINALIZED_MESSAGE = "Task draft finalized";
    private final TodoClient client;

    public FinalizeDraftExecution(TodoClient client) {
        this.client = client;
    }

    @Override
    public String execute(Parameters params) {
        final FinalizeDraft finalizeDraft = FinalizeDraft.newBuilder()
                                                         .setId(params.getTaskId())
                                                         .build();
        client.finalize(finalizeDraft);
        return TASK_DRAFT_FINALIZED_MESSAGE;
    }
}
