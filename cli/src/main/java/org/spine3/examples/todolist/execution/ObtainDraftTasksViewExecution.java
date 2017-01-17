package org.spine3.examples.todolist.execution;

import org.spine3.examples.todolist.Parameters;
import org.spine3.examples.todolist.client.TodoClient;
import org.spine3.examples.todolist.view.DraftTasksView;

/**
 * @author Illia Shepilov
 */
public class ObtainDraftTasksViewExecution implements Executable {

    private final TodoClient client;

    public ObtainDraftTasksViewExecution(TodoClient client) {
        this.client = client;
    }

    @Override
    public String execute(Parameters params) {
        final DraftTasksView draftTasksView = client.getDraftTasksView();
        final String result = draftTasksView.toString();
        return null;
    }
}
