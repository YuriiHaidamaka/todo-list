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

package org.spine3.examples.todolist.c.aggregates;

import com.google.protobuf.Message;
import org.spine3.change.ValueMismatch;
import org.spine3.examples.todolist.LabelColor;
import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsChange;
import org.spine3.examples.todolist.TaskLabel;
import org.spine3.examples.todolist.TaskLabelId;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.c.events.LabelCreated;
import org.spine3.examples.todolist.c.events.LabelDetailsUpdated;
import org.spine3.examples.todolist.c.failures.CannotUpdateLabelDetails;
import org.spine3.server.aggregate.Aggregate;
import org.spine3.server.aggregate.Apply;
import org.spine3.server.command.Assign;

import java.util.Collections;
import java.util.List;

import static org.spine3.examples.todolist.c.aggregates.MismatchHelper.of;

/**
 * The aggregates managing the state of a {@link TaskLabel}.
 *
 * @author Illia Shepilov
 */

@SuppressWarnings({"unused"}) // The methods annotated with {@link Assign} are declared {@code private} by design.
public class TaskLabelAggregate extends Aggregate<TaskLabelId, TaskLabel, TaskLabel.Builder> {

    /**
     * Creates a new aggregates instance.
     *
     * @param id the ID for the new aggregates.
     * @throws IllegalArgumentException if the ID is not of one of the supported types.
     */
    public TaskLabelAggregate(TaskLabelId id) {
        super(id);
    }

    @Assign
    public List<? extends Message> handle(CreateBasicLabel cmd) {
        final LabelDetails.Builder labelDetails = LabelDetails.newBuilder()
                                                              .setColor(LabelColor.GRAY)
                                                              .setTitle(cmd.getLabelTitle());
        final LabelCreated result = LabelCreated.newBuilder()
                                                .setId(cmd.getLabelId())
                                                .setDetails(labelDetails)
                                                .build();
        return Collections.singletonList(result);
    }

    @Assign
    public List<? extends Message> handle(UpdateLabelDetails cmd) throws CannotUpdateLabelDetails {
        final TaskLabel state = getState();
        final LabelDetails actualLabelDetails = LabelDetails.newBuilder()
                                                            .setColor(getState().getColor())
                                                            .setTitle(getState().getTitle())
                                                            .build();
        final LabelDetailsChange labelDetailsChange = cmd.getLabelDetailsChange();
        final LabelDetails expectedLabelDetails = labelDetailsChange.getPreviousDetails();

        final boolean isEquals = actualLabelDetails.equals(expectedLabelDetails);
        final TaskLabelId labelId = cmd.getId();

        if (!isEquals) {
            final LabelDetails newLabelDetails = labelDetailsChange.getNewDetails();
            final ValueMismatch mismatch = of(expectedLabelDetails, actualLabelDetails, newLabelDetails, getVersion());
            FailureHelper.throwCannotUpdateLabelDetailsFailure(labelId, mismatch);
        }

        final LabelDetailsUpdated labelDetailsUpdated = LabelDetailsUpdated.newBuilder()
                                                                           .setLabelId(labelId)
                                                                           .setLabelDetailsChange(labelDetailsChange)
                                                                           .build();
        final List<? extends Message> result = Collections.singletonList(labelDetailsUpdated);
        return result;
    }

    @Apply
    private void labelCreated(LabelCreated event) {
        getBuilder().setId(event.getId())
                    .setTitle(event.getDetails()
                                   .getTitle())
                    .setColor(LabelColor.GRAY);
    }

    @Apply
    private void labelDetailsUpdated(LabelDetailsUpdated event) {
        final LabelDetails labelDetails = event.getLabelDetailsChange()
                                               .getNewDetails();
        getBuilder().setTitle(labelDetails.getTitle())
                    .setColor(labelDetails.getColor());
    }
}
