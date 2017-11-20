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

package io.spine.examples.todolist.mylist;

import android.arch.lifecycle.Observer;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Timestamp;
import io.spine.examples.todolist.LabelColor;
import io.spine.examples.todolist.R;
import io.spine.examples.todolist.q.projection.MyListView;
import io.spine.examples.todolist.q.projection.TaskItem;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.view.View.GONE;
import static com.google.protobuf.util.Timestamps.toMillis;
import static io.spine.examples.todolist.LabelColor.BLUE;
import static io.spine.examples.todolist.LabelColor.GRAY;
import static io.spine.examples.todolist.LabelColor.GREEN;
import static io.spine.examples.todolist.LabelColor.RED;
import static io.spine.validate.Validate.isDefault;

/**
 * The {@link RecyclerView.Adapter} implementation listening to the {@link MyListView} data updates
 * and initiating the {@link RecyclerView} updates upon the data updates.
 */
final class MyTaskListViewAdapter
        extends Adapter<MyTaskListViewAdapter.TaskViewHolder> implements Observer<MyListView> {

    /**
     * The map of the {@link LabelColor} enum to the RGB values of the color.
     */
    private static final Map<LabelColor, Integer> COLORS;

    static {
        final int defaultColor = Color.GRAY;
        final Class<Color> colorConstants = Color.class;
        final ImmutableMap.Builder<LabelColor, Integer> colors = ImmutableMap.builder();
        for (LabelColor color : LabelColor.values()) {
            final String colorName = color.name();
            try {
                final Field field = colorConstants.getField(colorName);
                final int colorCode = (Integer) field.get(null);
                colors.put(color, colorCode);
            } catch (NoSuchFieldException ignored) {
                colors.put(color, defaultColor);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        COLORS = colors.build();
    }

    /**
     * The cached data currently displayed on the associated view.
     */
    private final List<TaskItem> data = new ArrayList<>();

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View taskView = LayoutInflater.from(parent.getContext())
                                            .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(taskView);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        final TaskItem task = data.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Updates the {@linkplain #data cached data} and initiates the view redraw.
     */
    @Override
    public void onChanged(@Nullable MyListView myListView) {
        data.clear();
        if (myListView != null && !isDefault(myListView)) {
            data.addAll(myListView.getMyList().getItemsList());
        }
        notifyDataSetChanged();
    }

    /**
     * Maps the given {@link TaskItem} enum value to the RGB color code.
     *
     * @param item the color as an enum value
     * @return the RGB code of the color
     */
    private static int colorOf(TaskItem item) {
        final LabelColor color = item.getLabelColor();
        Integer hex = COLORS.get(color);
        if (hex == null) {
            hex = COLORS.get(GRAY);
        }
        return hex;
    }

    /**
     * The implementation of the {@link RecyclerView.ViewHolder} holding a single
     * {@link TaskItem} view.
     */
    static class TaskViewHolder extends RecyclerView.ViewHolder {

        private static final SimpleDateFormat DATE_FORMAT =
                new SimpleDateFormat("dd MMM YYYY", Locale.getDefault());

        private final TextView description;
        private final TextView dueDate;
        private final View dueDateLabel;
        private final View colorView;
        private final View root;

        private final Resources resources;

        private TaskViewHolder(View itemView) {
            super(itemView);
            this.root = itemView;
            this.description = itemView.findViewById(R.id.task_content);
            this.dueDate = itemView.findViewById(R.id.task_due_date);
            this.dueDateLabel = itemView.findViewById(R.id.task_due_date_label);
            this.colorView = itemView.findViewById(R.id.task_label_color_stripe);
            this.resources = itemView.getResources();
        }

        /**
         * Binds the given {@link TaskItem} to the held view.
         *
         * @param data the data to display on the view
         */
        private void bind(TaskItem data) {
            description.setText(data.getDescription().getValue());
            bindDueDate(data.getDueDate());
            colorView.setBackgroundColor(colorOf(data));
            if (data.getCompleted()) {
                root.setBackgroundColor(resources.getColor(R.color.completedTaskColor));
            }
        }

        private void bindDueDate(Timestamp taskDueDate) {
            final boolean isPresent = taskDueDate.getSeconds() > 0;
            if (isPresent) {
                setTime(dueDate, taskDueDate);
            } else {
                dueDate.setVisibility(GONE);
                dueDateLabel.setVisibility(GONE);
            }
        }

        @SuppressWarnings("AccessToNonThreadSafeStaticField")
            // DATE_FORMAT - OK since setTime() is always called from the main thread.
        private static void setTime(TextView view, Timestamp time) {
            final long millis = toMillis(time);
            final Date date = new Date(millis);
            view.setText(DATE_FORMAT.format(date));
        }
    }
}
