package com.example.final_project.ui.search;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.model.DevTask;

import java.util.List;

public class SearchFragment extends Fragment implements SearchTaskAdapter.OnTaskClickListener {
    private SearchViewModel searchViewModel;
    private SearchTaskAdapter taskAdapter; // Use SearchTaskAdapter
    private RecyclerView recyclerView;
    private EditText editSearch;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout for SearchFragment
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize RecyclerView and EditText for search
        recyclerView = root.findViewById(R.id.recyclerViewSearchResults);
        editSearch = root.findViewById(R.id.edit_search);
        Button btnSearch = root.findViewById(R.id.btn_search);

        // Set up RecyclerView
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Add DividerItemDecoration
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Initialize ViewModel using SearchViewModelFactory
        searchViewModel = new ViewModelProvider(this, new SearchViewModelFactory(requireActivity().getApplication())).get(SearchViewModel.class);
        searchViewModel.getSearchResults().observe(getViewLifecycleOwner(), this::updateSearchResults);

        searchViewModel.getNoResults().observe(getViewLifecycleOwner(), noResults -> {
            String query = editSearch.getText().toString().trim();
            if (noResults &&!query.isEmpty() ) {
                Toast.makeText(getContext(), "No tasks found", Toast.LENGTH_SHORT).show();
                searchViewModel.resetNoResults(); // Reset noResults về false sau khi thông báo
            }
        });
        // Set onClickListener for the search button
        btnSearch.setOnClickListener(v -> {
            String query = editSearch.getText().toString().trim();
            if (!query.isEmpty()) {
                searchViewModel.searchTasks(query); // Trigger search
            } else {
                Toast.makeText(getContext(), "Please enter a search query", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    // Update RecyclerView with search results
    private void updateSearchResults(List<DevTask> tasks) {
        if (taskAdapter == null) {
            taskAdapter = new SearchTaskAdapter(getContext(), tasks, this);
            recyclerView.setAdapter(taskAdapter);
        } else {
            taskAdapter.updateTaskList(tasks); // Cập nhật danh sách nếu đã khởi tạo
        }
    }

    @Override
    public void onTaskClick(DevTask task) {
        // Show task details and options to update or delete
        showTaskDetailsDialog(task);
    }

    private void showTaskDetailsDialog(DevTask task) {
        // Implement the dialog to show task details and options to update or delete
        // Similar to the previous implementation of showTaskDetailsDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_task_details, null);
        builder.setView(dialogView);

        EditText editTaskName = dialogView.findViewById(R.id.edit_task_name);
        EditText editDevName = dialogView.findViewById(R.id.edit_assignee);
        EditText editEstimateDay = dialogView.findViewById(R.id.edit_estimate_day);
        EditText editStartDate = dialogView.findViewById(R.id.edit_start_date);
        EditText editEndDate = dialogView.findViewById(R.id.edit_end_date);

        // Display task details
        editTaskName.setText(task.getTaskName());
        editDevName.setText(task.getDevName());
        editStartDate.setText(task.getStartDate());
        editEstimateDay.setText(String.valueOf(task.getEstimateDay()));
        editEndDate.setText(task.getEndDate());

        builder.setPositiveButton("Update", (dialog, which) -> {
            // Confirm and update task
            String updatedTaskName = editTaskName.getText().toString().trim();
            String updatedDevName = editDevName.getText().toString().trim();
            String updatedStartDate = editStartDate.getText().toString().trim();
            int updatedEstimateDay = Integer.parseInt(editEstimateDay.getText().toString().trim());
            String updatedEndDate = editEndDate.getText().toString().trim();

            // Call update method in ViewModel
            searchViewModel.updateTask(task.getId(), updatedDevName, task.getTaskId(), updatedTaskName, updatedEstimateDay, updatedStartDate, updatedEndDate);
            Toast.makeText(getContext(), "Task updated successfully", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Delete", (dialog, which) -> {
            // Confirm and delete task
            searchViewModel.deleteTask(task.getTaskId());
            Toast.makeText(getContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();
        });

        builder.setNeutralButton("Cancel", null);
        builder.create().show();
    }
}

