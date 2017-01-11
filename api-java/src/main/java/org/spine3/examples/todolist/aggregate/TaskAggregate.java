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

package org.spine3.examples.todolist.aggregate;

import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import org.spine3.change.StringChange;
import org.spine3.change.TimestampChange;
import org.spine3.examples.todolist.AssignLabelToTask;
import org.spine3.examples.todolist.CompleteTask;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.CreateDraft;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.DeletedTaskRestored;
import org.spine3.examples.todolist.FinalizeDraft;
import org.spine3.examples.todolist.LabelAssignedToTask;
import org.spine3.examples.todolist.LabelRemovedFromTask;
import org.spine3.examples.todolist.LabelledTaskRestored;
import org.spine3.examples.todolist.PriorityChange;
import org.spine3.examples.todolist.RemoveLabelFromTask;
import org.spine3.examples.todolist.ReopenTask;
import org.spine3.examples.todolist.RestoreDeletedTask;
import org.spine3.examples.todolist.Task;
import org.spine3.examples.todolist.TaskCompleted;
import org.spine3.examples.todolist.TaskCreated;
import org.spine3.examples.todolist.TaskDeleted;
import org.spine3.examples.todolist.TaskDescriptionUpdated;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskDraftCreated;
import org.spine3.examples.todolist.TaskDraftFinalized;
import org.spine3.examples.todolist.TaskDueDateUpdated;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.TaskPriorityUpdated;
import org.spine3.examples.todolist.TaskReopened;
import org.spine3.examples.todolist.TaskStatus;
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;
import org.spine3.protobuf.Timestamps;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.aggregate.Apply;
import org.spine3.server.command.Assign;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static org.spine3.examples.todolist.aggregate.TaskFlowValidator.ensureNeitherCompletedNorDeleted;
import static org.spine3.examples.todolist.aggregate.TaskFlowValidator.validateAssignLabelToTaskCommand;
import static org.spine3.examples.todolist.aggregate.TaskFlowValidator.validateCreateDraftCommand;
import static org.spine3.examples.todolist.aggregate.TaskFlowValidator.validateRemoveLabelFromTaskCommand;
import static org.spine3.examples.todolist.aggregate.TaskFlowValidator.validateTransition;
import static org.spine3.examples.todolist.aggregate.TaskFlowValidator.validateUpdateTaskDueDateCommand;
import static org.spine3.examples.todolist.aggregate.TaskFlowValidator.validateUpdateTaskPriorityCommand;

/**
 * The aggregate managing the state of a {@link Task}.
 *
 * @author Illia Shepilov
 */
// The methods annotated with {@link Assign} are declared {@code private} by design.
// Also, such methods must take a single {@code Event} as a parameter.
// However, it is unused in some cases due to the business rules.
// E.g. {@code TaskRemoved} event is explicit enough by itself,
// so it is not needed to use any of its field values. So the parameter left unused.
@SuppressWarnings("unused")
public class TaskAggregate extends Aggregate<TaskId, Task, Task.Builder> {

    private static final int MIN_DESCRIPTION_LENGTH = 3;
    private static final String SHORT_DESCRIPTION_EXCEPTION_MESSAGE =
            "Description should contain at least 3 alphanumeric symbols.";
    private static final String NULL_DESCRIPTION_EXCEPTION_MESSAGE = "Description cannot be null.";

    /**
     * Creates a new aggregate instance.
     *
     * @param id the ID for the new aggregate.
     * @throws IllegalArgumentException if the ID is not of one of the supported types.
     */
    public TaskAggregate(TaskId id) {
        super(id);
    }

    @Assign
    public List<? extends Message> handle(CreateBasicTask cmd) {
        validateCommand(cmd);
        final TaskDetails.Builder taskDetails = TaskDetails.newBuilder()
                                                           .setDescription(cmd.getDescription());
        final TaskCreated result = TaskCreated.newBuilder()
                                              .setId(cmd.getId())
                                              .setDetails(taskDetails)
                                              .build();
        return Collections.singletonList(result);
    }

    @Assign
    public List<? extends Message> handle(UpdateTaskDescription cmd) {
        validateCommand(cmd);
        final Task state = getState();
        final String previousDescription = state.getDescription();
        final StringChange descriptionChange = StringChange.newBuilder()
                                                           .setPreviousValue(previousDescription)
                                                           .setNewValue(cmd.getUpdatedDescription())
                                                           .build();
        final TaskDescriptionUpdated taskDescriptionUpdated = TaskDescriptionUpdated.newBuilder()
                                                                                    .setId(cmd.getId())
                                                                                    .setDescriptionChange(descriptionChange)
                                                                                    .build();
        final List<TaskDescriptionUpdated> result = Collections.singletonList(taskDescriptionUpdated);
        return result;
    }

