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

package org.spine3.examples.todolist.q.projection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.spine3.base.Event;
import org.spine3.base.EventContext;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
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
import org.spine3.examples.todolist.repository.LabelledTasksViewRepository;
import org.spine3.server.BoundedContext;
import org.spine3.server.event.EventBus;
import org.spine3.server.event.enrich.EventEnricher;
import org.spine3.server.projection.ProjectionRepository;
import org.spine3.server.storage.StorageFactory;
import org.spine3.server.storage.StorageFactorySwitch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.base.Events.createEvent;
import static org.spine3.base.Identifiers.newUuid;
import static org.spine3.examples.todolist.testdata.TestBoundedContextFactory.boundedContextInstance;
import static org.spine3.examples.todolist.testdata.TestEventBusFactory.eventBusInstance;
import static org.spine3.examples.todolist.testdata.TestEventContextFactory.eventContextInstance;
import static org.spine3.examples.todolist.testdata.TestEventEnricherFactory.LABEL_TITLE;
import static org.spine3.examples.todolist.testdata.TestEventEnricherFactory.eventEnricherInstance;
import static org.spine3.examples.todolist.testdata.TestLabelEventFactory.labelDetailsUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.labelledTaskRestoredInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskCompletedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskDeletedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.ChangeStatusEvents.taskReopenedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.LABEL_ID;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.TASK_ID;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.UPDATED_DESCRIPTION;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.UPDATED_TASK_DUE_DATE;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.UPDATED_TASK_PRIORITY;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.UpdateEvents.taskDescriptionUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.UpdateEvents.taskDueDateUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskEventFactory.UpdateEvents.taskPriorityUpdatedInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelsEventFactory.labelAssignedToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelsEventFactory.labelRemovedFromTaskInstance;

/**
 * @author Illia Shepilov
 */
@SuppressWarnings("OptionalGetWithoutIsPresent") // it is OK as object creation is controlled during the test.
public class LabelledTasksViewProjectionTest extends ProjectionTest {

    private final EventContext eventContext = eventContextInstance();
    private ProjectionRepository<LabelId, LabelledTasksViewProjection, LabelledTasksView> repository;
    private EventBus eventBus;

    @BeforeEach
    public void setUp() {
        final StorageFactorySwitch storageFactorySwitch = StorageFactorySwitch.getInstance();
        final StorageFactory storageFactory = storageFactorySwitch.get();
        final EventEnricher eventEnricher = eventEnricherInstance();
        eventBus = eventBusInstance(storageFactory, eventEnricher);
        final BoundedContext boundedContext = boundedContextInstance(eventBus, storageFactorySwitch);
        repository = new LabelledTasksViewRepository(boundedContext);
        repository.initStorage(storageFactory);
        boundedContext.register(repository);
    }

    @Nested
    @DisplayName("LabelAssignedToTask event should be interpreted by LabelledTasksViewProjection and")
    class LabelAssignedToTaskEvent {

        @Test
        @DisplayName("add TaskView to LabelledTasksView")
        public void addView() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);
            LabelledTasksView labelledTaskView = repository.load(LABEL_ID)
                                                           .get()
                                                           .getState();

            TaskListView listView = labelledTaskView.getLabelledTasks();

            matchesExpectedValues(labelledTaskView);

            int actualListSize = listView.getItemsCount();
            int expectedListSize = 1;
            assertEquals(expectedListSize, actualListSize);

            TaskView view = listView.getItems(0);

            matchesExpectedValues(view);

            eventBus.post(labelAssignedToTaskEvent);

            labelledTaskView = repository.load(LABEL_ID)
                                         .get()
                                         .getState();
            listView = labelledTaskView.getLabelledTasks();
            actualListSize = listView.getItemsCount();

            expectedListSize = 2;
            assertEquals(expectedListSize, actualListSize);

            view = listView.getItems(0);

            matchesExpectedValues(view);
            matchesExpectedValues(labelledTaskView);

            view = listView.getItems(1);

