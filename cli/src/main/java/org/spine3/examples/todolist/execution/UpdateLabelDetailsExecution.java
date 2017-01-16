package org.spine3.examples.todolist.execution;

import org.spine3.examples.todolist.Settings;
import org.spine3.examples.todolist.UpdateLabelDetails;
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
    public String execute(Settings params) {
        final UpdateLabelDetails updateLabelDetails = UpdateLabelDetails.newBuilder()
                                                                        .setNewTitle(params.getTitle())
                                                                        .setColor(params.getColor())
                                                                        .setId(params.getLabelId())
                                                                        .build();
        client.update(updateLabelDetails);
        return LABEL_DETAILS_UPDATED_MESSAGE;
    }
}
