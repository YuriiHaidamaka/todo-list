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

package org.spine3.examples.todolist;

import org.spine3.examples.todolist.client.CommandLineTodoClient;
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.execution.AssigneLabelToTaskExecution;
import org.spine3.examples.todolist.execution.CompleteTaskExecution;
import org.spine3.examples.todolist.execution.CreateDraftExecution;
import org.spine3.examples.todolist.execution.CreateLabelExecution;
import org.spine3.examples.todolist.execution.CreateTaskExecution;
import org.spine3.examples.todolist.execution.DeleteTaskExecution;
import org.spine3.examples.todolist.execution.Executable;
import org.spine3.examples.todolist.execution.FinalizeDraftExecution;
import org.spine3.examples.todolist.execution.HelpExecution;
import org.spine3.examples.todolist.execution.ObtainDraftTasksViewExecution;
import org.spine3.examples.todolist.execution.ObtainLabelledTasksViewExecution;
import org.spine3.examples.todolist.execution.ObtainMyListViewExecution;
import org.spine3.examples.todolist.execution.RemoveLabelFromTaskExecution;
import org.spine3.examples.todolist.execution.ReopenTaskExecution;
import org.spine3.examples.todolist.execution.RestoreTaskExecution;
import org.spine3.examples.todolist.execution.UpdateLabelDetailsExecution;
import org.spine3.examples.todolist.execution.UpdateTaskDescriptionExecution;
import org.spine3.examples.todolist.execution.UpdateTaskDueDateExecution;
import org.spine3.examples.todolist.execution.UpdateTaskPriorityExecution;

import java.util.HashMap;
import java.util.Map;

import static org.spine3.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;

/**
 * @author Illia Shepilov
 */
public class CommandHolder {

    private static final String HELP_COMMAND = "help";
    private static final String CREATE_TASK_COMMAND = "create-task";
    private static final String CREATE_LABEL_COMMAND = "create-label";
    private static final String CREATE_DRAFT_COMMAND = "create-draft";
    private static final String UPDATE_DESCRIPTION_COMMAND = "update-description";
    private static final String UPDATE_DUE_DATE_COMMAND = "update-due-date";
    private static final String UPDATE_PRIORITY_COMMAND = "update-priority";
    private static final String UPDATE_LABEL_DETAILS_COMMAND = "update-label-details";
    private static final String OBTAIN_MY_LIST_VIEW_COMMAND = "my-list-view";
    private static final String OBTAIN_DRAFT_TASKS_VIEW_COMMAND = "draft-tasks-view";
    private static final String OBTAIN_LABELLED_TASKS_VIEW_COMMAND = "labelled-tasks-view";
    private static final String FINALIZE_DRAFT_COMMAND = "finalize-draft";
    private static final String COMPLETE_TASK_COMMAND = "complete-task";
    private static final String REOPEN_TASK_COMMAND = "reopen-task";
    private static final String DELETE_TASK_COMMAND = "delete-task";
    private static final String RESTORE_TASK_COMMAND = "restore-task";
    private static final String ASSIGN_LABEL_TO_TASK_COMMAND = "assign-label";
    private static final String REMOVE_LABEL_FROM_TASK_COMMAND = "remove-label";

    private final Map<String, Executable> map;
    private final TodoClient client;

    public CommandHolder() {
        map = new HashMap<>();
        client = new CommandLineTodoClient("localhost", DEFAULT_CLIENT_SERVICE_PORT);
        initCommandMap();
    }

    private void initCommandMap() {
        map.put(HELP_COMMAND, new HelpExecution());
        map.put(CREATE_TASK_COMMAND, new CreateTaskExecution(client));
        map.put(CREATE_LABEL_COMMAND, new CreateLabelExecution(client));
        map.put(CREATE_DRAFT_COMMAND, new CreateDraftExecution(client));
        map.put(UPDATE_DESCRIPTION_COMMAND, new UpdateTaskDescriptionExecution(client));
        map.put(UPDATE_DUE_DATE_COMMAND, new UpdateTaskDueDateExecution(client));
        map.put(UPDATE_PRIORITY_COMMAND, new UpdateTaskPriorityExecution(client));
        map.put(UPDATE_LABEL_DETAILS_COMMAND, new UpdateLabelDetailsExecution(client));
        map.put(OBTAIN_MY_LIST_VIEW_COMMAND, new ObtainMyListViewExecution(client));
        map.put(OBTAIN_DRAFT_TASKS_VIEW_COMMAND, new ObtainDraftTasksViewExecution(client));
        map.put(OBTAIN_LABELLED_TASKS_VIEW_COMMAND, new ObtainLabelledTasksViewExecution(client));
        map.put(FINALIZE_DRAFT_COMMAND, new FinalizeDraftExecution(client));
        map.put(COMPLETE_TASK_COMMAND, new CompleteTaskExecution(client));
        map.put(REOPEN_TASK_COMMAND, new ReopenTaskExecution(client));
        map.put(DELETE_TASK_COMMAND, new DeleteTaskExecution(client));
        map.put(RESTORE_TASK_COMMAND, new RestoreTaskExecution(client));
        map.put(ASSIGN_LABEL_TO_TASK_COMMAND, new AssigneLabelToTaskExecution(client));
        map.put(REMOVE_LABEL_FROM_TASK_COMMAND, new RemoveLabelFromTaskExecution(client));
    }

    public Executable get(String key) {
        final Executable result = map.get(key);
        return result;
    }
}