    @Assign
    public List<? extends Message> handle(UpdateTaskDueDate cmd) {
        final Task state = getState();
        validateUpdateTaskDueDateCommand(getState().getTaskStatus());
        final Timestamp previousDueDate = getState().getDueDate();
        final TimestampChange taskDueDateChange = TimestampChange.newBuilder()
                                                                 .setPreviousValue(previousDueDate)
                                                                 .setNewValue(cmd.getUpdatedDueDate())
                                                                 .build();
        final TaskDueDateUpdated taskDueDateUpdated = TaskDueDateUpdated.newBuilder()
                                                                        .setId(cmd.getId())
                                                                        .setDueDateChange(taskDueDateChange)
                                                                        .build();
        final List<TaskDueDateUpdated> result = Collections.singletonList(taskDueDateUpdated);
        return result;
    }

    @Assign
    public List<? extends Message> handle(UpdateTaskPriority cmd) {
        final Task state = getState();
        validateUpdateTaskPriorityCommand(state.getTaskStatus());
        final TaskPriority previousPriority = state.getPriority();
        final PriorityChange taskPriorityChange = PriorityChange.newBuilder()
                                                                .setPreviousValue(previousPriority)
                                                                .setNewValue(cmd.getUpdatedPriority())
                                                                .build();
        final TaskPriorityUpdated taskPriorityUpdated = TaskPriorityUpdated.newBuilder()
                                                                           .setId(cmd.getId())
                                                                           .setPriorityChange(taskPriorityChange)
                                                                           .build();
        final List<TaskPriorityUpdated> result = Collections.singletonList(taskPriorityUpdated);
        return result;
    }

    @Assign
    public List<? extends Message> handle(ReopenTask cmd) {
        final Task state = getState();
        validateTransition(state.getTaskStatus(), TaskStatus.OPEN);
        final TaskReopened taskReopened = TaskReopened.newBuilder()
                                                      .setId(cmd.getId())
                                                      .build();
        final List<TaskReopened> result = Collections.singletonList(taskReopened);
        return result;
    }

    @Assign
    public List<? extends Message> handle(DeleteTask cmd) {
        final Task state = getState();
        validateTransition(state.getTaskStatus(), TaskStatus.DELETED);
        final TaskDeleted taskDeleted = TaskDeleted.newBuilder()
                                                   .setId(cmd.getId())
                                                   .build();
        final List<TaskDeleted> result = Collections.singletonList(taskDeleted);
        return result;
    }

    @Assign
    public List<? extends Message> handle(RestoreDeletedTask cmd) {
        final Task state = getState();
        validateTransition(state.getTaskStatus(), TaskStatus.OPEN);
        final DeletedTaskRestored deletedTaskRestored = DeletedTaskRestored.newBuilder()
                                                                           .setId(cmd.getId())
                                                                           .build();
        final List<Message> result = newLinkedList();
        result.add(deletedTaskRestored);
        for (TaskLabelId labelId : state.getLabelIdsList()) {
            final LabelledTaskRestored labelledTaskRestored = LabelledTaskRestored.newBuilder()
                                                                                  .setTaskId(cmd.getId())
                                                                                  .setLabelId(labelId)
                                                                                  .build();
            result.add(labelledTaskRestored);
        }
        return result;
    }

    @Assign
    public List<? extends Message> handle(CompleteTask cmd) {
        final Task state = getState();
        validateTransition(state.getTaskStatus(), TaskStatus.COMPLETED);
        final TaskCompleted taskCompleted = TaskCompleted.newBuilder()
                                                         .setId(cmd.getId())
                                                         .build();
        final List<TaskCompleted> result = Collections.singletonList(taskCompleted);
        return result;
    }

    @Assign
    public List<? extends Message> handle(FinalizeDraft cmd) {
        validateTransition(getState().getTaskStatus(), TaskStatus.FINALIZED);
        final TaskDraftFinalized taskDraftFinalized = TaskDraftFinalized.newBuilder()
                                                                        .setId(cmd.getId())
                                                                        .build();
        final List<TaskDraftFinalized> result = Collections.singletonList(taskDraftFinalized);
        return result;
    }

    @Assign
    public List<? extends Message> handle(RemoveLabelFromTask cmd) {
        validateRemoveLabelFromTaskCommand(getState().getTaskStatus());
        final LabelRemovedFromTask labelRemoved = LabelRemovedFromTask.newBuilder()
                                                                      .setId(cmd.getId())
                                                                      .setLabelId(cmd.getLabelId())
                                                                      .build();
        final List<LabelRemovedFromTask> result = Collections.singletonList(labelRemoved);
        return result;
    }

