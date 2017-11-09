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

package io.spine.server.catchup;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;
import io.spine.core.Event;
import io.spine.core.EventClass;
import io.spine.core.EventEnvelope;
import io.spine.core.MessageEnvelope;
import io.spine.server.event.EventDispatcher;
import io.spine.server.kafka.KafkaStreamsConfigs;
import io.spine.server.projection.ProjectionRepository;
import io.spine.server.storage.kafka.KafkaWrapper;
import io.spine.server.storage.kafka.Topic;
import io.spine.type.TypeName;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.server.storage.kafka.MessageSerializer.deserializer;
import static io.spine.server.storage.kafka.MessageSerializer.serializer;
import static java.lang.String.format;
import static org.apache.kafka.common.serialization.Serdes.serdeFrom;

/**
 * A utility for configuring the {@linkplain ProjectionRepository projection repository} catch up
 * basing on Kafka.
 *
 * <p>The general flow of the catch up is following:
 * <ol>
 *     <li>An event is dispatched by the {@link io.spine.server.event.EventBus EventBus} into
 *     the {@link KafkaEventDispatcher}.
 *     <li>The dispatcher publishes the event into a dedicated Kafka {@linkplain Topic topic} with
 *     name {@code R.spine.core.Event}.
 *     <li>All the events are grouped by the target {@code ProjectionRepository} and dispatched
 *     into the repository synchronously.
 *     <li>The repository handles the events on its own.
 * </ol>
 *
 * @author Dmytro Dashenkov
 */
public final class KafkaCatchUp {

    /**
     * The Kafka Streams {@link org.apache.kafka.streams.kstream.Window Window} size.
     *
     * @see #windows
     */
    private static final long WINDOW_SIZE_MS = 2000;

    private static final Topic EVENT_TOPIC = Topic.ofValue("R.spine.core.Event");

    /**
     * The Kafka Streams {@linkplain Windows Window factory}.
     *
     * <p>This factory is used to window the events during the catch up.
     *
     * <p>The events are widowed by non-overlapping 2 second windows and then processed. This
     * allows us to reduce the time lap between the events which were produced chronologically
     * closely, but, for some reason, arrive to the processing topology with a more significant
     * time distance.
     */
    private static final Windows<?> windows = TimeWindows.of(WINDOW_SIZE_MS);

    private KafkaCatchUp() {
        // Prevent utility class instantiation.
    }

    /**
     * Starts the Kafka-based catch up of the given {@link ProjectionRepository}.
     *
     * <p>Call this method on the server start up for all the Projection Repositories or when
     * setting up a repository of a new projection type.
     *
     * <p>This method builds and starts a
     * <a href="https://kafka.apache.org/documentation/streams/">Kafka Stream topology</a>.
     * It's recommended that the source topic with name {@code R.spine.core.Event}
     * exists before this method is called; otherwise the catch up process might be delayed while
     * Kafka is creating the topic. The new topology may spawn intermediate topics.
     *
     * <p>As soon as the topology is started, the events from the write side will be dispatched to
     * read side.
     *
     * <p>In case if you are adding a new Projection type to the system, no specific steps are
     * required. The newly created topology will start processing from the first event found in
     * {@code R.spine.core.Event} topic.
     *
     * <p>The passed {@code streamConfig} should not contain {@code application.id} attribute, as
     * it's assigned automatically depending on the repository type.
     *
     * <p>It may take a few seconds before the Kafka Streams topology starts serving. Do not rely
     * on an immediate start.
     *
     * <p>It is required to add {@link KafkaEventDispatcher} to
     * the {@link io.spine.server.event.EventBus EventBus} to make the Kafka catch up work.
     *
     * @param repository   the {@link ProjectionRepository} to catch up
     * @param streamConfig the Kafka Streams configuration containing {@code bootstrap.servers}
     *                     property and (optionally) other Streams configs
     * @see KafkaEventDispatcher
     */
    public static void start(ProjectionRepository<?, ?, ?> repository, Properties streamConfig) {
        checkNotNull(repository);
        final String repositoryKey = repositoryKey(repository);
        final Properties config = KafkaStreamsConfigs.prepareConfig(streamConfig, repositoryKey);
        doStart(repository, repositoryKey, config);
    }

    private static void doStart(ProjectionRepository<?, ?, ?> repository,
                                String repositoryKey,
                                Properties streamConfig) {
        final Set<EventClass> handledClasses = repository.getMessageClasses();
        final StreamsBuilder builder = new StreamsBuilder();
        final Serde<Message> messageSerde = serdeFrom(serializer(), deserializer());
        final KStream<Message, Message> stream = builder.stream(EVENT_TOPIC.getName(),
                                                                Consumed.with(messageSerde,
                                                                              messageSerde));
        stream.filter((key, value) -> handledClasses.contains(EventClass.of(value)))
              .map((key, value) -> new KeyValue<>(repositoryKey, value))
              .groupByKey(Serialized.with(Serdes.String(), messageSerde))
              .windowedBy(windows)
              .aggregate(KafkaCatchUp::voidInstance,
                         (key, value, aggregate) -> dispatchEvent(repository, (Event) value),
                         Materialized.with(VoidSerde.cast(), VoidSerde.cast()));
        final KafkaStreams streams = new KafkaStreams(builder.build(), streamConfig);
        streams.start();
        log().info("Starting catch up for {} projection.", repositoryKey);
    }

