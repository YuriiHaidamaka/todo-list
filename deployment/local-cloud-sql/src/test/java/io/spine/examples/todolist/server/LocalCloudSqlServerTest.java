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

import io.spine.server.BoundedContext;
import io.spine.server.storage.StorageFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.spine.examples.todolist.server.LocalCloudSqlServer.createBoundedContext;
import static io.spine.examples.todolist.server.LocalCloudSqlServer.getActualArguments;
import static io.spine.examples.todolist.server.LocalCloudSqlServer.getDefaultArguments;
import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("LocalCloudSqlServer should")
class LocalCloudSqlServerTest {

    @Test
    @DisplayName("have the private constructor")
    void havePrivateCtor() {
        assertHasPrivateParameterlessCtor(LocalCloudSqlServer.class);
    }

    @Test
    @DisplayName("return default arguments on invalid custom arguments")
    void returnDefaultArguments() {
        final String[] customArguments = {"firstArg", "secondArg"};
        assertNotEquals(customArguments.length, getDefaultArguments().length);

        final String[] actualArguments = getActualArguments(customArguments);
        assertArrayEquals(getDefaultArguments(), actualArguments);
    }

    @Test
    @DisplayName("return specified arguments if match length requirements")
    void returnSpecifiedArguments() {
        final int requiredLength = getDefaultArguments().length;
        final String[] customArguments = new String[requiredLength];
        final String[] actualArguments = getActualArguments(customArguments);
        assertEquals(customArguments, actualArguments);
    }

    @Test
    @DisplayName("create signletenant BoundedContext")
    void createSingletenantBoundedContext() {
        final BoundedContext boundedContext = createBoundedContext(getDefaultArguments());
        final StorageFactory storageFactory = boundedContext.getStorageFactory();
        assertFalse(storageFactory.isMultitenant());
    }
}
