package com.example.final_project.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.model.DevTask;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<DevTask> taskList;
    private Context context;
    private OnTaskClickListener listener;

    public TaskAdapter(Context context, List<DevTask> taskList, OnTaskClickListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DevTask task = taskList.get(position);
        holder.taskName.setText(task.getTaskName() != null ? task.getTaskName() : "No Task Name");
        holder.devName.setText(task.getDevName() != null ? task.getDevName() : "No Assignee");
        holder.startDate.setText(Html.fromHtml("<b>" + (task.getStartDate() != null ? task.getStartDate() : "No Start Date") + "</b>"));
        holder.endDate.setText(Html.fromHtml("<b>" + (task.getEndDate() != null ? task.getEndDate() : "No End Date") + "</b>"));

        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isEstimateDayVisible = sharedPreferences.getBoolean("show_estimate_day", true);

        if (isEstimateDayVisible) {
            holder.textEstimateDay.setVisibility(View.VISIBLE);
            holder.textEstimateDay.setText(task.getEstimateDay() > 0 ? task.getEstimateDay() + " days" : "No Estimate Day");
        } else {
            holder.textEstimateDay.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
    }


    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, devName, startDate, endDate, textEstimateDay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            devName = itemView.findViewById(R.id.devName);
            startDate = itemView.findViewById(R.id.startDate);
            textEstimateDay = itemView.findViewById(R.id.estimateDay);
            endDate = itemView.findViewById(R.id.endDate);
        }
    }

    public void updateTaskList(List<DevTask> newTaskList) {
        this.taskList = newTaskList;
        notifyDataSetChanged();
    }

    public interface OnTaskClickListener {
        void onTaskClick(DevTask task);
    }
}
