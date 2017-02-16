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

import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.CompleteTaskFailed;
import org.spine3.examples.todolist.CreateBasicTaskFailed;
import org.spine3.examples.todolist.CreateDraftFailed;
import org.spine3.examples.todolist.DeleteTaskFailed;
import org.spine3.examples.todolist.DescriptionUpdateFailed;
import org.spine3.examples.todolist.FailedTaskCommandDetails;
import org.spine3.examples.todolist.FinalizeDraftFailed;
import org.spine3.examples.todolist.PriorityUpdateFailed;
import org.spine3.examples.todolist.ReopenTaskFailed;
import org.spine3.examples.todolist.RestoreDeletedTaskFailed;
import org.spine3.examples.todolist.TaskDueDateUpdateFailed;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.aggregate.TaskDefinitionPart;
import org.spine3.examples.todolist.c.failures.CannotCompleteTask;
import org.spine3.examples.todolist.c.failures.CannotCreateDraft;
import org.spine3.examples.todolist.c.failures.CannotCreateTaskWithInappropriateDescription;
import org.spine3.examples.todolist.c.failures.CannotDeleteTask;
import org.spine3.examples.todolist.c.failures.CannotFinalizeDraft;
import org.spine3.examples.todolist.c.failures.CannotReopenTask;
import org.spine3.examples.todolist.c.failures.CannotRestoreDeletedTask;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskDescription;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskDueDate;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskPriority;
import org.spine3.examples.todolist.c.failures.CannotUpdateTaskWithInappropriateDescription;

/**
 * Utility class for working with {@link TaskDefinitionPart} failures.
 *
 * @author Illia Shepilov
 */
public class TaskDefinitionPartFailures {

    private TaskDefinitionPartFailures() {
    }

    public static class UpdateFailures {

