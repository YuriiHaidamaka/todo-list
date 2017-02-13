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

package org.spine3.examples.todolist.modes;

import com.google.protobuf.Timestamp;
import com.google.protobuf.util.Timestamps;
import jline.console.ConsoleReader;
import org.spine3.change.StringChange;
import org.spine3.change.TimestampChange;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsChange;
import org.spine3.examples.todolist.PriorityChange;
import org.spine3.examples.todolist.TaskId;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.TaskPriority;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.c.commands.ReopenTask;
import org.spine3.examples.todolist.c.commands.RestoreDeletedTask;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.client.TodoClient;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.spine3.examples.todolist.DateHelper.getDateFormat;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.DEFAULT_VALUE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.EMPTY;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.ENTER_NEW_COLOR_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.ENTER_NEW_DATE_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.ENTER_NEW_DESCRIPTION_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.ENTER_NEW_PRIORITY_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.ENTER_NEW_TITLE_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.ENTER_PREVIOUS_COLOR_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.ENTER_PREVIOUS_DATE_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.ENTER_PREVIOUS_DESCRIPTION_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.ENTER_PREVIOUS_PRIORITY_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.ENTER_PREVIOUS_TITLE_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.UPDATED_DESCRIPTION_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.UPDATED_DUE_DATE_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.UPDATED_LABEL_DETAILS_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.UPDATED_PRIORITY_MESSAGE;
import static org.spine3.examples.todolist.modes.Mode.ModeConstants.LINE_SEPARATOR;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;

/**
 * @author Illia Shepilov
 */
abstract class CommonMode extends Mode {

    Map<String, Mode> modeMap;

    CommonMode(TodoClient client, ConsoleReader reader) {
        super(client, reader);
        initModeMap(client, reader);
    }

    private void initModeMap(TodoClient client, ConsoleReader reader) {
        modeMap = newHashMap();
        modeMap.put("2", new UpdateTaskDescriptionMode(client, reader));
        modeMap.put("3", new UpdateTaskPriorityMode(client, reader));
        modeMap.put("4", new UpdateTaskDueDateMode(client, reader));
        modeMap.put("5", new UpdateLabelDetailsMode(client, reader));
        modeMap.put("6", new DeleteTaskMode(client, reader));
        modeMap.put("7", new ReopenTaskMode(client, reader));
        modeMap.put("8", new RestoreTaskMode(client, reader));
        modeMap.put("9", new CompleteTaskMode(client, reader));
        modeMap.put("10", new AssignLabelToTaskMode(client, reader));
        modeMap.put("11", new RemoveLabelFromTaskMode(client, reader));
    }

    private static class UpdateTaskDescriptionMode extends Mode {

        private UpdateTaskDescriptionMode(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            final String taskIdValue = obtainTaskIdValue();
            final TaskId taskId = TaskId.newBuilder()
                                        .setValue(taskIdValue)
                                        .build();

            final String newDescription = obtainDescriptionValue(ENTER_NEW_DESCRIPTION_MESSAGE, true);
            final String previousDescription = obtainDescriptionValue(ENTER_PREVIOUS_DESCRIPTION_MESSAGE, false);
            final StringChange change = StringChange.newBuilder()
                                                    .setNewValue(newDescription)
                                                    .setPreviousValue(previousDescription)
                                                    .build();
            final UpdateTaskDescription updateTaskDescription = UpdateTaskDescription.newBuilder()
                                                                                     .setDescriptionChange(change)
                                                                                     .setId(taskId)
                                                                                     .build();
            client.update(updateTaskDescription);
            final String previousDescriptionValue = previousDescription.isEmpty() ? DEFAULT_VALUE : previousDescription;
            final String message = String.format(UPDATED_DESCRIPTION_MESSAGE, previousDescriptionValue, newDescription);
            sendMessageToUser(message);

        }
    }

    private static class UpdateTaskPriorityMode extends Mode {

        private UpdateTaskPriorityMode(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            final String taskIdValue = obtainTaskIdValue();
            final TaskId taskId = TaskId.newBuilder()
                                        .setValue(taskIdValue)
                                        .build();
            final TaskPriority newTaskPriority = obtainTaskPriority(ENTER_NEW_PRIORITY_MESSAGE);
            final TaskPriority previousTaskPriority = obtainTaskPriority(ENTER_PREVIOUS_PRIORITY_MESSAGE);
            final PriorityChange change = PriorityChange.newBuilder()
                                                        .setPreviousValue(previousTaskPriority)
                                                        .setNewValue(newTaskPriority)
                                                        .build();
            final UpdateTaskPriority updateTaskPriority = UpdateTaskPriority.newBuilder()
                                                                            .setPriorityChange(change)
                                                                            .setId(taskId)
                                                                            .build();
            client.update(updateTaskPriority);
            final String message = String.format(UPDATED_PRIORITY_MESSAGE, previousTaskPriority, newTaskPriority);
            sendMessageToUser(message);
        }
    }

