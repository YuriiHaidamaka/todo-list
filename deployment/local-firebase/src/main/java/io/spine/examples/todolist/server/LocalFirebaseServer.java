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

package io.spine.examples.todolist.server;

import com.google.cloud.firestore.Firestore;
import com.google.common.collect.ImmutableSet;
import io.spine.examples.todolist.context.BoundedContexts;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.server.BoundedContext;
import io.spine.server.firebase.FirebaseSubscriptionMirror;
import io.spine.type.TypeUrl;

import java.io.IOException;
import java.util.Collection;

import static io.spine.client.ConnectionConstants.DEFAULT_CLIENT_SERVICE_PORT;
import static io.spine.examples.todolist.server.Server.newServer;
import static io.spine.type.TypeUrl.of;

/**
 * A local {@link Server} using
 * {@link io.spine.server.storage.memory.InMemoryStorageFactory InMemoryStorageFactory} and
 * the {@link FirebaseSubscriptionMirror}.
 *
 * <p>The server exposes its {@code gRPC API} at
 * {@linkplain io.spine.client.ConnectionConstants#DEFAULT_CLIENT_SERVICE_PORT default port}.
 *
 * @author Dmytro Dashenkov
 */
public class LocalFirebaseServer {

    /**
     * The types to reflect with the Firebase subscription mirror.
     */
    private static final Collection<TypeUrl> MIRRORED_TYPES = ImmutableSet.of(
            of(MyListView.class)
    );

    /**
     * The {@code private} constructor prevents direct instantiation.
     */
    private LocalFirebaseServer() {}

    public static void main(String[] args) throws IOException {
        final BoundedContext boundedContext = BoundedContexts.create();
        startSubscriptionMirror(boundedContext);
        final Server server = newServer(DEFAULT_CLIENT_SERVICE_PORT, boundedContext);
        server.start();
    }

    /**
     * Starts the Firebase subscription mirror in the root of the bound Cloud Firestore instance.
     *
     * @param boundedContext the {@link BoundedContext} to take the entity updates from
     */
    private static void startSubscriptionMirror(BoundedContext boundedContext) {
        final Firestore firestore = FirebaseClients.initializeFirestore();
        final FirebaseSubscriptionMirror subscriptionMirror =
                FirebaseSubscriptionMirror.newBuilder()
                                          .addBoundedContext(boundedContext)
                                          .setFirestore(firestore)
                                          .build();
        MIRRORED_TYPES.forEach(subscriptionMirror::reflect);
    }
}
