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

package io.spine.examples.todolist.client;

import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AssignLabelToTask;
import io.spine.examples.todolist.c.commands.CreateBasicLabel;
import io.spine.examples.todolist.c.commands.CreateBasicTask;
import io.spine.examples.todolist.c.commands.CreateDraft;
import io.spine.examples.todolist.c.commands.UpdateTaskPriority;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.TaskItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.spine.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;
import static io.spine.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author Illia Shepilov
 */
class UpdateTaskPriorityTest extends TodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class UpdateTaskPriorityInLabelledTasksView {

        @Test
        @DisplayName("contain the task view with updated task priority")
        void containUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskItem view = obtainViewWhenHandledCommandUpdateTaskPriority(newPriority, true);
            assertEquals(newPriority, view.getPriority());
        }

        @Test
        @DisplayName("contain the task view with non-updated task priority " +
                "when command has wrong task ID")
        void containNonUpdated() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskItem view = obtainViewWhenHandledCommandUpdateTaskPriority(newPriority,
                                                                                 false);
            assertNotEquals(newPriority, view.getPriority());
        }
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class UpdateTaskPriorityInDraftTasksView {

        @Test
        @DisplayName("contain the task view with updated task priority")
        void containUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskItem view =
                    obtainViewWhenHandledUpdateTaskPriorityCommand(newPriority, true);

            assertEquals(newPriority, view.getPriority());
        }

        @Test
        @DisplayName("contain task view with non-updated task priority " +
                "when command has wrong task ID")
        void containNonUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskItem view =
                    obtainViewWhenHandledUpdateTaskPriorityCommand(newPriority, false);

            assertNotEquals(newPriority, view.getPriority());
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class UpdateTaskPriorityInMyListView {

        @Test
        @DisplayName("contain the task view with updated task priority")
        void containUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskItem view =
                    obtainTaskItemWhenHandledUpdateTaskPriority(newPriority, true);

            assertEquals(newPriority, view.getPriority());
        }

        @Test
        @DisplayName("contain task view with non-updated task priority " +
                "when command has wrong task ID")
        void containNonUpdatedView() {
            final TaskPriority newPriority = TaskPriority.HIGH;
            final TaskItem view =
                    obtainTaskItemWhenHandledUpdateTaskPriority(newPriority, false);

            assertNotEquals(newPriority, view.getPriority());
        }
    }

    private TaskItem obtainTaskItemWhenHandledUpdateTaskPriority(TaskPriority newPriority,
                                                                 boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.postCommand(createTask);

        final TaskId idOfCreatedTask = createTask.getId();
        updatePriority(newPriority, isCorrectId, idOfCreatedTask);

        final List<TaskItem> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        assertEquals(1, taskViews.size());
        final TaskItem view = taskViews.get(0);

        assertEquals(idOfCreatedTask, view.getId());
        return view;
    }

    private TaskItem obtainViewWhenHandledCommandUpdateTaskPriority(TaskPriority priority,
                                                                    boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId createdTaskId = createTask.getId();
        client.postCommand(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.postCommand(createLabel);

        final int expectedListSize = 1;
        final AssignLabelToTask assignLabelToTask =
                assignLabelToTaskInstance(createdTaskId, createLabel.getLabelId());
        client.postCommand(assignLabelToTask);

        updatePriority(priority, isCorrectId, createdTaskId);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());
        final List<TaskItem> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();

        assertEquals(expectedListSize, taskViews.size());

        final TaskItem view = taskViews.get(0);
        assertEquals(createLabel.getLabelId(), view.getLabelId());
        assertEquals(createdTaskId, view.getId());

        return view;
    }

    private TaskItem obtainViewWhenHandledUpdateTaskPriorityCommand(TaskPriority newPriority,
                                                                    boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.postCommand(createDraft);
        final TaskId createdTaskId = createDraft.getId();

        updatePriority(newPriority, isCorrectId, createdTaskId);

        final List<TaskItem> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskItem view = taskViews.get(0);
        assertEquals(createdTaskId, view.getId());

        return view;
    }

    private void updatePriority(TaskPriority newPriority, boolean isCorrectId,
                                TaskId createdTaskId) {
        final TaskId updatedTaskId = isCorrectId ? createdTaskId : createWrongTaskId();
        final UpdateTaskPriority updateTaskPriority =
                updateTaskPriorityInstance(updatedTaskId, TaskPriority.TP_UNDEFINED, newPriority);
        client.postCommand(updateTaskPriority);
    }
}
