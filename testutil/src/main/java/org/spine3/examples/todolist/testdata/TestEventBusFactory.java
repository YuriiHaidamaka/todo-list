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

package org.spine3.examples.todolist.testdata;

import org.spine3.server.event.EventBus;
import org.spine3.server.event.enrich.EventEnricher;
import org.spine3.server.storage.StorageFactory;

/**
 * Provides the {@link EventBus} instances for the test needs.
 *
 * @author Illia Shepilov
 */
public class TestEventBusFactory {

    private TestEventBusFactory() {
    }

    /**
     * Returns a pre-configured {@link EventBus} instance.
     *
     * @param storageFactory the {@link StorageFactory} to set
     * @param eventEnricher  the {@link EventEnricher} to set
     * @return the {@code EventBus} instance
     */
    public static EventBus eventBusInstance(StorageFactory storageFactory, EventEnricher eventEnricher) {
        final EventBus result = EventBus.newBuilder()
                                        .setStorageFactory(storageFactory)
                                        .setEnricher(eventEnricher)
                                        .build();
        return result;
    }
}
