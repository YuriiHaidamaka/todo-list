/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.examples.todolist.c.aggregate;

import io.spine.examples.todolist.TaskStatus;

/**
 * Validates task commands and state transitions.
 *
 * @author Illia Shepilov
 */
class TaskFlowValidator {

    private TaskFlowValidator() {
    }

    /**
     * Check whether the transition from the current task status to the new status is allowed.
     *
     * @param currentStatus current task status
     * @param newStatus     new task status
     */
    static boolean isValidTransition(TaskStatus currentStatus, TaskStatus newStatus) {
        return TaskStatusTransition.isValid(currentStatus, newStatus);
    }

    static boolean isValidUpdateTaskPriorityCommand(TaskStatus currentStatus) {
        return ensureNeitherCompletedNorDeleted(currentStatus);
    }

    static boolean isValidUpdateTaskDueDateCommand(TaskStatus currentStatus) {
        return ensureNeitherCompletedNorDeleted(currentStatus);
    }

    static boolean isValidTaskStatusToRemoveLabel(TaskStatus currentStatus) {
        return ensureNeitherCompletedNorDeleted(currentStatus);
    }

    static boolean isValidAssignLabelToTaskCommand(TaskStatus currentStatus) {
        return ensureNeitherCompletedNorDeleted(currentStatus);
    }

    static boolean isValidCreateDraftCommand(TaskStatus currentStatus) {
        return ensureNeitherCompletedNorDeleted(currentStatus);
    }

    static boolean ensureDeleted(TaskStatus currentStatus) {
        return currentStatus == TaskStatus.DELETED;
    }

    static boolean ensureCompleted(TaskStatus currentStatus) {
        return currentStatus == TaskStatus.COMPLETED;
    }

    /**
     * Verifies that the specified {@link TaskStatus} is neither completed nor deleted.
     *
     * @param currentStatus task current state {@link TaskStatus}
     * @return {@code true} if the status is neither completed nor deleted
     */
    static boolean ensureNeitherCompletedNorDeleted(TaskStatus currentStatus) {
        boolean isDeleted = ensureDeleted(currentStatus);
        boolean isCompleted = ensureCompleted(currentStatus);
        return !isDeleted && !isCompleted;
    }
}