        private UpdateFailures() {
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDueDate} failure according to the passed parameters.
         *
         * @param taskId the ID of the task
         * @throws CannotUpdateTaskDueDate the failure to throw
         */
        public static void throwCannotUpdateTaskDueDateFailure(TaskId taskId) throws CannotUpdateTaskDueDate {
            final FailedTaskCommandDetails commandFailed =
                    FailedTaskCommandDetails.newBuilder()
                                            .setTaskId(taskId)
                                            .build();
            final TaskDueDateUpdateFailed dueDateUpdateFailed =
                    TaskDueDateUpdateFailed.newBuilder()
                                           .setFailureDetails(commandFailed)
                                           .build();
            throw new CannotUpdateTaskDueDate(dueDateUpdateFailed);
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDescription} failure according to the passed parameters.
         *
         * @param taskId   the ID of the task
         * @param mismatch the {@link ValueMismatch}
         * @throws CannotUpdateTaskDescription the failure to throw
         */
        public static void throwCannotUpdateTaskDescriptionFailure(TaskId taskId, ValueMismatch mismatch)
                throws CannotUpdateTaskDescription {
            final FailedTaskCommandDetails commandFailed = FailedTaskCommandDetails.newBuilder()
                                                                                   .setTaskId(taskId)
                                                                                   .build();
            final DescriptionUpdateFailed descriptionUpdateFailed =
                    DescriptionUpdateFailed.newBuilder()
                                           .setFailureDetails(commandFailed)
                                           .setDescriptionMismatch(mismatch)
                                           .build();
            throw new CannotUpdateTaskDescription(descriptionUpdateFailed);
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDescription} failure according to the passed parameters.
         *
         * @param taskId the ID of the task
         * @throws CannotUpdateTaskDescription the failure to throw
         */
        public static void throwCannotUpdateTaskDescriptionFailure(TaskId taskId)
                throws CannotUpdateTaskDescription {
            final FailedTaskCommandDetails commandFailed = FailedTaskCommandDetails.newBuilder()
                                                                                   .setTaskId(taskId)
                                                                                   .build();
            final DescriptionUpdateFailed descriptionUpdateFailed =
                    DescriptionUpdateFailed.newBuilder()
                                           .setFailureDetails(commandFailed)
                                           .build();
            throw new CannotUpdateTaskDescription(descriptionUpdateFailed);
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDueDate} failure according to the passed parameters.
         *
         * @param taskId   the ID of the task
         * @param mismatch the {@link ValueMismatch}
         * @throws CannotUpdateTaskDueDate the failure to throw
         */
        public static void throwCannotUpdateTaskDueDateFailure(TaskId taskId, ValueMismatch mismatch)
                throws CannotUpdateTaskDueDate {
            final FailedTaskCommandDetails commandFailed = FailedTaskCommandDetails.newBuilder()
                                                                                   .setTaskId(taskId)
                                                                                   .build();
            final TaskDueDateUpdateFailed dueDateUpdateFailed =
                    TaskDueDateUpdateFailed.newBuilder()
                                           .setFailureDetails(commandFailed)
                                           .setDueDateMismatch(mismatch)
                                           .build();
            throw new CannotUpdateTaskDueDate(dueDateUpdateFailed);
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskDescription} failure according to the passed parameters.
         *
         * @param taskId   the ID of the task
         * @param mismatch the {@link ValueMismatch}
         * @throws CannotUpdateTaskDescription the failure to throw
         */
        public static void throwCannotUpdateDescriptionFailure(TaskId taskId, ValueMismatch mismatch)
                throws CannotUpdateTaskDescription {
            final FailedTaskCommandDetails commandFailed = FailedTaskCommandDetails.newBuilder()
                                                                                   .setTaskId(taskId)
                                                                                   .build();
            final DescriptionUpdateFailed descriptionUpdateFailed =
                    DescriptionUpdateFailed.newBuilder()
                                           .setFailureDetails(commandFailed)
                                           .setDescriptionMismatch(mismatch)
                                           .build();
            throw new CannotUpdateTaskDescription(descriptionUpdateFailed);
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskPriority} failure according to the passed parameters.
         *
         * @param taskId   the ID of the task
         * @param mismatch the {@link ValueMismatch}
         * @throws CannotUpdateTaskPriority the failure to throw
         */
        public static void throwCannotUpdateTaskPriorityFailure(TaskId taskId, ValueMismatch mismatch)
                throws CannotUpdateTaskPriority {
            final FailedTaskCommandDetails commandFailed = FailedTaskCommandDetails.newBuilder()
                                                                                   .setTaskId(taskId)
                                                                                   .build();
            final PriorityUpdateFailed priorityUpdateFailed = PriorityUpdateFailed.newBuilder()
                                                                                  .setFailureDetails(commandFailed)
                                                                                  .setPriorityMismatch(mismatch)
                                                                                  .build();
            throw new CannotUpdateTaskPriority(priorityUpdateFailed);
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskPriority} failure according to the passed parameters.
         *
         * @param taskId the ID of the task
         * @throws CannotUpdateTaskPriority the failure to throw
         */
        public static void throwCannotUpdateTaskPriorityFailure(TaskId taskId) throws CannotUpdateTaskPriority {
            final FailedTaskCommandDetails commandFailed =
                    FailedTaskCommandDetails.newBuilder()
                                            .setTaskId(taskId)
                                            .build();
            final PriorityUpdateFailed priorityUpdateFailed = PriorityUpdateFailed.newBuilder()
                                                                                  .setFailureDetails(commandFailed)
                                                                                  .build();
            throw new CannotUpdateTaskPriority(priorityUpdateFailed);
        }

        /**
         * Constructs and throws the {@link CannotUpdateTaskWithInappropriateDescription} failure
         * according to the passed parameters.
         *
         * @param taskId the ID of the task
         * @throws CannotUpdateTaskWithInappropriateDescription the failure to throw
         */
        public static void throwCannotUpdateTooShortDescriptionFailure(TaskId taskId)
                throws CannotUpdateTaskWithInappropriateDescription {
            final FailedTaskCommandDetails commandFailed =
                    FailedTaskCommandDetails.newBuilder()
                                            .setTaskId(taskId)
                                            .build();
            final DescriptionUpdateFailed updateFailed = DescriptionUpdateFailed.newBuilder()
                                                                                .setFailureDetails(commandFailed)
                                                                                .build();
            throw new CannotUpdateTaskWithInappropriateDescription(updateFailed);
        }
    }

    public static class TaskCreationFailures {

        private TaskCreationFailures() {
        }

        /**
         * Constructs and throws the {@link CannotCreateDraft} failure according to the passed parameters.
         *
         * @param taskId the ID of the task
         * @throws CannotCreateDraft the failure to throw
         */
        public static void throwCannotCreateDraftFailure(TaskId taskId) throws CannotCreateDraft {
            final FailedTaskCommandDetails commandFailed =
                    FailedTaskCommandDetails.newBuilder()
                                            .setTaskId(taskId)
                                            .build();
            final CreateDraftFailed createDraftFailed = CreateDraftFailed.newBuilder()
                                                                         .setFailureDetails(commandFailed)
                                                                         .build();
            throw new CannotCreateDraft(createDraftFailed);
        }

