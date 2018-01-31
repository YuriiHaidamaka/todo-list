/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.examples.todolist.client;

import com.google.protobuf.Any;
import com.google.protobuf.Message;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.spine.Identifier;
import io.spine.client.ActorRequestFactory;
import io.spine.client.EntityStateUpdate;
import io.spine.client.Query;
import io.spine.client.Subscription;
import io.spine.client.SubscriptionUpdate;
import io.spine.client.Topic;
import io.spine.client.grpc.CommandServiceGrpc;
import io.spine.client.grpc.CommandServiceGrpc.CommandServiceBlockingStub;
import io.spine.client.grpc.QueryServiceGrpc;
import io.spine.client.grpc.QueryServiceGrpc.QueryServiceBlockingStub;
import io.spine.client.grpc.SubscriptionServiceGrpc;
import io.spine.client.grpc.SubscriptionServiceGrpc.SubscriptionServiceBlockingStub;
import io.spine.client.grpc.SubscriptionServiceGrpc.SubscriptionServiceStub;
import io.spine.core.Command;
import io.spine.core.UserId;
import io.spine.examples.todolist.LabelId;
import io.spine.examples.todolist.Task;
import io.spine.examples.todolist.TaskId;
import io.spine.examples.todolist.TaskLabel;
import io.spine.examples.todolist.TaskLabels;
import io.spine.examples.todolist.c.commands.TodoCommand;
import io.spine.examples.todolist.q.projection.DraftTasksView;
import io.spine.examples.todolist.q.projection.LabelledTasksView;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.protobuf.AnyPacker;
import io.spine.time.ZoneOffsets;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.of;
import static io.spine.Identifier.newUuid;
import static io.spine.util.Exceptions.illegalStateWithCauseOf;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

/**
 * An implementation of the TodoList gRPC client.
 *
 * @author Illia Shepilov
 * @author Dmitry Ganzha
 */
@SuppressWarnings("OverlyCoupledClass")
final class TodoClientImpl implements SubscribingTodoClient {

    private static final int TIMEOUT = 10;

    private final ManagedChannel channel;
    private final QueryServiceBlockingStub queryService;
    private final CommandServiceBlockingStub commandService;
    private final SubscriptionServiceStub subscriptionService;
    private final SubscriptionServiceBlockingStub blockingSubscriptionService;
    private final ActorRequestFactory requestFactory;

