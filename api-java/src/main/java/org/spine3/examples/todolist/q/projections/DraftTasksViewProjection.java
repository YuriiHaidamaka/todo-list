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

package org.spine3.examples.todolist.q.projections;

import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskListId;
import org.spine3.examples.todolist.c.events.LabelAssignedToTask;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;
import org.spine3.examples.todolist.c.events.LabelRemovedFromTask;
import org.spine3.examples.todolist.c.events.TaskDeleted;
import org.spine3.examples.todolist.c.events.TaskDescriptionUpdated;
import org.spine3.examples.todolist.c.events.TaskDraftCreated;
import org.spine3.examples.todolist.c.events.TaskDraftFinalized;
import org.spine3.examples.todolist.c.events.TaskDueDateUpdated;
import org.spine3.examples.todolist.c.events.TaskPriorityUpdated;
import org.spine3.server.event.Subscribe;
import org.spine3.server.projection.Projection;

import java.util.List;
import java.util.stream.Collectors;

import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.q.projections.ProjectionHelper.removeViewByTaskId;
import static org.spine3.examples.todolist.q.projections.ProjectionHelper.updateTaskViewList;

/**
 * A projections state of tasks in a "draft" state.
 *
 * <p> This view includes all non-deleted tasks, which creation is in progress.
 *
 * @author Illia Shepilov
 */
public class DraftTasksViewProjection extends Projection<TaskListId, DraftTasksView> {

    /**
     * As long as there is just a single end-user in the app, the {@link DraftTasksViewProjection} is a singleton.
     */
    public static final TaskListId ID = TaskListId.newBuilder()
                                                  .setValue(newUuid())
                                                  .build();

    /**
     * Creates a new instance.
     *
     * @param id the ID for the new instance
     * @throws IllegalArgumentException if the ID is not of one of the supported types
     */
    public DraftTasksViewProjection(TaskListId id) {
        super(id);
    }

    @Subscribe
    public void on(TaskDraftCreated event) {
        final TaskDetails taskDetails = event.getDetails();
        final TaskView taskView = TaskView.newBuilder()
                                          .setId(event.getId())
                                          .setDescription(taskDetails.getDescription())
                                          .setPriority(taskDetails.getPriority())
                                          .setCompleted(taskDetails.getCompleted())
                                          .build();
        final List<TaskView> views = getState().getDraftTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        views.add(taskView);
        final TaskListView taskListView = TaskListView.newBuilder()
                                                      .addAllItems(views)
                                                      .build();
        final DraftTasksView state = getState().newBuilderForType()
                                               .setDraftTasks(taskListView)
                                               .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDraftFinalized event) {
        final List<TaskView> views = getState().getDraftTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        final TaskListView taskListView = removeViewByTaskId(views, event.getId());
        final DraftTasksView state = getState().newBuilderForType()
                                               .setDraftTasks(taskListView)
                                               .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDeleted event) {
        final List<TaskView> views = getState().getDraftTasks()
                                               .getItemsList()
                                               .stream()
                                               .collect(Collectors.toList());
        final TaskListView taskListView = removeViewByTaskId(views, event.getId());
        final DraftTasksView state = getState().newBuilderForType()
                                               .setDraftTasks(taskListView)
                                               .build();
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDescriptionUpdated event) {
        final List<TaskView> views = getState().getDraftTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final DraftTasksView state = constructDraftTasksViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(TaskPriorityUpdated event) {
        final List<TaskView> views = getState().getDraftTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final DraftTasksView state = constructDraftTasksViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(TaskDueDateUpdated event) {
        final List<TaskView> views = getState().getDraftTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final DraftTasksView state = constructDraftTasksViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelAssignedToTask event) {
        final List<TaskView> views = getState().getDraftTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final DraftTasksView state = constructDraftTasksViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelRemovedFromTask event) {
        final List<TaskView> views = getState().getDraftTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final DraftTasksView state = constructDraftTasksViewState(updatedList);
        incrementState(state);
    }

    @Subscribe
    public void on(LabelDetailsUpdated event) {
        final List<TaskView> views = getState().getDraftTasks()
                                               .getItemsList();
        final List<TaskView> updatedList = updateTaskViewList(views, event);
        final DraftTasksView state = constructDraftTasksViewState(updatedList);
        incrementState(state);
    }

    private DraftTasksView constructDraftTasksViewState(List<TaskView> updatedList) {
        final TaskListView listView = TaskListView.newBuilder()
                                                  .addAllItems(updatedList)
                                                  .build();
        final DraftTasksView result = getState().newBuilderForType()
                                                .setDraftTasks(listView)
                                                .build();
        return result;
    }
}
