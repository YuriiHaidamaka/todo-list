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

package org.spine3.examples.todolist.client;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.spine3.base.Command;
import org.spine3.base.Queries;
import org.spine3.client.CommandFactory;
import org.spine3.client.Query;
import org.spine3.client.QueryResponse;
import org.spine3.client.grpc.CommandServiceGrpc;
import org.spine3.client.grpc.QueryServiceGrpc;
import org.spine3.examples.todolist.c.commands.AssignLabelToTask;
import org.spine3.examples.todolist.c.commands.CompleteTask;
import org.spine3.examples.todolist.c.commands.CreateBasicLabel;
import org.spine3.examples.todolist.c.commands.CreateBasicTask;
import org.spine3.examples.todolist.c.commands.CreateDraft;
import org.spine3.examples.todolist.c.commands.DeleteTask;
import org.spine3.examples.todolist.c.commands.FinalizeDraft;
import org.spine3.examples.todolist.c.commands.RemoveLabelFromTask;
import org.spine3.examples.todolist.c.commands.ReopenTask;
import org.spine3.examples.todolist.c.commands.RestoreDeletedTask;
import org.spine3.examples.todolist.c.commands.UpdateLabelDetails;
import org.spine3.examples.todolist.c.commands.UpdateTaskDescription;
import org.spine3.examples.todolist.c.commands.UpdateTaskDueDate;
import org.spine3.examples.todolist.c.commands.UpdateTaskPriority;
import org.spine3.examples.todolist.q.projections.DraftTasksView;
import org.spine3.examples.todolist.q.projections.LabelledTasksView;
import org.spine3.examples.todolist.q.projections.MyListView;
import org.spine3.time.ZoneOffsets;
import org.spine3.users.UserId;
import org.spine3.util.Exceptions;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.spine3.base.Identifiers.newUuid;

/**
 * Implementation of the command line gRPC client.
 *
 * @author Illia Shepilov
 */
public class CommandLineTodoClient implements TodoClient {

    private static final int TIMEOUT = 10;
    private final ManagedChannel channel;
    private final QueryServiceGrpc.QueryServiceBlockingStub queryService;
    private final CommandServiceGrpc.CommandServiceBlockingStub commandService;
    private final CommandFactory commandFactory;

    /**
     * Construct the client connecting to server at {@code host:port}.
     */
    public CommandLineTodoClient(String host, int port) {
        this.commandFactory = commandFactoryInstance();
        this.channel = initChannel(host, port);
        this.commandService = CommandServiceGrpc.newBlockingStub(channel);
        this.queryService = QueryServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void create(CreateBasicTask cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void create(CreateBasicLabel cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void create(CreateDraft cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void update(UpdateTaskDescription cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void update(UpdateTaskDueDate cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void update(UpdateTaskPriority cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void update(UpdateLabelDetails cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void delete(DeleteTask cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void removeLabel(RemoveLabelFromTask cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void assignLabel(AssignLabelToTask cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void reopen(ReopenTask cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void restore(RestoreDeletedTask cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void complete(CompleteTask cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public void finalize(FinalizeDraft cmd) {
        final Command executableCmd = commandFactory.create(cmd);
        commandService.post(executableCmd);
    }

    @Override
    public MyListView getMyListView() {
        try {
            final Query query = Queries.readAll(MyListView.class);
            final QueryResponse response = queryService.read(query);

            final boolean isEmpty = response.getMessagesList()
                                            .isEmpty();
            if (isEmpty) {
                return MyListView.getDefaultInstance();
            }

            MyListView result = response.getMessages(0)
                                        .unpack(MyListView.class);
            return result;
        } catch (InvalidProtocolBufferException e) {
            throw Exceptions.wrappedCause(e);
        }
    }

    @Override
    public List<LabelledTasksView> getLabelledTasksView() {
        try {
            final Query query = Queries.readAll(LabelledTasksView.class);
            final QueryResponse response = queryService.read(query);
            final List<Any> messageList = response.getMessagesList();
            final List<LabelledTasksView> result = newArrayList();

            for (Any any : messageList) {
                final LabelledTasksView labelledView = any.unpack(LabelledTasksView.class);
                result.add(labelledView);
            }

            return result;
        } catch (InvalidProtocolBufferException e) {
            throw Exceptions.wrappedCause(e);
        }
    }

    @Override
    public DraftTasksView getDraftTasksView() {
        try {
            final Query query = Queries.readAll(DraftTasksView.class);
            final QueryResponse response = queryService.read(query);

            final boolean isEmpty = response.getMessagesList()
                                            .isEmpty();
            if (isEmpty) {
                return DraftTasksView.getDefaultInstance();
            }
            final DraftTasksView result = response.getMessages(0)
                                                  .unpack(DraftTasksView.class);
            return result;
        } catch (InvalidProtocolBufferException e) {
            throw Exceptions.wrappedCause(e);
        }
    }

    @Override
    public void shutdown() {
        try {
            channel.shutdown()
                   .awaitTermination(TIMEOUT, SECONDS);
        } catch (InterruptedException e) {
            throw Exceptions.wrappedCause(e);
        }
    }

    private static ManagedChannel initChannel(String host, int port) {
        final ManagedChannel result = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext(true)
                .build();
        return result;
    }

    private static CommandFactory commandFactoryInstance() {
        final UserId userId = UserId.newBuilder()
                                    .setValue(newUuid())
                                    .build();
        final CommandFactory result = CommandFactory.newBuilder()
                                                    .setActor(userId)
                                                    .setZoneOffset(ZoneOffsets.UTC)
                                                    .build();
        return result;
    }
}
