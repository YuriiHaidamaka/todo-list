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
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.q.projection.DraftTasksView;
import org.spine3.examples.todolist.q.projection.LabelledTasksView;
import org.spine3.examples.todolist.q.projection.TaskView;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Illia Shepilov
 */
@DisplayName("After execution CreateDraft command")
public class CreateDraftTest extends CommandLineTodoClientTest {

    private TodoClient client;

    @BeforeEach
    @Override
    public void setUp() throws InterruptedException {
        super.setUp();
        client = getClient();
    }

    @Test
    @DisplayName("DraftTasksView should contain the task view")
    public void obtainDraftView() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final DraftTasksView draftTasksView = client.getDraftTasksView();
        final List<TaskView> taskViewList = draftTasksView.getDraftTasks()
                                                          .getItemsList();
        assertEquals(1, taskViewList.size());
        assertEquals(createDraft.getId(), taskViewList.get(0)
                                                      .getId());
    }

    @Test
    @DisplayName("LabelledTasksView should not contain the task view")
    public void obtainLabelledView() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final List<LabelledTasksView> labelledTasksView = client.getLabelledTasksView();
        assertTrue(labelledTasksView.isEmpty());
    }

    @Test
    @DisplayName("MyListView should not contain task view")
    public void ObtainMyListView() {
        final CreateDraft createDraft = createDraft();
        client.create(createDraft);

        final List<TaskView> taskViews = client.getMyListView()
                                                    .getMyList()
                                                    .getItemsList();
        assertTrue(taskViews.isEmpty());
    }
}
