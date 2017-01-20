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

import asg.cliche.Command;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.UPDATED_LABLE_DETAILS_MESSAGE;
import static org.spine3.examples.todolist.modes.CommonMode.CommonModeConstants.UPDATED_PRIORITY_MESSAGE;
import static org.spine3.examples.todolist.modes.ModeHelper.sendMessageToUser;

/**
 * @author Illia Shepilov
 */
public class CommonMode extends Mode {

    CommonMode(TodoClient client, ConsoleReader reader) {
        super(client, reader);
    }

    @Override
    void start() {

    }

    @Command(abbrev = "2")
    public void updateTaskDescription() throws IOException {
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
        final String userFriendlyPrevDescr = previousDescription.isEmpty() ? DEFAULT_VALUE : previousDescription;
        final String message = String.format(UPDATED_DESCRIPTION_MESSAGE, userFriendlyPrevDescr, newDescription);
        sendMessageToUser(message);
    }

    @Command(abbrev = "3")
    public void updateTaskPriority() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final String priorityValue = obtainPriorityValue(ENTER_NEW_PRIORITY_MESSAGE);
        final TaskPriority newTaskPriority = TaskPriority.valueOf(priorityValue);
        final String previousPriorityValue = obtainPriorityValue(ENTER_PREVIOUS_PRIORITY_MESSAGE);
        final TaskPriority previousTaskPriority = TaskPriority.valueOf(previousPriorityValue);
        final PriorityChange change = PriorityChange.newBuilder()
                                                    .setPreviousValue(previousTaskPriority)
                                                    .setNewValue(newTaskPriority)
                                                    .build();
        final UpdateTaskPriority updateTaskPriority = UpdateTaskPriority.newBuilder()
                                                                        .setPriorityChange(change)
                                                                        .setId(taskId)
                                                                        .build();
        client.update(updateTaskPriority);
        final String message = String.format(UPDATED_PRIORITY_MESSAGE, previousPriorityValue, newTaskPriority);
        sendMessageToUser(message);
    }

    @Command(abbrev = "4")
    public void updateTaskDueDate() throws IOException, ParseException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final SimpleDateFormat simpleDateFormat = getDateFormat();
        final String newDueDateValue = obtainDueDateValue(ENTER_NEW_DATE_MESSAGE, true);
        final long newDueDateInMS = simpleDateFormat.parse(newDueDateValue)
                                                    .getTime();
        final Timestamp newDueDate = Timestamps.fromMillis(newDueDateInMS);
        final String previousDueDateValue = obtainDueDateValue(ENTER_PREVIOUS_DATE_MESSAGE, false);
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
    }

    @Command(abbrev = "5")
    public void updateLabelDetails() throws IOException {
        final String labelIdValue = obtainLabelIdValue();
        final TaskLabelId labelId = TaskLabelId.newBuilder()
                                               .setValue(labelIdValue)
                                               .build();
        final String newTitle = obtainLabelTitle(ENTER_NEW_TITLE_MESSAGE);
        final String previousTitle = obtainLabelTitle(ENTER_PREVIOUS_TITLE_MESSAGE);
        final String labelColorValue = obtainLabelColorValue(ENTER_NEW_COLOR_MESSAGE);
        final LabelColor newColor = LabelColor.valueOf(labelColorValue);
        final String previousColorValue = obtainLabelColorValue(ENTER_PREVIOUS_COLOR_MESSAGE);
        final LabelColor previousColor = LabelColor.valueOf(previousColorValue);

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
        final String message = String.format(UPDATED_LABLE_DETAILS_MESSAGE,
                                             previousColor, newColor, previousTitle, newTitle);
        sendMessageToUser(message);
    }

    @Command(abbrev = "6")
    public void deleteTask() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final DeleteTask deleteTask = DeleteTask.newBuilder()
                                                .setId(taskId)
                                                .build();
        client.delete(deleteTask);
    }

    @Command(abbrev = "7")
    public void reopenTask() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final ReopenTask reopenTask = ReopenTask.newBuilder()
                                                .setId(taskId)
                                                .build();
        client.reopen(reopenTask);
    }

    @Command(abbrev = "8")
    public void restoreTask() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final RestoreDeletedTask restoreDeletedTask = RestoreDeletedTask.newBuilder()
                                                                        .setId(taskId)
                                                                        .build();
        client.restore(restoreDeletedTask);
    }

    @Command(abbrev = "9")
    public void completeTask() throws IOException {
        final String taskIdValue = obtainTaskIdValue();
        final TaskId taskId = TaskId.newBuilder()
                                    .setValue(taskIdValue)
                                    .build();
        final CompleteTask completeTask = CompleteTask.newBuilder()
                                                      .setId(taskId)
                                                      .build();
        client.complete(completeTask);
    }

    @Command(abbrev = "10")
    public void assignLabel() throws IOException {
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

    @Command(abbrev = "11")
    public void removeLabel() throws IOException {
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

    static class CommonModeConstants {
        static final String EMPTY = "";
        final static String DEFAULT_VALUE = "default";
        static final String UPDATED_DESCRIPTION_MESSAGE = "The task description updated. %s --> %s";
        static final String UPDATED_PRIORITY_MESSAGE = "The task priority updated. %s --> %s";
        static final String UPDATED_DUE_DATE_MESSAGE = "The task due date updated. %s --> %s";
        static final String ENTER_NEW_DESCRIPTION_MESSAGE = "Please enter the new task description: ";
        static final String ENTER_PREVIOUS_DESCRIPTION_MESSAGE = "Please enter the previous task description: ";
        static final String ENTER_ID_MESSAGE = "Please enter the task id: ";
        static final String ENTER_NEW_PRIORITY_MESSAGE = "Please enter the new task priority: ";
        static final String ENTER_PREVIOUS_PRIORITY_MESSAGE = "Please enter the previous task priority: ";
        static final String ENTER_NEW_DATE_MESSAGE = "Please enter the new task due date: ";
        static final String ENTER_PREVIOUS_DATE_MESSAGE = "Please enter the previous task due date: ";
        static final String UPDATED_LABLE_DETAILS_MESSAGE = "The label details updated.\n" +
                "The label color: %s --> %s.\nThe label title: %s --> %s";
        static final String ENTER_NEW_TITLE_MESSAGE = "Please enter the new label title: ";
        static final String ENTER_PREVIOUS_TITLE_MESSAGE = "Please enter the previous label title: ";
        static final String ENTER_NEW_COLOR_MESSAGE = "Please enter the new label color: ";
        static final String ENTER_PREVIOUS_COLOR_MESSAGE = "Please enter the previous label color: ";
        final static String HELP_MESSAGE = "2:    Update the task description.\n" +
                "3:    Update the task priority.\n" +
                "4:    Update the task due date.\n" +
                "5:    Update the label details.\n" +
                "6:    Delete the task.\n" +
                "7:    Reopen the task.\n" +
                "8:    Restore the task.\n" +
                "9:    Complete the task.\n" +
                "10:   Assign the label to task.\n" +
                "11:   Remove the lable from task.\n";
    }
}