    /**
     * Dispatches the given event with the given {@link ProjectionRepository}.
     *
     * <p>This method should only be called for the side effect, as it always returns
     * {@link #voidInstance()}.
     *
     * @param repo  the {@link ProjectionRepository} to dispatch the event with
     * @param event the event to dispatch
     * @return {@link #voidInstance()}
     */
    @Nullable
    private static Void dispatchEvent(ProjectionRepository<?, ?, ?> repo, Event event) {
        final EventEnvelope envelope = EventEnvelope.of(event);
        repo.dispatch(envelope);
        log().info("Dispatched event {} (ID: {}) with {}.",
                   TypeName.of(envelope.getMessage()),
                   envelope.getId().getValue(),
                   repo.getClass().getSimpleName());
        return voidInstance();
    }

    private static String repositoryKey(ProjectionRepository<?, ?, ?> repository) {
        final String typeName = repository.getEntityStateType().getTypeName();
        return typeName;
    }

    /**
     * Returns {@code null}; performs no action.
     *
     * <p>Use this method as a stub when an instance of {@link Void} is required.
     *
     * @return always {@code null}
     */
    @Nullable
    private static Void voidInstance() {
        return null;
    }

    /**
     * Creates an event dispatcher publishing all the retrieved events into Kafka
     * {@code R.spine.core.Event} topic.
     *
     * <p>It's required to {@linkplain io.spine.server.event.EventBus#register register} this
     * dispatcher in your {@code EventBus} to make Kafka-based catch up work properly.
     *
     * <p>Also, consider {@linkplain io.spine.server.event.EventBus#unregister unregistering} all
     * the other dispatchers (like the {@link ProjectionRepository} instances).
     *
     * @param messageClasses the types of the events consumed by this dispatcher;
     *                       should be all the types applied to the caught up projections
     * @param kafka          the {@link KafkaWrapper} instance used to publish the events
     * @param <I>            the type parameter of the returned {@link EventDispatcher}; could be
     *                       anything, as the {@link EventDispatcher#dispatch(MessageEnvelope)}
     *                       always returns an empty collection
     * @return new instance of the Kafka event dispatcher
     */
    @SuppressWarnings("unchecked") // OK since the dispatcher never produces any IDs.
    public static <I> EventDispatcher<I> dispatcher(Collection<EventClass> messageClasses,
                                                    KafkaWrapper kafka) {
        return (EventDispatcher<I>) new KafkaEventDispatcher(messageClasses, kafka);
    }

    /**
     * An event dispatcher publishing the dispatched events into the {@code R.spine.core.Event}
     * topic.
     *
     * @see #dispatcher(Collection, KafkaWrapper) for external instantiation
     */
    private static class KafkaEventDispatcher implements EventDispatcher<Object> {

        private final ImmutableSet<EventClass> messageClasses;
        private final KafkaWrapper kafka;

        private KafkaEventDispatcher(Collection<EventClass> messageClasses,
                                     KafkaWrapper kafka) {
            this.messageClasses = ImmutableSet.copyOf(messageClasses);
            this.kafka = kafka;
        }

        @SuppressWarnings("ReturnOfCollectionOrArrayField") // OK for an immutable collection.
        @Override
        public Set<EventClass> getMessageClasses() {
            return messageClasses;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * Publishes the given {@linkplain EventEnvelope event} into the {@code R.spine.core.Event}
         * topic as a key-value pair:
         * {@link EventEnvelope#getId()} -> {@link EventEnvelope#getOuterObject()}.
         *
         * @return an empty set
         */
        @Override
        public Set<Object> dispatch(EventEnvelope envelope) {
            final Message id = envelope.getId();
            final Message event = envelope.getOuterObject();
            kafka.write(EVENT_TOPIC, id, event);
            return Collections.emptySet();
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * Writes the error into {@link #log()}.
         */
        @Override
        public void onError(EventEnvelope envelope, RuntimeException exception) {
            log().error(format("Error dispatching event %s.", envelope.getOuterObject()),
                        exception);
        }
    }

    /**
     * A {@link Serde} implementation as well as {@link Serializer} and {@link Deserializer}
     * implementation for type {@link Object}.
     *
     * <p>As a {@link Serializer}, transforms any object including {@code null}s into an empty byte
     * array.
     *
     * <p>As a {@link Deserializer}, transforms any byte array into a {@code null} object.
     *
     * <p>As a {@link Serde}, returns {@code this} reference at any invocation of
     * {@link Serde#serializer()} and {@link Serde#serializer()}.
     */
    private enum VoidSerde implements Serde<Object>, Serializer<Object>, Deserializer<Object> {

        INSTANCE;

        private static final byte[] DATA = {};

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * Performs no operation.
         */
        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            // NoOp
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * Returns an empty byte array upon any invocation.
         *
         * @return empty byte array
         */
        @Override
        public byte[] serialize(String topic, @Nullable Object data) {
            return DATA;
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * Returns {@code null} reference upon any invocation.
         *
         * @return {@link #voidInstance()}
         */
        @Nullable
        @Override
        public Object deserialize(String topic, byte[] data) {
            return voidInstance();
        }

        /**
         * {@inheritDoc}
         *
         * @implSpec
         * Performs no operation.
         */
        @Override
        public void close() {
            // NoOp
        }

        /**
         * {@inheritDoc}
         *
         * @return self reference
         */
        @Override
        public Serializer<Object> serializer() {
            return this;
        }

        /**
         * {@inheritDoc}
         *
         * @return self reference
         */
        @Override
        public Deserializer<Object> deserializer() {
            return this;
        }

        @SuppressWarnings("unchecked") // OK for the `VoidSerde`.
        private static <T> Serde<T> cast() {
            return (Serde<T>) INSTANCE;
        }
    }

    private static Logger log() {
        return LogSingleton.INSTANCE.value;
    }

    private enum LogSingleton {
        INSTANCE;
        @SuppressWarnings("NonSerializableFieldInSerializableClass")
        private final Logger value = LoggerFactory.getLogger(KafkaCatchUp.class);
    }
}
