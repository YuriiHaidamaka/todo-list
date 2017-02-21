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

package org.spine3.examples.todolist.client;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.LabelId;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.q.projection.LabelledTasksView;
import org.spine3.examples.todolist.q.projection.TaskView;
import org.spine3.protobuf.Timestamps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static org.spine3.examples.todolist.testdata.TestTaskLabelsCommandFactory.assignLabelToTaskInstance;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution of UpdateTaskDueDate command")
public class UpdateTaskDueDateTest extends CommandLineTodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Nested
    @DisplayName("LabelledTasksView should")
    class UpdateTaskDueDateInLabelledTasksView {

        @Test
        @DisplayName("contain the task view with updated due date")
        public void containUpdatedView() {
            final Timestamp newDueDate = Timestamps.getCurrentTime();
            final TaskView view = obtainViewWhenHandledCommandUpdateTaskDueDate(newDueDate, true);
            assertEquals(newDueDate, view.getDueDate());
        }

        @Test
        @DisplayName("contain the task view with not updated due date when command has wrong task ID")
        public void containNotUpdatedView() {
            final Timestamp newDueDate = Timestamps.getCurrentTime();
            final TaskView view = obtainViewWhenHandledCommandUpdateTaskDueDate(newDueDate, false);
            assertNotEquals(newDueDate, view.getDueDate());
        }
    }

    @Nested
    @DisplayName("DraftTasksView should")
    class UpdateTaskDueDateInDraftTasksView {

        @Test
        @DisplayName("contain the task view with updated due date")
        public void containUpdatedView() {
            final Timestamp newDueDate = Timestamps.getCurrentTime();
            final TaskView view = obtainViewWhenHandledUpdateTaskDueDate(newDueDate, true);

            assertEquals(newDueDate, view.getDueDate());
        }

        @Test
        @DisplayName("contain the task view with not updated due date when command has wrong task ID")
        public void containNotUpdatedView() {
            final Timestamp newDueDate = Timestamps.getCurrentTime();
            final TaskView view = obtainViewWhenHandledUpdateTaskDueDate(newDueDate, false);

            assertNotEquals(newDueDate, view.getDueDate());
        }
    }

    @Nested
    @DisplayName("MyListView should")
    class UpdateTaskDueDateInMyListView {

        @Test
        @DisplayName("contain the task view with updated due date")
        public void containUpdatedView() {
            final Timestamp newDueDate = Timestamps.getCurrentTime();
            final TaskView view = obtainTaskViewWhenHandledUpdateTaskDueDate(newDueDate, true);
            assertEquals(newDueDate, view.getDueDate());
        }

        @Test
        @DisplayName("contain task view with not updated due date when command has wrong task ID")
        public void containNotUpdatedView() {
            final Timestamp newDueDate = Timestamps.getCurrentTime();
            final TaskView view = obtainTaskViewWhenHandledUpdateTaskDueDate(newDueDate, false);
            assertNotEquals(newDueDate, view.getDueDate());
        }
    }

    private TaskView obtainTaskViewWhenHandledUpdateTaskDueDate(Timestamp newDueDate, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        client.create(createTask);

        final TaskId idOfCreatedTask = createTask.getId();

        updateDueDate(newDueDate, isCorrectId, idOfCreatedTask);
        final List<TaskView> taskViews = client.getMyListView()
                                               .getMyList()
                                               .getItemsList();
        assertEquals(1, taskViews.size());
        final TaskView view = taskViews.get(0);

        assertEquals(idOfCreatedTask, view.getId());
        return view;
    }

    private TaskView obtainViewWhenHandledUpdateTaskDueDate(Timestamp newDueDate, boolean isCorrectId) {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final TaskId createdTaskId = createDraft.getId();

        updateDueDate(newDueDate, isCorrectId, createdTaskId);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                               .getDraftTasks()
                                               .getItemsList();
        assertEquals(1, taskViews.size());

        final TaskView view = taskViews.get(0);
        assertEquals(createdTaskId, view.getId());

        return view;
    }

    private TaskView obtainViewWhenHandledCommandUpdateTaskDueDate(Timestamp newDueDate, boolean isCorrectId) {
        final CreateBasicTask createTask = createBasicTask();
        final TaskId createdTaskId = createTask.getId();
        client.create(createTask);

        final CreateBasicLabel createLabel = createBasicLabel();
        client.create(createLabel);

        final TaskId taskId = createTask.getId();
        final LabelId labelId = createLabel.getLabelId();

        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        updateDueDate(newDueDate, isCorrectId, createdTaskId);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        final int expectedListSize = 1;
        assertEquals(expectedListSize, tasksViewList.size());

        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(labelId, view.getLabelId());
        assertEquals(taskId, view.getId());

        return view;
    }

    private void updateDueDate(Timestamp newDueDate, boolean isCorrectId, TaskId idOfCreatedTask) {
        final TaskId idOfUpdatedTask = isCorrectId ? idOfCreatedTask : createWrongTaskId();
        final Timestamp previousDueDate = Timestamp.getDefaultInstance();
        final UpdateTaskDueDate updateTaskDueDate =
                updateTaskDueDateInstance(idOfUpdatedTask, previousDueDate, newDueDate);
        client.update(updateTaskDueDate);
    }
}
