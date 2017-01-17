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

package org.spine3.examples.todolist.repositories;

import com.google.common.collect.Sets;
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.LabelIdList;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.c.enrichments.LabelListEnrichment;
import org.spine3.examples.todolist.c.events.LabelAssignedToTask;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;
import org.spine3.examples.todolist.c.events.LabelRemovedFromTask;
import org.spine3.examples.todolist.c.events.LabelledTaskRestored;
import org.spine3.examples.todolist.c.events.TaskCompleted;
import org.spine3.examples.todolist.c.events.TaskDeleted;
import org.spine3.examples.todolist.c.events.TaskDescriptionUpdated;
import org.spine3.examples.todolist.c.events.TaskDueDateUpdated;
import org.spine3.examples.todolist.c.events.TaskPriorityUpdated;
import org.spine3.examples.todolist.c.events.TaskReopened;
import org.spine3.examples.todolist.q.projections.LabelledTasksViewProjection;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.server.BoundedContext;
import org.spine3.server.entity.IdSetEventFunction;
import org.spine3.server.projection.ProjectionRepository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.spine3.examples.todolist.EnrichmentHelper.getEnrichment;

/**
 * Repository for the {@link LabelledTasksViewProjection}.
 *
 * @author Illia Shepilov
 */
public class LabelledTasksViewRepository
        extends ProjectionRepository<TaskLabelId, LabelledTasksViewProjection, LabelledTasksView> {
    public LabelledTasksViewRepository(BoundedContext boundedContext) {
        super(boundedContext);
        addIdSetFunctions();
    }

    /**
     * Adds the {@link IdSetEventFunction}s to the repository.
     * Should to be overridden in an successor classes,
     * otherwise all successors will use {@code TaskLabelId}
     * and only with specified events below.
     */
    protected void addIdSetFunctions() {
        final IdSetEventFunction<TaskLabelId, LabelAssignedToTask> labelAssignedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelAssignedToTask.class, labelAssignedFn);

        final IdSetEventFunction<TaskLabelId, LabelRemovedFromTask> labelRemovedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelRemovedFromTask.class, labelRemovedFn);

        final IdSetEventFunction<TaskLabelId, LabelledTaskRestored> deletedTaskRestoredFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelledTaskRestored.class, deletedTaskRestoredFn);

        final IdSetEventFunction<TaskLabelId, LabelDetailsUpdated> labelDetailsUpdatedFn =
                (message, context) -> Collections.singleton(message.getLabelId());
        addIdSetFunction(LabelDetailsUpdated.class, labelDetailsUpdatedFn);

        final IdSetEventFunction<TaskLabelId, TaskDeleted> taskDeletedFn = (message, context) ->
                getLabelIdsSet(context);
        addIdSetFunction(TaskDeleted.class, taskDeletedFn);

        final IdSetEventFunction<TaskLabelId, TaskReopened> taskReopenedFn = (message, context) ->
                getLabelIdsSet(context);
        addIdSetFunction(TaskReopened.class, taskReopenedFn);

        final IdSetEventFunction<TaskLabelId, TaskCompleted> taskCompletedFn = (message, context) ->
                getLabelIdsSet(context);
        addIdSetFunction(TaskCompleted.class, taskCompletedFn);

        final IdSetEventFunction<TaskLabelId, TaskPriorityUpdated> taskPriorityUpdatedFn = (message, context) ->
                getLabelIdsSet(context);
        addIdSetFunction(TaskPriorityUpdated.class, taskPriorityUpdatedFn);

        final IdSetEventFunction<TaskLabelId, TaskDescriptionUpdated> taskDescriptionUpdatedFn = (message, context) ->
                getLabelIdsSet(context);
        addIdSetFunction(TaskDescriptionUpdated.class, taskDescriptionUpdatedFn);

        final IdSetEventFunction<TaskLabelId, TaskDueDateUpdated> taskDueDateUpdatedFn = (message, context) ->
                getLabelIdsSet(context);
        addIdSetFunction(TaskDueDateUpdated.class, taskDueDateUpdatedFn);
    }

    private Set<TaskLabelId> getLabelIdsSet(EventContext context) {
        final LabelListEnrichment enrichment = getEnrichment(LabelListEnrichment.class, context);
        final LabelIdList labelIdList = enrichment.getLabelIdList();
        final HashSet<TaskLabelId> result = Sets.newHashSet(labelIdList.getLabelIdList());
        return result;
    }
}
