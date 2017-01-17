package org.spine3.examples.todolist.execution;

import org.spine3.examples.todolist.LabelDetails;
import org.spine3.examples.todolist.LabelDetailsChange;
import org.spine3.examples.todolist.Parameters;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.client.TodoClient;

/**
 * @author Illia Shepilov
 */
public class UpdateLabelDetailsExecution implements Executable {

    private final TodoClient client;
    private static final String LABEL_DETAILS_UPDATED_MESSAGE = "Label details updated.";

    public UpdateLabelDetailsExecution(TodoClient client) {
        this.client = client;
    }

    @Override
    public String execute(Parameters params) {
        final LabelDetails labelDetails = LabelDetails.newBuilder()
                                                      .setTitle(params.getTitle())
                                                      .setColor(params.getColor())
                                                      .build();
        final LabelDetailsChange labelDetailsChange = LabelDetailsChange.newBuilder()
                                                                        .setNewDetails(labelDetails)
                                                                        .build();
        final UpdateLabelDetails updateLabelDetails = UpdateLabelDetails.newBuilder()
                                                                        .setLabelDetailsChange(labelDetailsChange)
                                                                        .setId(params.getLabelId())
                                                                        .build();
        client.update(updateLabelDetails);
        return LABEL_DETAILS_UPDATED_MESSAGE;
    }
}
