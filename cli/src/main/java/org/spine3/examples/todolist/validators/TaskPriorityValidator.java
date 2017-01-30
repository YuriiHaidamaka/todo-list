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

package org.spine3.examples.todolist.validators;

import org.spine3.examples.todolist.TaskPriority;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * @author Illia Shepilov
 */
public class TaskPriorityValidator implements Validator {

    private static final String INCORRECT_PRIORITY = "Please enter the correct task priority.\n" +
            "Valid task priority:\n1: LOW;\n2: NORMAL;\n3: HIGH.";
    private final Map<String, TaskPriority> priorityMap;
    private String message;

    public TaskPriorityValidator(Map<String, TaskPriority> priorityMap) {
        this.priorityMap = priorityMap;
    }

    @Override
    public boolean validate(String input) {
        final TaskPriority taskPriority = priorityMap.get(input);
        if (taskPriority == null) {
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