            matchesExpectedValues(view);
            matchesExpectedValues(labelledTaskView);
        }
    }

    @Nested
    @DisplayName("LabelRemovedFromTask event should be interpreted by LabelledTasksViewProjection and")
    class LabelRemovedFromTaskEvent {

        @Test
        @DisplayName("remove TaskView from LabelledTasksView")
        public void removeView() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);
            eventBus.post(labelAssignedToTaskEvent);

            LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                            .get()
                                                            .getState();
            assertEquals(LABEL_ID, labelledTasksView.getLabelId());

            final LabelRemovedFromTask labelRemovedFromTask = labelRemovedFromTaskInstance();
            final Event labelRemovedFromTaskEvent = createEvent(labelRemovedFromTask, eventContext);
            eventBus.post(labelRemovedFromTaskEvent);

            labelledTasksView = repository.load(LABEL_ID)
                                          .get()
                                          .getState();
            doesNotMatchValues(labelledTasksView);

            final TaskListView labelledTasks = labelledTasksView.getLabelledTasks();

            assertTrue(labelledTasks.getItemsList()
                                    .isEmpty());
        }
    }

    @Nested
    @DisplayName("LabelledTaskRestored event should be interpreted by LabelledTasksViewProjection and")
    class LabelledTaskRestoredEvent {

        @Test
        @DisplayName("add TaskView to LabelledTasksView")
        public void addView() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);
            eventBus.post(labelAssignedToTaskEvent);

            final LabelRemovedFromTask labelRemovedFromTask = labelRemovedFromTaskInstance();
            final Event labelRemovedFromTaskEvent = createEvent(labelRemovedFromTask, eventContext);
            eventBus.post(labelRemovedFromTaskEvent);

            final LabelledTaskRestored deletedTaskRestored = labelledTaskRestoredInstance();
            final Event deletedTaskRestoredEvent = createEvent(deletedTaskRestored, eventContext);
            eventBus.post(deletedTaskRestoredEvent);

            LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                            .get()
                                                            .getState();
            matchesExpectedValues(labelledTasksView);

            TaskListView listView = labelledTasksView.getLabelledTasks();
            int actualListSize = listView.getItemsCount();

            int expectedListSize = 1;
            assertEquals(expectedListSize, actualListSize);
            final TaskView taskView = listView.getItems(0);
            assertEquals(TASK_ID, taskView.getId());

            eventBus.post(deletedTaskRestoredEvent);
            labelledTasksView = repository.load(LABEL_ID)
                                          .get()
                                          .getState();
            matchesExpectedValues(labelledTasksView);
            listView = repository.load(LABEL_ID)
                                 .get()
                                 .getState()
                                 .getLabelledTasks();

            actualListSize = listView.getItemsCount();
            expectedListSize = 2;
            assertEquals(expectedListSize, actualListSize);
            assertEquals(TASK_ID, listView.getItems(0)
                                          .getId());
            assertEquals(TASK_ID, listView.getItems(1)
                                          .getId());
        }
    }

    @Nested
    @DisplayName("TaskDeleted event should be interpreted by LabelledTasksViewProjection and")
    class TaskDeletedEvent {

        @Test
        @DisplayName("remove TaskView from LabelledTasksView")
        public void removesView() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDeleted taskDeleted = taskDeletedInstance();
            final Event deletedTaskEvent = createEvent(taskDeleted, eventContext);
            eventBus.post(deletedTaskEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);

            TaskListView listView = labelledTasksView.getLabelledTasks();
            int actualListSize = listView.getItemsCount();

            int expectedListSize = 1;
            assertEquals(expectedListSize, actualListSize);

            final TaskView taskView = listView.getItems(0);

            matchesExpectedValues(taskView);

            eventBus.post(deletedTaskEvent);

            expectedListSize = 0;
            listView = repository.load(LABEL_ID)
                                 .get()
                                 .getState()
                                 .getLabelledTasks();
            actualListSize = listView.getItemsCount();

            eventBus.post(deletedTaskEvent);
            assertEquals(expectedListSize, actualListSize);
            assertTrue(listView.getItemsList()
                               .isEmpty());
        }
    }

    @Nested
    @DisplayName("TaskDescriptionUpdated event should be interpreted by LabelledTasksViewProjection and")
    class UpdateTaskDescriptionEvent {

        @Test
        @DisplayName("update the task description in LabelledTasksView")
        public void updateDescription() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDescriptionUpdated taskDescriptionUpdated = taskDescriptionUpdatedInstance();
            final Event descriptionUpdatedEvent = createEvent(taskDescriptionUpdated, eventContext);
            eventBus.post(descriptionUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);

            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertEquals(UPDATED_DESCRIPTION, taskView.getDescription());
        }

        @Test
        @DisplayName("not update the task description in LabelledTasksView by wrong task ID")
        public void notUpdateDescription() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDescriptionUpdated taskDescriptionUpdated =
                    taskDescriptionUpdatedInstance(TaskId.getDefaultInstance(), UPDATED_DESCRIPTION);
            final Event descriptionUpdatedEvent = createEvent(taskDescriptionUpdated, eventContext);
            eventBus.post(descriptionUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertNotEquals(UPDATED_DESCRIPTION, taskView.getDescription());
        }

    }

    @Nested
    @DisplayName("TaskPriorityUpdated event should be interpreted by LabelledTasksViewProjection and")
    class TaskPriorityUpdatedEvent {

        @Test
        @DisplayName("update the task priority in LabelledTasksView")
        public void updatePriority() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskPriorityUpdated taskPriorityUpdated = taskPriorityUpdatedInstance();
            final Event taskPriorityUpdatedEvent = createEvent(taskPriorityUpdated, eventContext);
            eventBus.post(taskPriorityUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertEquals(UPDATED_TASK_PRIORITY, taskView.getPriority());
        }

        @Test
        @DisplayName("not update the task priority in LabelledTasksView by wrong task ID")
        public void notUpdatePriority() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskPriorityUpdated taskPriorityUpdated =
                    taskPriorityUpdatedInstance(TaskId.getDefaultInstance(), UPDATED_TASK_PRIORITY);
            final Event taskPriorityUpdatedEvent = createEvent(taskPriorityUpdated, eventContext);
            eventBus.post(taskPriorityUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertNotEquals(UPDATED_TASK_PRIORITY, taskView.getPriority());
        }
    }

    @Nested
    @DisplayName("TaskDueDateUpdated event should be interpreted by LabelledTasksViewProjection and")
    class TaskDueDateUpdatedEvent {

        @Test
        @DisplayName("update the task due date in LabelledTasksView")
        public void updateDueDate() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDueDateUpdated taskDueDateUpdated = taskDueDateUpdatedInstance();
            final Event taskDueDateUpdatedEvent = createEvent(taskDueDateUpdated, eventContext);
            eventBus.post(taskDueDateUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertEquals(UPDATED_TASK_DUE_DATE, taskView.getDueDate());
        }

        @Test
        @DisplayName("not update the task due date in LabelledTasksView by wrong task ID")
        public void doesNotUpdateDueDate() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskDueDateUpdated taskDueDateUpdated =
                    taskDueDateUpdatedInstance(TaskId.getDefaultInstance(), UPDATED_TASK_DUE_DATE);
            final Event taskDueDateUpdatedEvent = createEvent(taskDueDateUpdated, eventContext);
            eventBus.post(taskDueDateUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertNotEquals(UPDATED_TASK_DUE_DATE, taskView.getDueDate());
        }
    }

    @Nested
    @DisplayName("TaskCompleted event should be interpreted by LabelledTasksViewProjection and")
    class TaskCompletedEvent {

        @Test
        @DisplayName("set `completed` to true in LabelledTasksView")
        public void setCompletedFlagToTrue() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskCompleted taskCompleted = taskCompletedInstance();
            final Event taskCompletedEvent = createEvent(taskCompleted, eventContext);
            eventBus.post(taskCompletedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertTrue(taskView.getCompleted());
        }

        @Test
        @DisplayName("set `completed` to false in LabelledTasksView")
        public void setCompletedFlagToFalse() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskCompleted taskCompleted = taskCompletedInstance();
            final Event taskCompletedEvent = createEvent(taskCompleted, eventContext);
            eventBus.post(taskCompletedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertTrue(taskView.getCompleted());
        }
    }

    @Nested
    @DisplayName("TaskReopened event should be interpreted by LabelledTasksViewProjection and")
    class TaskReopenedEvent {

        @Test
        @DisplayName("set `completed` to `false` in LabelledTasksView")
        public void setCompletedFlagToFalse() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskCompleted taskCompleted = taskCompletedInstance();
            final Event taskCompletedEvent = createEvent(taskCompleted, eventContext);
            eventBus.post(taskCompletedEvent);

            final TaskReopened taskReopened = taskReopenedInstance();
            final Event taskReopenedEvent = createEvent(taskReopened, eventContext);
            eventBus.post(taskReopenedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertFalse(taskView.getCompleted());
        }

        @Test
        @DisplayName("set `completed` to `true` in LabelledTasksView")
        public void setCompletedFlagToTrue() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final TaskCompleted taskCompleted = taskCompletedInstance();
            final Event taskCompletedEvent = createEvent(taskCompleted, eventContext);
            eventBus.post(taskCompletedEvent);

            final TaskReopened taskReopened = taskReopenedInstance(TaskId.getDefaultInstance());
            final Event taskReopenedEvent = createEvent(taskReopened, eventContext);
            eventBus.post(taskReopenedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            matchesExpectedValues(labelledTasksView);
            final TaskListView listView = labelledTasksView.getLabelledTasks();
            final int actualListSize = listView.getItemsCount();
            assertEquals(1, actualListSize);

            final TaskView taskView = listView.getItems(0);
            assertTrue(taskView.getCompleted());
        }
    }

    @Nested
    @DisplayName("LabelDetailsUpdated event should be interpreted by LabelledTasksViewProjection and")
    class LabelDetailsUpdatedEvent {

        private static final String UPDATED_LABEL_TITLE = "Updated label title.";

        @Test
        @DisplayName("update the label details in LabelledTasksView")
        public void updateLabelDetails() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final LabelDetailsUpdated labelDetailsUpdated =
                    labelDetailsUpdatedInstance(LABEL_ID, LabelColor.RED, UPDATED_LABEL_TITLE);
            final Event labelDetailsUpdatedEvent = createEvent(labelDetailsUpdated, eventContext);
            eventBus.post(labelDetailsUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            assertEquals(LABEL_ID, labelledTasksView.getLabelId());
            assertEquals(UPDATED_LABEL_TITLE, labelledTasksView.getLabelTitle());
            assertEquals(LabelColorView.RED_COLOR.getHexColor(), labelledTasksView.getLabelColor());
        }

        @Test
        @DisplayName("not update the label details in LabelledTasksView by wrong task ID")
        public void notUpdateLabelDetails() {
            final LabelAssignedToTask labelAssignedToTask = labelAssignedToTaskInstance();
            final Event labelAssignedToTaskEvent = createEvent(labelAssignedToTask, eventContext);
            eventBus.post(labelAssignedToTaskEvent);

            final LabelId wrongLabelId = LabelId.newBuilder()
                                                .setValue(newUuid())
                                                .build();

            final LabelDetailsUpdated labelDetailsUpdated =
                    labelDetailsUpdatedInstance(wrongLabelId, LabelColor.RED, UPDATED_LABEL_TITLE);
            final Event labelDetailsUpdatedEvent = createEvent(labelDetailsUpdated, eventContext);
            eventBus.post(labelDetailsUpdatedEvent);

            final LabelledTasksView labelledTasksView = repository.load(LABEL_ID)
                                                                  .get()
                                                                  .getState();
            assertEquals(LABEL_ID, labelledTasksView.getLabelId());
            assertNotEquals(UPDATED_LABEL_TITLE, labelledTasksView.getLabelTitle());
            assertNotEquals(LabelColorView.RED_COLOR.getHexColor(), labelledTasksView.getLabelColor());
        }
    }

    private static void matchesExpectedValues(TaskView taskView) {
        assertEquals(TASK_ID, taskView.getId());
        assertEquals(LABEL_ID, taskView.getLabelId());
    }

    private static void matchesExpectedValues(LabelledTasksView labelledTaskView) {
        assertEquals(LabelColorView.valueOf(LabelColor.BLUE), labelledTaskView.getLabelColor());
        assertEquals(LABEL_TITLE, labelledTaskView.getLabelTitle());
    }

    private static void doesNotMatchValues(LabelledTasksView labelledTaskView) {
        assertNotEquals(LabelColorView.valueOf(LabelColor.BLUE), labelledTaskView.getLabelColor());
        assertNotEquals(LABEL_TITLE, labelledTaskView.getLabelTitle());
    }
}
