package com.example.final_project.ui.gantt_chart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.model.DevTask;

import java.util.List;

public class GanttChartAdapter extends RecyclerView.Adapter<GanttChartAdapter.TaskViewHolder> {
    private List<DevTask> tasks;

    public GanttChartAdapter(List<DevTask> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_row, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        DevTask task = tasks.get(position);
        // Bind task data to the UI elements
        holder.taskName.setText(task.getTaskName());
        holder.devName.setText(task.getDevName());
        holder.startDate.setText(task.getStartDate());
        holder.endDate.setText(task.getEndDate());
        holder.textEstimateDay.setText(String.valueOf(task.getEstimateDay()));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, devName, startDate, endDate, textEstimateDay;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            devName = itemView.findViewById(R.id.devName);
            startDate = itemView.findViewById(R.id.startDate);
            textEstimateDay = itemView.findViewById(R.id.estimateDay);
            endDate = itemView.findViewById(R.id.endDate);

        }
    }

}
