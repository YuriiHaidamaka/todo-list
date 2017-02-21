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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.q.projection.LabelledTasksView;
import org.spine3.examples.todolist.q.projection.TaskView;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.spine3.examples.todolist.testdata.TestTaskCommandFactory.DESCRIPTION;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution of CreateBasicTask command")
public class CreateBasicTaskTest extends CommandLineTodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }
    
    @Test
    @DisplayName("LabelledTasksView should be empty")
    public void obtainEmptyLabelledTasksView() {
        final CreateBasicTask createBasicTask = createBasicTask();
        client.create(createBasicTask);

        final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
        assertTrue(labelledTasksView.isEmpty());
    }

    @Test
    @DisplayName("DraftTaskView should be empty")
    public void obtainEmptyDraftViewList() {
        final CreateBasicTask createBasicTask = createBasicTask();
        client.create(createBasicTask);

        final List<TaskView> taskViews = client.getDraftTasksView()
                                                    .getDraftTasks()
                                                    .getItemsList();
        assertTrue(taskViews.isEmpty());
    }

    @Test
    @DisplayName("MyListView should contain the created task")
    public void obtainMyListView() {
        final CreateBasicTask createFirstTask = createBasicTask();
        client.create(createFirstTask);

        final CreateBasicTask createSecondTask = createBasicTask();
        client.create(createSecondTask);

        final List<TaskView> taskViews = client.getMyListView()
                                                    .getMyList()
                                                    .getItemsList();
        assertEquals(2, taskViews.size());

        final TaskView firstView = taskViews.get(0);
        final TaskView secondView = taskViews.get(1);
        final List<TaskId> taskIds = new ArrayList<>(2);
        taskIds.add(firstView.getId());
        taskIds.add(secondView.getId());

        assertTrue(taskIds.contains(createFirstTask.getId()));
        assertTrue(taskIds.contains(createSecondTask.getId()));
        assertEquals(DESCRIPTION, firstView.getDescription());
        assertEquals(DESCRIPTION, secondView.getDescription());
    }
}
