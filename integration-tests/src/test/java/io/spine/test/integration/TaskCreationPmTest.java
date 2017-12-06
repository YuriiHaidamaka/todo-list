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

package io.spine.test.integration;

import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskCreationId;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskPriority;
import io.spine.examples.todolist.c.commands.AddLabels;
import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.q.projection.LabelColorView;
import io.spine.test.AbstractIntegrationTest;
import io.spine.test.integration.given.TaskCreationPmTestEnv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.protobuf.util.Durations.fromSeconds;
import static com.google.protobuf.util.Timestamps.add;
import static io.spine.examples.todolist.LabelColor.GRAY;
import static io.spine.examples.todolist.LabelColor.RED;
import static io.spine.examples.todolist.TaskPriority.LOW;
import static io.spine.examples.todolist.TaskStatus.DRAFT;
import static io.spine.examples.todolist.TaskStatus.FINALIZED;
import static io.spine.test.integration.given.TaskCreationPmTestEnv.newPid;
import static io.spine.test.integration.given.TaskCreationPmTestEnv.newTaskId;
import static io.spine.time.Time.getCurrentTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Dmytro Dashenkov
 */
@DisplayName("TaskCreationProcessManager should")
class TaskCreationPmTest extends AbstractIntegrationTest {

    private TodoClient client;
    private TaskCreationPmTestEnv testEnv;

    @BeforeEach
    void before() {
        client = getClient();
        testEnv = TaskCreationPmTestEnv.with(client);
    }

    @Test
    @DisplayName("supervise task creation")
    void firstCase() {
        final TaskCreationId pid = newPid();
        final TaskId taskId = newTaskId();
        testEnv.createDraft(pid, taskId);
        final String description = "firstCase";
        testEnv.setDescription(pid, description);
        testEnv.complete(pid);

        final Task actualTask = testEnv.taskById(taskId);
        assertEquals(FINALIZED, actualTask.getTaskStatus());
        assertEquals(description, actualTask.getDescription().getValue());
    }

    @Test
    @DisplayName("create and assign new labels")
    void secondCase() {
        final TaskCreationId pid = newPid();
        final TaskId taskId = newTaskId();
        testEnv.createDraft(pid, taskId);
        final String labelTitle = "red label";
        final LabelDetails newLabel = LabelDetails.newBuilder()
                                                  .setTitle(labelTitle)
                                                  .setColor(RED)
                                                  .build();
        final AddLabels addLabels = AddLabels.newBuilder()
                                             .setId(pid)
                                             .addNewLabels(newLabel)
                                             .build();
        client.postCommand(addLabels);
        assertAssignedLabel(taskId, labelTitle, RED);
    }

    @Test
    @DisplayName("set all the optional fields")
    void thirdCase() {
        final TaskCreationId pid = newPid();
        final TaskId taskId = newTaskId();
        testEnv.createDraft(pid, taskId);

        final String description = "thirdCase";
        testEnv.setDescription(pid, description);
        final String labelTitle = "thirdCase-label";
        final LabelId labelId = testEnv.createNewLabel(labelTitle);
        testEnv.addLabel(pid, labelId);
        final TaskPriority priority = LOW;
        testEnv.setPriority(pid, priority);
        final Timestamp dueDate = add(getCurrentTime(), fromSeconds(100));
        testEnv.setDueDate(pid, dueDate);
        testEnv.complete(pid);

        final Task task = testEnv.taskById(taskId);
        assertEquals(description, task.getDescription().getValue());
        assertEquals(priority, task.getPriority());
        assertAssignedLabel(taskId, labelTitle, GRAY);
    }

    @Test
    @DisplayName("cancel the process")
    void forthCase() {
        final TaskCreationId pid = newPid();
        final TaskId taskId = newTaskId();
        testEnv.createDraft(pid, taskId);
        final String description = "forthCase";
        testEnv.setDescription(pid, description);
        testEnv.cancel(pid);

        final Task actualTask = testEnv.taskById(taskId);
        assertEquals(DRAFT, actualTask.getTaskStatus());
    }

    private void assertAssignedLabel(TaskId taskId, String labelTitle, LabelColor labelColor) {
        final String color = LabelColorView.valueOf(labelColor);
        client.getLabelledTasksView()
              .stream()
              .filter(label -> labelTitle.equals(label.getLabelTitle()))
              .peek(label -> assertTrue(color.equalsIgnoreCase(label.getLabelColor())))
              .flatMap(label -> label.getLabelledTasks()
                                     .getItemsList()
                                     .stream())
              .filter(task -> taskId.equals(task.getId()))
              .findAny();
    }
}
