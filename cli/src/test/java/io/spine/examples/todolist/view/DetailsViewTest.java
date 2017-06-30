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

package io.spine.examples.todolist.view;

import com.google.protobuf.StringValue;
import io.spine.examples.todolist.UserIoTest;
import io.spine.examples.todolist.action.Action;
import io.spine.examples.todolist.action.PseudoAction;
import io.spine.examples.todolist.action.Shortcut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static io.spine.examples.todolist.view.ActionListView.getBackShortcut;
import static io.spine.examples.todolist.view.ActionListView.getSelectActionMsg;
import static io.spine.protobuf.Wrapper.forString;
import static java.lang.System.lineSeparator;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

/**
 * @author Dmytro Grankin
 */
@DisplayName("DetailsView should")
class DetailsViewTest extends UserIoTest {

    private static final StringValue DETAILS_STATE = forString("string");

    private final Collection<Action> actions = singletonList(
            new PseudoAction("Act", new Shortcut("a")));
    private final DetailsView<StringValue> detailsView = new ADetailsView(actions, DETAILS_STATE);

    @BeforeEach
    void setUp() {
        detailsView.setUserCommunicator(getCommunicator());
    }

    @Test
    @DisplayName("display details before actions")
    void displayDetailsBeforeActions() {
        final Shortcut back = getBackShortcut();
        addAnswer(back.getValue());

        detailsView.display();

        final String stateRepresentation = detailsView.viewOf(DETAILS_STATE);
        final String actionsRepresentation = detailsView.getActions()
                                                        .stream()
                                                        .map(Action::toString)
                                                        .collect(joining(lineSeparator()));
        final String expectedRepresentation =
                stateRepresentation + lineSeparator() +
                        actionsRepresentation + lineSeparator() +
                        getSelectActionMsg() + lineSeparator();
        assertOutput(expectedRepresentation);
    }

    private static class ADetailsView extends DetailsView<StringValue> {

        private ADetailsView(Collection<Action> actions, StringValue state) {
            super(true, actions, state);
        }

        @Override
        protected String viewOf(StringValue state) {
            return state.getValue();
        }
    }
}
