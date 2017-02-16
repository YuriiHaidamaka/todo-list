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

package org.spine3.examples.todolist.context;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import org.spine3.examples.todolist.repository.DraftTasksViewRepository;
import org.spine3.examples.todolist.repository.LabelAggregateRepository;
import org.spine3.examples.todolist.repository.LabelledTasksViewRepository;
import org.spine3.examples.todolist.repository.MyListViewRepository;
import org.spine3.examples.todolist.repository.TaskDefinitionRepository;
import org.spine3.examples.todolist.repository.TaskLabelsRepository;
import org.spine3.server.BoundedContext;
import org.spine3.server.storage.StorageFactorySwitch;
import org.spine3.server.storage.memory.InMemoryStorageFactory;
import org.spine3.util.Environment;

/**
 * Serves for creation the {@link BoundedContext} instances.
 *
 * @author Illia Shepilov
 */
public class TodoListBoundedContext {

    /** The name of the Bounded Context. */
    private static final String NAME = "TodoListBoundedContext";

    /**
     * Obtains the reference to the singleton {@link BoundedContext}.
     */
    public static BoundedContext getInstance() {
        return Singleton.INSTANCE.value;
    }

    /**
     * Creates and returns the {@link BoundedContext} instance.
     *
     * <p>Serves only for test needs.
     *
     * @return the {@link BoundedContext} instance
     */
    @VisibleForTesting
    public static BoundedContext createTestInstance() {
        final BoundedContext result = create();
        return result;
    }

    private TodoListBoundedContext() {
        // Disable instantiation from outside.
    }

    /**
     * Creates a new instance of the Bounded Context.
     */
    private static BoundedContext create() {
        final BoundedContext boundedContext = createBoundedContext();

        final TaskDefinitionRepository taskDefinitionRepo = new TaskDefinitionRepository(boundedContext);
        final LabelAggregateRepository labelAggregateRepo = new LabelAggregateRepository(boundedContext);
        final TaskLabelsRepository taskLabelsRepo = new TaskLabelsRepository(boundedContext);
        final MyListViewRepository myListViewRepo = new MyListViewRepository(boundedContext);
        final LabelledTasksViewRepository tasksViewRepo = new LabelledTasksViewRepository(boundedContext);
        final DraftTasksViewRepository draftTasksViewRepo = new DraftTasksViewRepository(boundedContext);

        boundedContext.register(taskDefinitionRepo);
        boundedContext.register(taskLabelsRepo);
        boundedContext.register(labelAggregateRepo);
        boundedContext.register(myListViewRepo);
        boundedContext.register(tasksViewRepo);
        boundedContext.register(draftTasksViewRepo);

        TodoListEnrichmentConfiguration.newBuilder()
                                       .setLabelRepository(labelAggregateRepo)
                                       .setTaskDefinitionRepository(taskDefinitionRepo)
                                       .setTaskLabelsRepository(taskLabelsRepo)
                                       .apply(boundedContext.getEventBus())
                                       .addEnrichmentFields();
        return boundedContext;
    }

    private static BoundedContext createBoundedContext() {
        final BoundedContext.Builder result = BoundedContext.newBuilder()
                                                            .setName(NAME);
        final boolean testEnv = Environment.getInstance()
                                           .isTests();
        if (!testEnv) {
            final StorageFactorySwitch factorySwitch =
                    StorageFactorySwitch.init(Suppliers.ofInstance(InMemoryStorageFactory.getInstance()),
                                              Suppliers.ofInstance(InMemoryStorageFactory.getInstance()));
            //TODO:2017-02-16:illiashepilov: think about injection storage factory switch
            result.setStorageFactorySupplier(factorySwitch);
        }

        return result.build();
    }

    /** The holder for the singleton reference. */
    private enum Singleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final BoundedContext value = create();
    }
}
