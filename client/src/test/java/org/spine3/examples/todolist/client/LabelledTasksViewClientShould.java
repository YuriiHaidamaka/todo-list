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

package org.spine3.examples.todolist.client;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.AssignLabelToTask;
import org.spine3.examples.todolist.CreateBasicLabel;
import org.spine3.examples.todolist.CreateBasicTask;
import org.spine3.examples.todolist.DeleteTask;
import org.spine3.examples.todolist.RemoveLabelFromTask;
import org.spine3.examples.todolist.RestoreDeletedTask;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.UpdateTaskDescription;
import org.spine3.examples.todolist.UpdateTaskDueDate;
import org.spine3.examples.todolist.UpdateTaskPriority;
import org.spine3.examples.todolist.view.LabelledTasksView;
import org.spine3.examples.todolist.view.TaskListView;
import org.spine3.examples.todolist.view.TaskView;
import org.spine3.protobuf.Timestamps;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.assignLabelToTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.deleteTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.removeLabelFromTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.restoreDeletedTaskInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDescriptionInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskDueDateInstance;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.updateTaskPriorityInstance;

/**
 * @author Illia Shepilov
 */
public class LabelledTasksViewClientShould extends BasicTodoClientShould {

    @Test
    public void obtain_labelled_tasks_view_when_handled_command_deleted_task_restored() {
        final int expectedListSize = 1;
        final CreateBasicTask createTask = createBasicTaskInstance();
        final CreateBasicLabel createLabel = createBasicLabelInstance();
        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(taskId);
        client.create(createTask);
        client.create(createLabel);
        client.assignLabel(assignLabelToTask);
        client.delete(deleteTask);
        client.restore(restoreDeletedTask);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());
        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();

        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(taskId, view.getId());
        assertEquals(labelId, view.getLabelId());
    }

    @Test
    public void obtain_empty_labelled_tasks_view_when_handled_command_deleted_task_restored_with_wrong_task_id() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        client.create(createTask);
        
        final CreateBasicLabel createLabel = createBasicLabelInstance();
        client.create(createLabel);

        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.assignLabel(assignLabelToTask);

        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        client.delete(deleteTask);

        final RestoreDeletedTask restoreDeletedTask = restoreDeletedTaskInstance(getWrongTaskId());
        client.restore(restoreDeletedTask);

        final int expectedListSize = 1;
        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());

        final TaskListView taskListView = tasksViewList.get(0)
                                                       .getLabelledTasks();
        final List<TaskView> taskViews = taskListView.getItemsList();

        assertTrue(taskViews.isEmpty());
    }

    @Test
    public void obtain_tasks_with_assigned_labels_from_labelled_tasks_view() {
        final int expectedListSize = 1;
        final CreateBasicTask createTask = createBasicTaskInstance();
        final CreateBasicLabel createLabel = createBasicLabelInstance();
        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.create(createTask);
        client.create(createLabel);
        client.assignLabel(assignLabelToTask);
        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());
        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(taskId, view.getId());
        assertEquals(labelId, view.getId());
    }

    @Test
    public void obtain_empty_labelled_tasks_view_when_handled_command_assign_label_to_task_with_wrong_task_id() {
        final int expectedListSize = 1;
        final CreateBasicTask createTask = createBasicTaskInstance();
        final CreateBasicLabel createLabel = createBasicLabelInstance();
        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(getWrongTaskId(), labelId);
        client.create(createTask);
        client.create(createLabel);
        client.assignLabel(assignLabelToTask);
        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());
        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();
        assertEquals(expectedListSize, taskViews.size());

        final TaskView taskView = taskViews.get(0);

        assertEquals(taskId, taskView.getId());
        assertNotEquals(labelId, taskView.getLabelId());
    }

    @Test
    public void obtain_updated_task_description_from_labelled_tasks_view_when_handled_command_update_task_description() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        final CreateBasicLabel createLabel = createBasicLabelInstance();
        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        final int expectedListSize = 1;
        final String newDescription = UPDATED_TASK_DESCRIPTION;
        final UpdateTaskDescription updateTaskDescription = updateTaskDescriptionInstance(taskId, newDescription);
        client.create(createTask);
        client.create(createLabel);
        client.assignLabel(assignLabelToTask);
        client.update(updateTaskDescription);
        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();

        assertEquals(expectedListSize, tasksViewList.get(0)
                                                    .getLabelledTasks()
                                                    .getItemsCount());
        final TaskView taskView = tasksViewList.get(0)
                                               .getLabelledTasks()
                                               .getItems(0);

        assertEquals(labelId, taskView.getLabelId());
        assertEquals(taskId, taskView.getId());
        assertEquals(newDescription, taskView.getDescription());
    }

    @Test
    public void obtain_updated_task_priority_from_labelled_tasks_view_when_handled_command_update_task_priority() {
        final int expectedListSize = 1;
        final CreateBasicTask createTask = createBasicTaskInstance();
        final CreateBasicLabel createLabel = createBasicLabelInstance();
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance();
        final TaskPriority newPriority = TaskPriority.HIGH;
        final TaskId taskId = createTask.getId();
        final UpdateTaskPriority updateTaskPriority = updateTaskPriorityInstance(taskId, newPriority);
        client.create(createTask);
        client.create(createLabel);
        client.assignLabel(assignLabelToTask);
        client.update(updateTaskPriority);
        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());
        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();

        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(createLabel.getLabelId(), view.getLabelId());
        assertEquals(taskId, view.getId());
        assertEquals(newPriority, view.getPriority());
    }

    @Test
    public void obtain_updated_task_due_date_from_labelled_tasks_view_when_handled_command_update_task_due_date() {
        final int expectedListSize = 1;
        final CreateBasicTask createTask = createBasicTaskInstance();
        final CreateBasicLabel createLabel = createBasicLabelInstance();
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance();
        final Timestamp newDueDate = Timestamps.getCurrentTime();
        final TaskId taskId = createTask.getId();
        final UpdateTaskDueDate updateTaskDueDate = updateTaskDueDateInstance(taskId, newDueDate);
        client.create(createTask);
        client.create(createLabel);
        client.assignLabel(assignLabelToTask);
        client.update(updateTaskDueDate);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());
        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();

        assertEquals(expectedListSize, taskViews.size());

        final TaskView view = taskViews.get(0);

        assertEquals(createLabel.getLabelId(), view.getLabelId());
        assertEquals(taskId, view.getId());
        assertEquals(newDueDate, view.getDueDate());
    }

    @Test
    public void obtain_empty_labelled_tasks_view_when_handled_command_remove_label_from_task() {
        final int expectedListSize = 1;
        final CreateBasicTask createTask = createBasicTaskInstance();
        final CreateBasicLabel createLabel = createBasicLabelInstance();
        final TaskId taskId = createTask.getId();
        final TaskLabelId labelId = createLabel.getLabelId();
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        final RemoveLabelFromTask removeLabelFromTask = removeLabelFromTaskInstance(taskId, labelId);
        client.create(createTask);
        client.create(createLabel);
        client.assignLabel(assignLabelToTask);
        client.removeLabel(removeLabelFromTask);

        final List<LabelledTasksView> tasksViewList = client.getLabelledTasksView();
        assertEquals(expectedListSize, tasksViewList.size());
        final List<TaskView> taskViews = tasksViewList.get(0)
                                                      .getLabelledTasks()
                                                      .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    @Test
    public void obtain_empty_labelled_tasks_view_when_handled_command_delete_task() {
        final CreateBasicTask createTask = createBasicTaskInstance();
        final CreateBasicLabel createLabel = createBasicLabelInstance();
        final TaskLabelId labelId = createLabel.getLabelId();
        final TaskId taskId = createTask.getId();
        final DeleteTask deleteTask = deleteTaskInstance(taskId);
        final AssignLabelToTask assignLabelToTask = assignLabelToTaskInstance(taskId, labelId);
        client.create(createTask);
        client.create(createLabel);
        client.assignLabel(assignLabelToTask);
        client.delete(deleteTask);
        final List<TaskView> taskViews = client.getLabelledTasksView()
                                               .get(0)
                                               .getLabelledTasks()
                                               .getItemsList();
        assertTrue(taskViews.isEmpty());
    }
}