/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

import org.spine3.examples.todolist.Settings;

/**
 * @author Illia Shepilov
 */
public class HelpExecution implements Executable {

    private static final String HELP =
            "create-task [--description]                          creates the task with the specified description.\n" +
            "create-label [--title]                               creates the label with the specified title.\n" +
            "create-draft [--title]                               creates the task draft.\n" +
            "update-due-date [--task-id] [--due-date]             updates the task due date.\n" +
            "update-priority [--task-id] [--priority]             updates the task priority.\n" +
            "update-description [--task-id][--description]        updates the task description.\n" +
            "update-label-details [--label-id][--title][--color]  updates the label details.\n" +
            "complete-task [--task-id]                            completes the task.\n" +
            "reopen-task [--task-id]                              reopens the task.\n" +
            "delete-task [--task-id]                              deletes the task.\n" +
            "restore-task [--task-id]                             restores deleted the task.\n" +
            "finalize-draft [--task-id]                           finalizes the draft.\n" +
            "assign-label [--task-id][--label-id]                 assign the label to the task.\n" +
            "remove-label [--task-id][--label-id]                 removes the label from the task.\n" +
            "my-list-view                                         returns the my list view.\n" +
            "draft-tasks-view                                     returns the draft tasks view.\n" +
            "labelled-tasks-view                                  returns the labelled tasks view.\n";

    @Override
    public String execute(Settings params) {
        return HELP;
    }
}
