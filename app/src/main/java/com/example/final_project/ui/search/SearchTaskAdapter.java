package com.example.final_project.ui.search;

import android.content.Context;
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

public class SearchTaskAdapter extends RecyclerView.Adapter<SearchTaskAdapter.ViewHolder> {

    private List<DevTask> taskList;
    private Context context;
    private OnTaskClickListener listener; // Listener to handle task click events

    public SearchTaskAdapter(Context context, List<DevTask> taskList, OnTaskClickListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener; // Initialize listener
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
        holder.taskName.setText(task.getTaskName());
        holder.devName.setText(task.getDevName());
        holder.startDate.setText(Html.fromHtml("<b>" + task.getStartDate() + "</b>"));
        holder.textEstimateDay.setText(task.getEstimateDay() + " days");
        holder.endDate.setText(Html.fromHtml("<b>" + task.getEndDate() + "</b>"));

        // Set click listener for each item
        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task)); // Call method when clicked
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
        notifyDataSetChanged(); // Notify adapter to refresh the data
    }

    // Interface to handle click events on tasks
    public interface OnTaskClickListener {
        void onTaskClick(DevTask task);
    }
}
