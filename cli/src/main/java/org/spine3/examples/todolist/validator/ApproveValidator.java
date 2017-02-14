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

package org.spine3.examples.todolist.validator;

/**
 * @author Illia Shepilov
 */
public class ApproveValidator implements Validator {

    private static final String INCORRECT_INPUT = "Incorrect input. Valid values: 'y' or 'n'";
    private static final String NEGATIVE_ANSWER = "n";
    private static final String POSITIVE_ANSWER = "y";
    private String message;

    @Override
    public boolean validate(String input) {

        final boolean isNegativeOrPositiveAns = input.equals(NEGATIVE_ANSWER) || input.equals(POSITIVE_ANSWER);
        final boolean invalidInput = input.isEmpty() || !isNegativeOrPositiveAns;

        if (invalidInput) {
            this.message = INCORRECT_INPUT;
            return false;
        }
        return true;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