    /**
     * Construct the client connecting to server at {@code host:port}.
     */
    TodoClientImpl(String host, int port) {
        this.requestFactory = actorRequestFactoryInstance();
        this.channel = initChannel(host, port);
        this.commandService = CommandServiceGrpc.newBlockingStub(channel);
        this.queryService = QueryServiceGrpc.newBlockingStub(channel);
        this.subscriptionService = SubscriptionServiceGrpc.newStub(channel);
        this.blockingSubscriptionService = SubscriptionServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void postCommand(TodoCommand cmd) {
        final Command executableCmd = requestFactory.command()
                                                    .create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public MyListView getMyListView() {
        final Query query = requestFactory.query()
                                          .all(MyListView.class);
        final List<Any> messages = queryService.read(query)
                                               .getMessagesList();
        return messages.isEmpty()
               ? MyListView.getDefaultInstance()
               : AnyPacker.unpack(messages.get(0));
    }

    @Override
    public List<LabelledTasksView> getLabelledTasksView() {
        final Query query = requestFactory.query()
                                          .all(LabelledTasksView.class);
        final List<Any> messages = queryService.read(query)
                                               .getMessagesList();
        final List<LabelledTasksView> result = messages
                .stream()
                .map(AnyPacker::<LabelledTasksView>unpack)
                .collect(toList());

        return result;
    }

    @Override
    public DraftTasksView getDraftTasksView() {
        final Query query = requestFactory.query()
                                          .all(DraftTasksView.class);
        final List<Any> messages = queryService.read(query)
                                               .getMessagesList();
        return messages.isEmpty()
               ? DraftTasksView.getDefaultInstance()
               : AnyPacker.unpack(messages.get(0));
    }

    @Override
    public List<Task> getTasks() {
        return getByType(Task.class);
    }

    @Override
    public Task getTaskOr(TaskId id, @Nullable Task other) {
        final Optional<Task> found = findById(Task.class, id);
        return found.orElse(other);
    }

    @Override
    public List<TaskLabel> getLabels() {
        return getByType(TaskLabel.class);
    }

    @Override
    public TaskLabels getLabels(TaskId taskId) {
        final Optional<TaskLabels> labels = findById(TaskLabels.class, taskId);
        final TaskLabels result = labels.orElse(TaskLabels.newBuilder()
                                                          .setTaskId(taskId)
                                                          .build());
        return result;
    }

    @Nullable
    @Override
    public TaskLabel getLabelOr(LabelId id, @Nullable TaskLabel other) {
        final Optional<TaskLabel> found = findById(TaskLabel.class, id);
        return found.orElse(other);
    }

    @Override
    public Subscription subscribeToTasks(StreamObserver<MyListView> observer) {
        final Topic topic = requestFactory.topic()
                                          .allOf(MyListView.class);
        return subscribeTo(topic, observer);
    }

    @Override
    public void unSubscribe(Subscription subscription) {
        blockingSubscriptionService.cancel(subscription);
    }

    @Override
    public void shutdown() {
        try {
            channel.shutdown()
                   .awaitTermination(TIMEOUT, SECONDS);
        } catch (InterruptedException e) {
            throw illegalStateWithCauseOf(e);
        }
    }

    /**
     * Subscribes the given {@link StreamObserver} to the given topic and activates
     * the subscription.
     *
     * @param topic    the topic to subscribe to
     * @param observer the observer to subscribe
     * @param <M>      the type of the result messages
     * @return the activated subscription
     */
    private <M extends Message> Subscription subscribeTo(Topic topic, StreamObserver<M> observer) {
        final Subscription subscription = blockingSubscriptionService.subscribe(topic);
        subscriptionService.activate(subscription, new SubscriptionUpdateObserver<>(observer));
        return subscription;
    }

    /**
     * Retrieves all the messages of the given type.
     *
     * @param cls the class of the desired messages
     * @param <M> the compile-time type of the desired messages
     * @return all the messages of the given type present in the system
     */
    private <M extends Message> List<M> getByType(Class<M> cls) {
        final Query query = requestFactory.query()
                                          .all(cls);
        final List<Any> messages = queryService.read(query)
                                               .getMessagesList();
        final List<M> result = messages.stream()
                                       .map(AnyPacker::<M>unpack)
                                       .collect(toList());
        return result;
    }

    private <M extends Message> Optional<M> findById(Class<M> messageClass, Message id) {
        final Query query = requestFactory.query()
                                          .byIds(messageClass, of(id));
        final List<Any> messages = queryService.read(query)
                                               .getMessagesList();
        checkState(messages.size() <= 1,
                   "Too many %s-s with ID %s:%s %s",
                   messageClass.getSimpleName(), Identifier.toString(id),
                   System.lineSeparator(), messages);
        final Optional<M> result = messages.stream()
                                           .map(AnyPacker::<M>unpack)
                                           .findFirst();
        return result;
    }

    private static ManagedChannel initChannel(String host, int port) {
        final ManagedChannel result = ManagedChannelBuilder.forAddress(host, port)
                                                           .usePlaintext(true)
                                                           .build();
        return result;
    }

    private static ActorRequestFactory actorRequestFactoryInstance() {
        final UserId userId = UserId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        final ActorRequestFactory result = ActorRequestFactory.newBuilder()
                                                              .setActor(userId)
                                                              .setZoneOffset(ZoneOffsets.UTC)
                                                              .build();
        return result;
    }

    /**
     * A {@link StreamObserver} of {@link SubscriptionUpdate} messages translating the message
     * payload to the given delegate {@code StreamObserver}.
     *
     * <p>The errors and completion acknowledgements are translated directly to the delegate.
     *
     * <p>The {@linkplain SubscriptionUpdate#getEntityStateUpdatesList() messages} are unpacked
     * and sent to the delegate observer one by one.
     *
     * @param <M> the type of the delegate observer messages
     */
    private static final class SubscriptionUpdateObserver<M extends Message>
            implements StreamObserver<SubscriptionUpdate> {

        private final StreamObserver<M> delegate;

        private SubscriptionUpdateObserver(StreamObserver<M> targetObserver) {
            this.delegate = targetObserver;
        }

        @Override
        public void onNext(SubscriptionUpdate value) {
            value.getEntityStateUpdatesList()
                 .stream()
                 .map(EntityStateUpdate::getState)
                 .map(AnyPacker::<M>unpack)
                 .forEach(delegate::onNext);
        }

        @Override
        public void onError(Throwable t) {
            delegate.onError(t);
        }

        @Override
        public void onCompleted() {
            delegate.onCompleted();
        }
    }
}