    private static class UpdateTaskDueDateMode extends Mode {
        private UpdateTaskDueDateMode(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            try {
                final String taskIdValue = obtainTaskIdValue();
                final TaskId taskId = TaskId.newBuilder()
                                            .setValue(taskIdValue)
                                            .build();
                final SimpleDateFormat simpleDateFormat = getDateFormat();
                final String newDueDateValue = obtainDueDateValue(ENTER_NEW_DATE_MESSAGE, true);
                final long newDueDateInMS = simpleDateFormat.parse(newDueDateValue)
                                                            .getTime();
                final Timestamp newDueDate = Timestamps.fromMillis(newDueDateInMS);
                final String previousDueDateValue;
                previousDueDateValue = obtainDueDateValue(ENTER_PREVIOUS_DATE_MESSAGE, false);
                Timestamp previousDueDate = constructPreviousPriority(simpleDateFormat, previousDueDateValue);
                final TimestampChange change = TimestampChange.newBuilder()
                                                              .setPreviousValue(previousDueDate)
                                                              .setNewValue(newDueDate)
                                                              .build();
                final UpdateTaskDueDate updateTaskDueDate = UpdateTaskDueDate.newBuilder()
                                                                             .setDueDateChange(change)
                                                                             .setId(taskId)
                                                                             .build();
                client.update(updateTaskDueDate);
                final boolean isEmpty = previousDueDateValue.equals(EMPTY);
                final String previousDueDateForUser = isEmpty ? DEFAULT_VALUE : previousDueDateValue;
                final String message = String.format(UPDATED_DUE_DATE_MESSAGE, previousDueDateForUser, newDueDateValue);
                sendMessageToUser(message);
            } catch (ParseException e) {
                throw new ParseDateException(e);
            }
        }
    }

    protected static class UpdateLabelDetailsMode extends Mode {

        private UpdateLabelDetailsMode(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            final String labelIdValue = obtainLabelIdValue();
            final TaskLabelId labelId = TaskLabelId.newBuilder()
                                                   .setValue(labelIdValue)
                                                   .build();
            final String newTitle = obtainLabelTitle(ENTER_NEW_TITLE_MESSAGE);
            final String previousTitle = obtainLabelTitle(ENTER_PREVIOUS_TITLE_MESSAGE);
            final LabelColor newColor = obtainLabelColor(ENTER_NEW_COLOR_MESSAGE);
            final LabelColor previousColor = obtainLabelColor(ENTER_PREVIOUS_COLOR_MESSAGE);

            final LabelDetails newLabelDetails = LabelDetails.newBuilder()
                                                             .setTitle(newTitle)
                                                             .setColor(newColor)
                                                             .build();
            final LabelDetails previousLabelDetails = LabelDetails.newBuilder()
                                                                  .setTitle(previousTitle)
                                                                  .setColor(previousColor)
                                                                  .build();
            final LabelDetailsChange change = LabelDetailsChange.newBuilder()
                                                                .setNewDetails(newLabelDetails)
                                                                .setPreviousDetails(previousLabelDetails)
                                                                .build();
            final UpdateLabelDetails updateLabelDetails = UpdateLabelDetails.newBuilder()
                                                                            .setId(labelId)
                                                                            .setLabelDetailsChange(change)
                                                                            .setId(labelId)
                                                                            .build();
            client.update(updateLabelDetails);
            final String message = String.format(UPDATED_LABEL_DETAILS_MESSAGE,
                                                 previousColor, newColor, previousTitle, newTitle);
            sendMessageToUser(message);
        }
    }

    private static class DeleteTaskMode extends Mode {

        private DeleteTaskMode(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            final String taskIdValue = obtainTaskIdValue();
            final TaskId taskId = TaskId.newBuilder()
                                        .setValue(taskIdValue)
                                        .build();
            final DeleteTask deleteTask = DeleteTask.newBuilder()
                                                    .setId(taskId)
                                                    .build();
            client.delete(deleteTask);
        }
    }

    private static class ReopenTaskMode extends Mode {

        private ReopenTaskMode(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            final String taskIdValue = obtainTaskIdValue();
            final TaskId taskId = TaskId.newBuilder()
                                        .setValue(taskIdValue)
                                        .build();
            final ReopenTask reopenTask = ReopenTask.newBuilder()
                                                    .setId(taskId)
                                                    .build();

            client.reopen(reopenTask);
        }
    }

    private static class RestoreTaskMode extends Mode {

