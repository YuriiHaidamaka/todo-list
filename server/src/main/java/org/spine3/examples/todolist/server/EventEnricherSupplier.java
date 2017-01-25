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

package org.spine3.examples.todolist.server;

import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelIdList;
import org.spine3.examples.todolist.Task;
import org.spine3.examples.todolist.TaskDetails;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.c.aggregates.TaskAggregate;
import org.spine3.examples.todolist.c.aggregates.TaskLabelAggregate;
import org.spine3.examples.todolist.repositories.TaskAggregateRepository;
import org.spine3.examples.todolist.repositories.TaskLabelAggregateRepository;
import org.spine3.server.BoundedContext;
import org.spine3.server.event.enrich.EventEnricher;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Performs all necessary initializations to provide the ready-to-use {@link EventEnricher}.
 *
 * @author Illia Shepilov
 */
class EventEnricherSupplier implements Supplier<EventEnricher> {

    private static final String REPOSITORY_IS_NOT_INITIALIZED = "Repository is not initialized.";
    private final TodoListRepositoryProvider repositoryProvider;

    private EventEnricherSupplier(Builder builder) {
        this.repositoryProvider = builder.repositoryProvider;
    }

    @Override
    public EventEnricher get() {
        final Function<TaskLabelId, LabelDetails> taskLabelIdToLabelDetails = initLabelIdToDetailsFunction();
        final Function<TaskId, Task> taskIdToTask = initTaskIdToTaskFunction();
        final Function<TaskId, TaskDetails> taskIdToTaskDetails = initTaskIdToDetailsFunction();
        final Function<TaskId, LabelIdList> taskIdToLabelList = initTaskIdToLabelListFunction();
        final EventEnricher result = EventEnricher.newBuilder()
                                                  .addFieldEnrichment(TaskLabelId.class,
                                                                      LabelDetails.class,
                                                                      taskLabelIdToLabelDetails::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      TaskDetails.class,
                                                                      taskIdToTaskDetails::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      LabelIdList.class,
                                                                      taskIdToLabelList::apply)
                                                  .addFieldEnrichment(TaskId.class,
                                                                      Task.class,
                                                                      taskIdToTask::apply)
                                                  .build();
        return result;
    }

    private Function<TaskId, Task> initTaskIdToTaskFunction() {
        final Function<TaskId, Task> result = taskId -> {
            if (taskId == null) {
                return Task.getDefaultInstance();
            }
            final TaskAggregateRepository taskAggregateRepository =
                    repositoryProvider.getTaskAggregateRepository()
                                      .orElseThrow(() -> new RepositoryNotInitializedException(
                                              REPOSITORY_IS_NOT_INITIALIZED));
            final TaskAggregate taskAggregate = taskAggregateRepository.load(taskId);
            final Task task = taskAggregate.getState();
            return task;
        };
        return result;
    }

    private Function<TaskLabelId, LabelDetails> initLabelIdToDetailsFunction() {
        final Function<TaskLabelId, LabelDetails> result = labelId -> {
            if (labelId == null) {
                return LabelDetails.getDefaultInstance();
            }
            final TaskLabelAggregateRepository labelAggregateRepository =
                    repositoryProvider.getLabelAggregateRepository()
                                      .orElseThrow(() -> new RepositoryNotInitializedException(
                                              REPOSITORY_IS_NOT_INITIALIZED));
            final TaskLabelAggregate aggregate = labelAggregateRepository.load(labelId);
            final TaskLabel state = aggregate.getState();
            final LabelDetails details = LabelDetails.newBuilder()
                                                     .setColor(state.getColor())
                                                     .setTitle(state.getTitle())
                                                     .build();
            return details;
        };

        return result;
    }

    private Function<TaskId, TaskDetails> initTaskIdToDetailsFunction() {
        final Function<TaskId, TaskDetails> result = taskId -> {
            if (taskId == null) {
                return TaskDetails.getDefaultInstance();
            }
            final TaskAggregateRepository taskAggregateRepository =
                    repositoryProvider.getTaskAggregateRepository()
                                      .orElseThrow(() -> new RepositoryNotInitializedException(
                                              REPOSITORY_IS_NOT_INITIALIZED));
            final TaskAggregate aggregate = taskAggregateRepository.load(taskId);
            final Task state = aggregate.getState();
            final TaskDetails details = TaskDetails.newBuilder()
                                                   .setDescription(state.getDescription())
                                                   .setPriority(state.getPriority())
                                                   .build();
            return details;
        };

        return result;
    }

    private Function<TaskId, LabelIdList> initTaskIdToLabelListFunction() {
        final Function<TaskId, LabelIdList> result = taskId -> {
            final TaskAggregateRepository taskAggregateRepository =
                    repositoryProvider.getTaskAggregateRepository()
                                      .orElseThrow(() -> new RepositoryNotInitializedException(
                                              REPOSITORY_IS_NOT_INITIALIZED));
            final TaskAggregate aggregate = taskAggregateRepository.load(taskId);
            final List<TaskLabelId> labelIdsList = aggregate.getState()
                                                            .getLabelIdsList();
            final LabelIdList labelIdList = LabelIdList.newBuilder()
                                                       .addAllLabelId(labelIdsList)
                                                       .build();
            return labelIdList;
        };
        return result;
    }

    /**
     * Creates a new builder for the {@code EventEnricherSupplier}.
     *
     * @return new builder instance
     */
    static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for producing {@code EventEnricherSupplier} instances.
     */
    static class Builder {

        private TodoListRepositoryProvider repositoryProvider;

        private Builder() {
        }

        /**
         * Sets the {@link TodoListRepositoryProvider}.
         *
         * <p> It is not possible to pass the initialized repositories to the {@link EventEnricherSupplier} constructor,
         * because at the time of {@code enricher} initialization, the repositories are not initialized yet.
         *
         * <p> To initialize the {@link BoundedContext}, the instance of the {@code EventEnrciher} is required.
         * And to initialize the {@code repository} instances, the {@code BoundedContext} instance is required.
         *
         * <p> The {@code TodoListRepositoryProvider} is intended to break the cyclic dependency
         * by providing the {@code repository} instances in a “lazy” mode.
         *
         * @param repositoryProvider the task aggregate repository
         * @return the {@code Builder}
         * @see TodoListRepositoryProvider
         */
        Builder setRepositoryProvider(TodoListRepositoryProvider repositoryProvider) {
            this.repositoryProvider = repositoryProvider;
            return this;
        }

        /**
         * Returns the constructed {@link EventEnricherSupplier}.
         *
         * @return the {@code EventEnricherSupplier} instance
         */
        EventEnricherSupplier build() {
            final EventEnricherSupplier result = new EventEnricherSupplier(this);
            return result;
        }
    }
}
