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

package io.spine.examples.todolist.view;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static com.google.protobuf.util.Timestamps.toMillis;
import static io.spine.examples.todolist.view.DateFormatter.DEFAULT_TIMESTAMP_VALUE;
import static io.spine.examples.todolist.view.DateFormatter.format;
import static io.spine.examples.todolist.view.DateFormatter.getDateFormat;
import static io.spine.test.Tests.assertHasPrivateParameterlessCtor;
import static io.spine.time.Time.getCurrentTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Dmytro Grankin
 */
@DisplayName("DateFormatter should")
class DateFormatterTest {

    @Test
    @DisplayName("have the private constructor")
    void havePrivateConstructor() {
        assertHasPrivateParameterlessCtor(DateFormatter.class);
    }

    @Test
    @DisplayName("return the default value for default timestamp")
    void returnDefaultValueForTimestamp() {
        assertEquals(DEFAULT_TIMESTAMP_VALUE, format(Timestamp.getDefaultInstance()));
    }

    @Test
    @DisplayName("format timestamp")
    void formatTimestamp() {
        final Timestamp timestamp = getCurrentTime();
        final long millis = toMillis(timestamp);
        final Date date = new Date(millis);

        final String expectedValue = getDateFormat().format(date);
        assertEquals(expectedValue, format(timestamp));
    }
}
