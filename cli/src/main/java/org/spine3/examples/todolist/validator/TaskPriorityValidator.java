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

import com.google.common.collect.Lists;
import org.spine3.examples.todolist.TaskPriority;

import java.util.List;

import static org.spine3.examples.todolist.validator.ValidatorHelper.isEmpty;
import static org.spine3.examples.todolist.validator.ValidatorHelper.isNull;

/**
 * @author Illia Shepilov
 */
public class TaskPriorityValidator implements Validator {

    private static final String INCORRECT_PRIORITY = "Please enter the valid priority.";
    private static final String PRIORITY_IS_NULL = "The task priority cannot be null.";
    private static final String PRIORITY_IS_EMPTY = "The task priority cannot be empty.";
    private String message;

    @Override
    public boolean validate(String input) {
        boolean isNull = isNull(input);

        if (isNull) {
            message = PRIORITY_IS_NULL;
            return false;
        }

        boolean isEmpty = isEmpty(input);

        if (isEmpty) {
            message = PRIORITY_IS_EMPTY;
            return false;
        }

        final List<TaskPriority> validPriorities = Lists.newArrayList(TaskPriority.LOW,
                                                                      TaskPriority.NORMAL,
                                                                      TaskPriority.HIGH);
        boolean isValid = false;
        for (TaskPriority currentPriority : validPriorities) {
            if (currentPriority.name()
                               .equals(input.toUpperCase())) {
                isValid = true;
            }
        }
        if (!isValid) {
            message = INCORRECT_PRIORITY;
            return false;
        }
        return true;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
