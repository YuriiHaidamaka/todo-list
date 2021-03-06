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

package io.spine.examples.todolist.view.newtask;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.LabelDetails;
import io.spine.examples.todolist.R;
import io.spine.examples.todolist.view.TimeFormatter;
import io.spine.examples.todolist.view.ViewModelFactory;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;

/**
 * The fragment which displays all the new task data for the user confirmation.
 *
 * @author Dmytro Dashenkov
 */
public final class NewTaskConfirmationFragment extends PagerFragment {

    static final int POSITION_IN_WIZARD = 2;

    private NewTaskViewModel model;
    private TextView taskDescription;
    private TextView taskPriority;
    private TextView taskDueDate;
    private RecyclerView taskLabels;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        model = ViewModelProviders.of(this, ViewModelFactory.CACHING)
                                  .get(NewTaskViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_new_task_confirmation,
                                           container,
                                           false);
        initViews(root);
        return root;
    }

    @Override
    void prepare() {
        bindViews();
    }

    private void initViews(View root) {
        this.taskDescription = root.findViewById(R.id.taskDescription);
        this.taskPriority = root.findViewById(R.id.taskPriority);
        this.taskDueDate = root.findViewById(R.id.taskDueDate);
        this.taskLabels = root.findViewById(R.id.taskLabels);

        final Context context = getContext();
        final int spinCount = context.getResources()
                                     .getInteger(R.integer.label_list_column_count);
        taskLabels.setLayoutManager(new GridLayoutManager(context, spinCount));
    }

    private void bindViews() {
        final String taskDescriptionValue = model.getTaskDescription().getValue();
        taskDescription.setText(taskDescriptionValue);
        final String taskPriorityValue = model.getTaskPriority().toString();
        taskPriority.setText(taskPriorityValue);
        final Timestamp dueDateTimestamp = model.getTaskDueDate();
        final String formattedDueDate = TimeFormatter.INSTANCE.format(dueDateTimestamp);
        if (!formattedDueDate.isEmpty()) {
            final String template = getString(R.string.due_date);
            taskDueDate.setText(format(template, formattedDueDate));
        }
        final List<LabelDetails> labels = newArrayList(model.getTaskLabels());
        final ReadonlyLabelsAdapter adapter = new ReadonlyLabelsAdapter(labels);
        taskLabels.setAdapter(adapter);
    }

    @Override
    void complete() {
        model.confirmTaskCreation();
    }
}
