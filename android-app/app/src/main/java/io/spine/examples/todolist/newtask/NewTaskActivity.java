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

package io.spine.examples.todolist.newtask;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.EditText;
import io.spine.examples.todolist.R;
import io.spine.examples.todolist.TaskDescription;
import io.spine.examples.todolist.lifecycle.AbstractActivity;

/**
 * The {@code Activity} allowing the user to create a new task.
 *
 * @see NewTaskViewModel
 */
public class NewTaskActivity extends AbstractActivity<NewTaskViewModel> {

    private ViewPager wizardView;

    @Override
    protected Class<NewTaskViewModel> getViewModelClass() {
        return NewTaskViewModel.class;
    }

    @Override
    protected int getTitleResource() {
        return R.string.new_task;
    }

    @Override
    protected int getContentViewResource() {
        return R.layout.activity_new_task;
    }

    @Override
    protected void initializeView() {
//        final EditText taskDescription = findViewById(R.id.new_task_description);

        wizardView = findViewById(R.id.new_task_pager);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final PagerAdapter wizard = new WizardAdapter(fragmentManager);
        wizardView.setAdapter(wizard);

        final Button nextPage = findViewById(R.id.next_btn);

        nextPage.setOnClickListener(button -> nextPage());
//            final String descriptionValue = taskDescription.getText().toString();
//            final TaskDescription description = createDescription(descriptionValue);
//            model().createTask(description);
//            navigator().navigateBack();
//        navigator().navigateBack()
    }

    private void nextPage() {
        final int currentPageIndex = wizardView.getCurrentItem();
        wizardView.setCurrentItem(currentPageIndex + 1);
    }

    private static TaskDescription createDescription(String value) {
        final TaskDescription description = TaskDescription.newBuilder()
                                                           .setValue(value)
                                                           .build();
        return description;
    }
}