    @Assign
    public List<? extends Message> handle(AssignLabelToTask cmd) {
        validateAssignLabelToTaskCommand(getState().getTaskStatus());
        final LabelAssignedToTask labelAssigned = LabelAssignedToTask.newBuilder()
                                                                     .setTaskId(cmd.getId())
                                                                     .setLabelId(cmd.getLabelId())
                                                                     .build();
        final List<LabelAssignedToTask> result = Collections.singletonList(labelAssigned);
        return result;
    }

    @Assign
    public List<? extends Message> handle(CreateDraft cmd) {
        validateCreateDraftCommand(getState().getTaskStatus());
        final TaskDraftCreated draftCreated = TaskDraftCreated.newBuilder()
                                                              .setId(cmd.getId())
                                                              .setDraftCreationTime(Timestamps.getCurrentTime())
                                                              .build();
        final List<TaskDraftCreated> result = Collections.singletonList(draftCreated);
        return result;
    }

    /*
     * Event appliers
     *****************/

    @Apply
    private void taskCreated(TaskCreated event) {
        final TaskDetails taskDetails = event.getDetails();
        getBuilder().setId(event.getId())
                    .setCreated(Timestamps.getCurrentTime())
                    .setDescription(taskDetails.getDescription())
                    .setPriority(taskDetails.getPriority())
                    .setTaskStatus(TaskStatus.FINALIZED);
    }

    @Apply
    private void taskDescriptionUpdated(TaskDescriptionUpdated event) {
        final String newDescription = event.getDescriptionChange()
                                           .getNewValue();
        getBuilder().setDescription(newDescription);
    }

    @Apply
    private void taskDueDateUpdated(TaskDueDateUpdated event) {
        final Timestamp newDueDate = event.getDueDateChange()
                                          .getNewValue();
        getBuilder().setDueDate(newDueDate);
    }

    @Apply
    private void taskPriorityUpdated(TaskPriorityUpdated event) {
        final TaskPriority newPriority = event.getPriorityChange()
                                              .getNewValue();
        getBuilder().setPriority(newPriority);
    }

    @Apply
    private void taskReopened(TaskReopened event) {
        getBuilder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void taskDeleted(TaskDeleted event) {
        getBuilder().setTaskStatus(TaskStatus.DELETED);
    }

    @Apply
    private void deletedTaskRestored(DeletedTaskRestored event) {
        getBuilder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void labelledTaskRestored(LabelledTaskRestored event) {
        getBuilder().setTaskStatus(TaskStatus.OPEN);
    }

    @Apply
    private void taskCompleted(TaskCompleted event) {
        getBuilder().setTaskStatus(TaskStatus.COMPLETED);
    }

    @Apply
    private void taskDraftFinalized(TaskDraftFinalized event) {
        getBuilder().setTaskStatus(TaskStatus.FINALIZED);
    }

    @Apply
    private void labelAssignedToTask(LabelAssignedToTask event) {
        List<TaskLabelId> list = getState().getLabelIdsList()
                                           .stream()
                                           .collect(Collectors.toList());
        list.add(event.getLabelId());
        getBuilder().clearLabelIds()
                    .addAllLabelIds(list);
    }

    @Apply
    private void labelRemovedFromTask(LabelRemovedFromTask event) {
        List<TaskLabelId> list = getState().getLabelIdsList()
                                           .stream()
                                           .collect(Collectors.toList());
        list.remove(event.getLabelId());

        getBuilder().clearLabelIds()
                    .addAllLabelIds(list);

    }

    @Apply
    private void draftCreated(TaskDraftCreated event) {
        getBuilder().setId(event.getId())
                    .setCreated(event.getDraftCreationTime())
                    .setDescription(event.getDetails()
                                         .getDescription())
                    .setTaskStatus(TaskStatus.DRAFT);
    }

    private static void validateCommand(CreateBasicTask cmd) {
        final String description = cmd.getDescription();
        if (description != null && description.length() < MIN_DESCRIPTION_LENGTH) {
            throw new IllegalStateException(SHORT_DESCRIPTION_EXCEPTION_MESSAGE);
        }
    }

    private void validateCommand(UpdateTaskDescription cmd) {
        final String description = cmd.getUpdatedDescription();
        checkNotNull(description, NULL_DESCRIPTION_EXCEPTION_MESSAGE);

        if (description.length() < MIN_DESCRIPTION_LENGTH) {
            throw new IllegalStateException(SHORT_DESCRIPTION_EXCEPTION_MESSAGE);
        }

        ensureNeitherCompletedNorDeleted(getState().getTaskStatus());
    }
}