        /**
         * Constructs and throws the {@link CannotCreateTaskWithInappropriateDescription} failure
         * according to the passed parameters.
         *
         * @param taskId the ID of the task
         * @throws CannotCreateTaskWithInappropriateDescription the failure to throw
         */
        public static void throwCannotCreateTaskWithInappropriateDescriptionFailure(TaskId taskId)
                throws CannotCreateTaskWithInappropriateDescription {
            final FailedTaskCommandDetails commandFailed =
                    FailedTaskCommandDetails.newBuilder()
                                            .setTaskId(taskId)
                                            .build();
            final CreateBasicTaskFailed createBasicTaskFailed = CreateBasicTaskFailed.newBuilder()
                                                                                     .setFailureDetails(commandFailed)
                                                                                     .build();
            throw new CannotCreateTaskWithInappropriateDescription(createBasicTaskFailed);
        }
    }

    public static class ChangeStatusFailures {

        private ChangeStatusFailures() {
        }

        /**
         * Constructs and throws the {@link CannotReopenTask} failure according to the passed parameters.
         *
         * @param taskId the ID of the task
         * @throws CannotReopenTask the failure to throw
         */
        public static void throwCannotReopenTaskFailure(TaskId taskId) throws CannotReopenTask {
            final FailedTaskCommandDetails commandFailed = FailedTaskCommandDetails.newBuilder()
                                                                                   .setTaskId(taskId)
                                                                                   .build();
            final ReopenTaskFailed reopenTaskFailed = ReopenTaskFailed.newBuilder()
                                                                      .setFailureDetails(commandFailed)
                                                                      .build();
            throw new CannotReopenTask(reopenTaskFailed);
        }

        /**
         * Constructs and throws the {@link CannotFinalizeDraft} failure according to the passed parameters.
         *
         * @param taskId the ID of the task
         * @throws CannotFinalizeDraft the failure to throw
         */
        public static void throwCannotFinalizeDraftFailure(TaskId taskId) throws CannotFinalizeDraft {
            final FailedTaskCommandDetails commandFailed = FailedTaskCommandDetails.newBuilder()
                                                                                   .setTaskId(taskId)
                                                                                   .build();
            final FinalizeDraftFailed finalizeDraftFailed = FinalizeDraftFailed.newBuilder()
                                                                               .setFailureDetails(commandFailed)
                                                                               .build();
            throw new CannotFinalizeDraft(finalizeDraftFailed);
        }

        /**
         * Constructs and throws the {@link CannotDeleteTask} failure according to the passed parameters.
         *
         * @param taskId the ID of the task
         * @throws CannotDeleteTask the failure to throw
         */
        public static void throwCannotDeleteTaskFailure(TaskId taskId) throws CannotDeleteTask {
            final FailedTaskCommandDetails commandFailed = FailedTaskCommandDetails.newBuilder()
                                                                                   .setTaskId(taskId)
                                                                                   .build();
            final DeleteTaskFailed deleteTaskFailed = DeleteTaskFailed.newBuilder()
                                                                      .setFailureDetails(commandFailed)
                                                                      .build();
            throw new CannotDeleteTask(deleteTaskFailed);
        }

        /**
         * Constructs and throws the {@link CannotCompleteTask} failure according to the passed parameters.
         *
         * @param taskId the ID of the task
         * @throws CannotCompleteTask the failure to throw
         */
        public static void throwCannotCompleteTaskFailure(TaskId taskId) throws CannotCompleteTask {
            final FailedTaskCommandDetails commandFailed = FailedTaskCommandDetails.newBuilder()
                                                                                   .setTaskId(taskId)
                                                                                   .build();
            final CompleteTaskFailed completeTaskFailed = CompleteTaskFailed.newBuilder()
                                                                            .setFailureDetails(commandFailed)
                                                                            .build();
            throw new CannotCompleteTask(completeTaskFailed);
        }

        /**
         * Constructs and throws the {@link CannotRestoreDeletedTask} failure according to the passed parameters.
         *
         * @param taskId the ID of the task
         * @throws CannotRestoreDeletedTask the {@code CannotRestoreDeletedTask} failure
         */
        public static void throwCannotRestoreDeletedTaskFailure(TaskId taskId) throws CannotRestoreDeletedTask {
            final FailedTaskCommandDetails commandFailed = FailedTaskCommandDetails.newBuilder()
                                                                                   .setTaskId(taskId)
                                                                                   .build();
            final RestoreDeletedTaskFailed restoreDeletedTaskFailed =
                    RestoreDeletedTaskFailed.newBuilder()
                                            .setFailureDetails(commandFailed)
                                            .build();
            throw new CannotRestoreDeletedTask(restoreDeletedTaskFailed);
        }
    }
}
