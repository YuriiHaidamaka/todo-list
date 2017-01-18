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
import org.spine3.examples.todolist.validator.Validatable;

import java.util.List;

import static org.spine3.examples.todolist.validator.Validator.checkNotEmpty;
import static org.spine3.examples.todolist.validator.Validator.checkNotNull;

/**
 * @author Illia Shepilov
 */
public class TaskPriorityValidator implements Validatable {

    @Override
    public String validate(String input) {
        boolean isNotNull = checkNotNull(input);
        if (!isNotNull) {
            return "The task priority cannot be null.";
        }
        boolean isNotEmpty = checkNotEmpty(input);
        if (isNotEmpty) {
            return "The task priority cannot be empty.";
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
            return "Please enter the valid priority.";
        }
        return CORRECT_INPUT;
    }
}
