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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import io.spine.examples.todolist.R;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A simple dialog for selecting a date.
 *
 * <p>The dialog expects a {@linkplain #setTargetFragment(Fragment, int) target fragment} to be set
 * with the {@link DatePickerDialog#DUE_DATE_REQUEST} request code.
 *
 * <p>When the dialog result is ready, the target fragment
 * {@link Fragment#onActivityResult(int, int, Intent)} will be triggered with the same request code.
 * The result {@code Intent} contains a {@code long} extra under
 * the {@link DatePickerDialog#DUE_DATE_MILLIS_KEY} key. The value of the extra is the value of
 * the selected date expressed as millis elapsed since the 1 Jan 1970.
 *
 * @author Dmytro Dashenkov
 */
public final class DatePickerDialog extends DialogFragment {

    static final int DUE_DATE_REQUEST = 1;
    static final String DUE_DATE_MILLIS_KEY = "due_date";

    private long dateMillis = 0;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.dialog_date_picker, container, false);
        initViews(root);
        return root;
    }

    private void initViews(View root) {
        final CalendarView calendarView = root.findViewById(R.id.calendar);
        final Button accept = root.findViewById(R.id.accept_btn);
        final Button decline = root.findViewById(R.id.decline_btn);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            final Calendar calendar = new GregorianCalendar(year, month, dayOfMonth);
            dateMillis = calendar.getTimeInMillis();
        });
        accept.setOnClickListener(btn -> {
            onResult(dateMillis);
            dismiss();
        });
        decline.setOnClickListener(btn -> dismiss());
    }

    private void onResult(long millis) {
        final Intent intent = new Intent();
        intent.putExtra(DUE_DATE_MILLIS_KEY, millis);
        getTargetFragment().onActivityResult(DUE_DATE_REQUEST, 0, intent);
    }
}
