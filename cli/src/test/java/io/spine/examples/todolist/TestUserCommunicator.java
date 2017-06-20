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

package io.spine.examples.todolist;

import static io.spine.examples.todolist.mode.DisplayHelper.getLineSeparator;

/**
 * @author Dmytro Grankin
 */
public class TestUserCommunicator implements UserCommunicator {

    @SuppressWarnings("StringBufferField") // Used to collect all output of the class.
    private static final StringBuilder builder = new StringBuilder();

    /**
     * {@inheritDoc}
     *
     * <p>Just returns the question to make testing easier.
     *
     * @param question {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String askUser(String question) {
        return question;
    }

    @Override
    public void println(String message) {
        builder.append(message)
               .append(getLineSeparator());
    }

    public static String getOutput() {
        return builder.toString();
    }

    public static void clearOutput() {
        builder.setLength(0);
    }
}