        private RestoreTaskMode(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            final String taskIdValue = obtainTaskIdValue();
            final TaskId taskId = TaskId.newBuilder()
                                        .setValue(taskIdValue)
                                        .build();
            final RestoreDeletedTask restoreDeletedTask = RestoreDeletedTask.newBuilder()
                                                                            .setId(taskId)
                                                                            .build();
            client.restore(restoreDeletedTask);
        }
    }

    private static class CompleteTaskMode extends Mode {

        private CompleteTaskMode(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            final String taskIdValue = obtainTaskIdValue();
            final TaskId taskId = TaskId.newBuilder()
                                        .setValue(taskIdValue)
                                        .build();
            final CompleteTask completeTask = CompleteTask.newBuilder()
                                                          .setId(taskId)
                                                          .build();
            client.complete(completeTask);
        }
    }

    private static class AssignLabelToTaskMode extends Mode {

        private AssignLabelToTaskMode(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            final String taskIdValue = obtainTaskIdValue();
            final TaskId taskId = TaskId.newBuilder()
                                        .setValue(taskIdValue)
                                        .build();
            final String labelIdValue = obtainLabelIdValue();
            final TaskLabelId labelId = TaskLabelId.newBuilder()
                                                   .setValue(labelIdValue)
                                                   .build();
            final AssignLabelToTask assignLabelToTask = AssignLabelToTask.newBuilder()
                                                                         .setId(taskId)
                                                                         .setLabelId(labelId)
                                                                         .build();
            client.assignLabel(assignLabelToTask);
        }
    }

    private static class RemoveLabelFromTaskMode extends Mode {

        private RemoveLabelFromTaskMode(TodoClient client, ConsoleReader reader) {
            super(client, reader);
        }

        @Override
        void start() throws IOException {
            final String taskIdValue = obtainTaskIdValue();
            final TaskId taskId = TaskId.newBuilder()
                                        .setValue(taskIdValue)
                                        .build();
            final String labelIdValue = obtainLabelIdValue();
            final TaskLabelId labelId = TaskLabelId.newBuilder()
                                                   .setValue(labelIdValue)
                                                   .build();
            final RemoveLabelFromTask removeLabelFromTask = RemoveLabelFromTask.newBuilder()
                                                                               .setId(taskId)
                                                                               .setLabelId(labelId)
                                                                               .build();
            client.removeLabel(removeLabelFromTask);
        }
    }

    static class CommonModeConstants {
        static final String EMPTY = "";
        static final String DEFAULT_VALUE = "default";
        static final String UPDATED_DESCRIPTION_MESSAGE = "The task description updated. %s --> %s";
        static final String UPDATED_PRIORITY_MESSAGE = "The task priority updated. %s --> %s";
        static final String UPDATED_DUE_DATE_MESSAGE = "The task due date updated. %s --> %s";
        static final String ENTER_NEW_DESCRIPTION_MESSAGE = "Please enter the new task description: ";
        static final String ENTER_PREVIOUS_DESCRIPTION_MESSAGE = "Please enter the previous task description: ";
        static final String ENTER_ID_MESSAGE = "Please enter the task ID: ";
        static final String ENTER_NEW_PRIORITY_MESSAGE = "Please enter the new task priority: ";
        static final String ENTER_PREVIOUS_PRIORITY_MESSAGE = "Please enter the previous task priority: ";
        static final String ENTER_NEW_DATE_MESSAGE = "Please enter the new task due date: ";
        static final String ENTER_PREVIOUS_DATE_MESSAGE = "Please enter the previous task due date: ";
        static final String UPDATED_LABEL_DETAILS_MESSAGE = "The label details updated." + LINE_SEPARATOR +
                "The label color: %s --> %s." + LINE_SEPARATOR +
                "The label title: %s --> %s";
        static final String ENTER_NEW_TITLE_MESSAGE = "Please enter the new label title: ";
        static final String ENTER_PREVIOUS_TITLE_MESSAGE = "Please enter the previous label title: ";
        static final String ENTER_NEW_COLOR_MESSAGE = "Please enter the new label color: ";
        static final String ENTER_PREVIOUS_COLOR_MESSAGE = "Please enter the previous label color: ";
        static final String HELP_MESSAGE = "2:    Update the task description." + LINE_SEPARATOR +
                "3:    Update the task priority." + LINE_SEPARATOR +
                "4:    Update the task due date." + LINE_SEPARATOR +
                "5:    Update the label details." + LINE_SEPARATOR +
                "6:    Delete the task." + LINE_SEPARATOR +
                "7:    Reopen the task." + LINE_SEPARATOR +
                "8:    Restore the task." + LINE_SEPARATOR +
                "9:    Complete the task." + LINE_SEPARATOR +
                "10:   Assign the label to task." + LINE_SEPARATOR +
                "11:   Remove the label from task.";

        private CommonModeConstants() {
        }
    }
}
