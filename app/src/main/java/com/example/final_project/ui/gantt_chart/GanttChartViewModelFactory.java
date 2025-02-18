package com.example.final_project.ui.gantt_chart;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.final_project.DatabaseHelper;

public class GanttChartViewModelFactory implements ViewModelProvider.Factory {
    private final DatabaseHelper dbHelper;

    public GanttChartViewModelFactory(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(GanttChartViewModel.class)) {
            return (T) new GanttChartViewModel(dbHelper);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
