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
import io.spine.test.integration.given.TaskCreationWizardTestEnv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.google.protobuf.util.Durations.fromSeconds;
import static com.google.protobuf.util.Timestamps.add;
import static io.spine.examples.todolist.LabelColor.BLUE;
import static io.spine.examples.todolist.LabelColor.GRAY;
import static io.spine.examples.todolist.LabelColor.GREEN;
import static io.spine.examples.todolist.LabelColor.RED;
import static io.spine.examples.todolist.TaskPriority.LOW;
import static io.spine.examples.todolist.TaskStatus.DRAFT;
import static io.spine.examples.todolist.TaskStatus.FINALIZED;
import static io.spine.test.integration.given.TaskCreationWizardTestEnv.newPid;
import static io.spine.test.integration.given.TaskCreationWizardTestEnv.newTaskId;
import static io.spine.time.Time.getCurrentTime;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Dmytro Dashenkov
 */
@DisplayName("TaskCreationWizard should")
class TaskCreationWizardTest extends AbstractIntegrationTest {

    private TodoClient client;
    private TaskCreationWizardTestEnv testEnv;

    @BeforeEach
    void before() {
        client = getClient();
        testEnv = TaskCreationWizardTestEnv.with(client);
    }

    @Test
    @DisplayName("supervise task creation")
    void firstCase() {
        final TaskCreationId pid = newPid();
        final TaskId taskId = newTaskId();
        testEnv.createDraft(pid, taskId);
        final String description = "firstCase";
        testEnv.setDetails(pid, description);
        testEnv.skipLabels(pid);
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
        testEnv.setDetails(pid, "secondCase");
        final LabelDetails redLabel = LabelDetails.newBuilder()
                                                  .setTitle("red label")
                                                  .setColor(RED)
                                                  .build();
        final LabelDetails greenLabel = LabelDetails.newBuilder()
                                                    .setTitle("green label")
                                                    .setColor(GREEN)
                                                    .build();
        final LabelDetails blueLabel = LabelDetails.newBuilder()
                                                   .setTitle("blue label")
                                                   .setColor(BLUE)
                                                   .build();
        final AddLabels addLabels = AddLabels.newBuilder()
                                             .setId(pid)
                                             .addNewLabels(redLabel)
                                             .addNewLabels(greenLabel)
                                             .addNewLabels(blueLabel)
                                             .build();
        client.postCommand(addLabels);
        assertAssignedLabel(taskId, redLabel.getTitle(), RED);
        assertAssignedLabel(taskId, greenLabel.getTitle(), GREEN);
        assertAssignedLabel(taskId, blueLabel.getTitle(), BLUE);
    }

    @Test
    @DisplayName("set all the optional fields")
    void thirdCase() {
        final TaskCreationId pid = newPid();
        final TaskId taskId = newTaskId();
        testEnv.createDraft(pid, taskId);

        final String description = "thirdCase";
        final TaskPriority priority = LOW;
        final Timestamp dueDate = add(getCurrentTime(), fromSeconds(100));
        testEnv.setDetails(pid, description, priority, dueDate);
        final String labelTitle = "thirdCase-label";
        final LabelId labelId = testEnv.createNewLabel(labelTitle);
        testEnv.addLabel(pid, labelId);
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
        testEnv.setDetails(pid, description);
        testEnv.cancel(pid);

        final Task actualTask = testEnv.taskById(taskId);
        assertEquals(DRAFT, actualTask.getTaskStatus());
    }

    private void assertAssignedLabel(TaskId taskId, String labelTitle, LabelColor labelColor) {
        final String color = LabelColorView.valueOf(labelColor);
        final boolean match = client.getLabelledTasksView()
                                    .stream()
                                    .filter(label -> labelTitle.equals(label.getLabelTitle()))
                                    .peek(label -> {
                                        final String actualColor = label.getLabelColor();
                                        assertTrue(color.equalsIgnoreCase(actualColor));
                                    })
                                    .flatMap(label -> label.getLabelledTasks()
                                                           .getItemsList()
                                                           .stream())
                                    .anyMatch(task -> taskId.equals(task.getId()));
        if (!match) {
            fail(format("Task %s has no label with title \"%s\" and color %s.",
                        taskId.getValue(), labelTitle, labelColor));
        }
    }
}
