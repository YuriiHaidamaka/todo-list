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

package io.spine.examples.todolist.context;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import io.spine.examples.todolist.repository.DraftTasksViewRepository;
import io.spine.examples.todolist.repository.LabelAggregateRepository;
import io.spine.examples.todolist.repository.LabelledTasksViewRepository;
import io.spine.examples.todolist.repository.MyListViewRepository;
import io.spine.examples.todolist.repository.TaskLabelsRepository;
import io.spine.examples.todolist.repository.TaskRepository;
import io.spine.server.BoundedContext;
import io.spine.server.event.EventBus;
import io.spine.server.event.EventEnricher;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.kafka.KafkaStorageFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.server.storage.kafka.Consistency.STRONG;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * Utilities for creation the {@link BoundedContext} instances.
 *
 * @author Illia Shepilov
 * @author Dmytro Grankin
 */
public final class BoundedContexts {

    /** The default name of the {@code BoundedContext}. */
    private static final String KAFKA_PRODUCER_PROPS_PATH = "config/kafka-producer.properties";
    private static final String KAFKA_CONSUMER_PROPS_PATH = "config/kafka-consumer.properties";

    /** The name of the Bounded Context. */
    private static final String NAME = "TodoListBoundedContext";
    private static final StorageFactory IN_MEMORY_FACTORY =
            InMemoryStorageFactory.newInstance(NAME, false);

    private static final StorageFactory storageFactory;

    static {
        final Properties producerConfig = loadProperties(KAFKA_PRODUCER_PROPS_PATH);
        final Properties consumerConfig = loadProperties(KAFKA_CONSUMER_PROPS_PATH);
        storageFactory = new KafkaStorageFactory(producerConfig, consumerConfig, STRONG);
    }

    private BoundedContexts() {
        // Disable instantiation from outside.
    }

    /**
     * Creates the {@link BoundedContext} instance
     * using {@code InMemoryStorageFactory} for a single tenant.
     *
     * @return the {@link BoundedContext} instance
     */
    public static BoundedContext create() {
        final BoundedContext result = create(IN_MEMORY_FACTORY);
        return result;
    }

    /**
     * Creates a new instance of the {@link BoundedContext}
     * using the specified {@link StorageFactory}.
     *
     * @param storageFactory the storage factory to use
     * @return the bounded context created with the storage factory
     */
    public static BoundedContext create(StorageFactory storageFactory) {
        checkNotNull(storageFactory);
        final LabelAggregateRepository labelAggregateRepo = new LabelAggregateRepository();
        final TaskRepository taskRepo = new TaskRepository();
        final TaskLabelsRepository taskLabelsRepo = new TaskLabelsRepository();
        final MyListViewRepository myListViewRepo = new MyListViewRepository();
        final LabelledTasksViewRepository tasksViewRepo = new LabelledTasksViewRepository();
        final DraftTasksViewRepository draftTasksViewRepo = new DraftTasksViewRepository();

        final EventBus.Builder eventBus = createEventBus(storageFactory,
                                                         labelAggregateRepo,
                                                         taskRepo,
                                                         taskLabelsRepo);
        final BoundedContext boundedContext = createBoundedContext(eventBus);

        boundedContext.register(taskRepo);
        boundedContext.register(taskLabelsRepo);
        boundedContext.register(labelAggregateRepo);
        boundedContext.register(myListViewRepo);
        boundedContext.register(tasksViewRepo);
        boundedContext.register(draftTasksViewRepo);

        return boundedContext;
    }

    private static EventBus.Builder createEventBus(StorageFactory storageFactory,
                                                   LabelAggregateRepository labelRepo,
                                                   TaskRepository taskRepo,
                                                   TaskLabelsRepository labelsRepo) {
        final EventEnricher enricher = TodoListEnrichments.newBuilder()
                                                          .setLabelRepository(labelRepo)
                                                          .setTaskRepository(taskRepo)
                                                          .setTaskLabelsRepository(labelsRepo)
                                                          .build()
                                                          .createEnricher();
        final EventBus.Builder eventBus = EventBus.newBuilder()
                                                  .setEnricher(enricher)
                                                  .setStorageFactory(storageFactory);
        return eventBus;
    }

    @SuppressWarnings("Guava" /* Spine API is Java 7-based
                                 and uses `Optional` from Google Guava. */)
    @VisibleForTesting
    static BoundedContext createBoundedContext(EventBus.Builder eventBus) {
        final Optional<StorageFactory> storageFactory = eventBus.getStorageFactory();
        if (!storageFactory.isPresent()) {
            throw newIllegalStateException("EventBus does not specify a StorageFactory.");
        }

        return BoundedContext.newBuilder()
                             .setStorageFactorySupplier(storageFactory::get)
                             .setName(NAME)
                             .setEventBus(eventBus)
                             .build();
    }

    /**
     * Loads {@code .properties} file from the classpath by the given filename.
     *
     * <p>If the file is not found, an {@link NullPointerException} is thrown.
     */
    private static Properties loadProperties(String filename) {
        final ClassLoader loader = TodoListBoundedContext.class.getClassLoader();
        final InputStream rawProperties = loader.getResourceAsStream(filename);
        checkNotNull(rawProperties, "Could not load properties file %s from classpath.", filename);

        final Properties result = new Properties();

        try (InputStream props = rawProperties) {
            result.load(props);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    /** The holder for the singleton reference. */
    private enum Singleton {
        INSTANCE;

        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final BoundedContext value = create();
    }
}
