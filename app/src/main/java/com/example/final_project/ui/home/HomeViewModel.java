package com.example.final_project.ui.home;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.final_project.DatabaseHelper;
import com.example.final_project.model.DevTask;

import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<List<DevTask>> taskList;
    private DatabaseHelper dbHelper;

    public HomeViewModel(Application application) {
        taskList = new MutableLiveData<>();
        dbHelper = new DatabaseHelper(application);
        loadTasksFromDatabase(); // Load tasks when ViewModel is created
    }

    public LiveData<List<DevTask>> getTaskList() {
        return taskList;
    }

    // Load tasks from SQLite into LiveData
    public void loadTasksFromDatabase() {
        List<DevTask> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT dt.ID, dt.DEV_NAME, dt.TASKID, dt.STARTDATE, dt.ENDDATE, t.TASK_NAME, t.ESTIMATE_DAY " +
                        "FROM dev_task dt " +
                        "JOIN task t ON dt.TASKID = t.ID " +
                        "ORDER BY dt.TASKID", null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("ID"));
                @SuppressLint("Range") String devName = cursor.getString(cursor.getColumnIndex("DEV_NAME"));
                @SuppressLint("Range") int taskId = cursor.getInt(cursor.getColumnIndex("TASKID"));
                @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex("TASK_NAME"));
                @SuppressLint("Range") String startDate = cursor.getString(cursor.getColumnIndex("STARTDATE"));
                @SuppressLint("Range") String endDate = cursor.getString(cursor.getColumnIndex("ENDDATE"));
                @SuppressLint("Range") int estimateDay = cursor.getInt(cursor.getColumnIndex("ESTIMATE_DAY"));

                DevTask task = new DevTask(id, taskName, devName, taskId, startDate, endDate, estimateDay);
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        taskList.setValue(tasks);
    }

    // Add a new task and associate it with dev_task
    public void addTask(String taskName, int estimateDay, String devName, String startDate, String endDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("TASK_NAME", taskName);
        values.put("ESTIMATE_DAY", estimateDay);

        long newTaskId = db.insert("task", null, values);

        if (newTaskId != -1) {
            ContentValues devTaskValues = new ContentValues();
            devTaskValues.put("DEV_NAME", devName);
            devTaskValues.put("TASKID", newTaskId);
            devTaskValues.put("STARTDATE", startDate);
            devTaskValues.put("ENDDATE", endDate);

            long newRowId = db.insert("dev_task", null, devTaskValues);
            if (newRowId != -1) {
                loadTasksFromDatabase();
            }
        }
        db.close();
    }

    // Update an existing task in the dev_task table
    public void updateTask(int id, String devName, int taskId, String taskName, int estimateDay, String startDate, String endDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues devTaskValues = new ContentValues();
        devTaskValues.put("DEV_NAME", devName);
        devTaskValues.put("TASKID", taskId);
        devTaskValues.put("STARTDATE", startDate);
        devTaskValues.put("ENDDATE", endDate);

        db.update("dev_task", devTaskValues, "ID = ?", new String[]{String.valueOf(id)});

        ContentValues taskValues = new ContentValues();
        taskValues.put("TASK_NAME", taskName);
        taskValues.put("ESTIMATE_DAY", estimateDay);

        db.update("task", taskValues, "ID = ?", new String[]{String.valueOf(taskId)});
        db.close();

        loadTasksFromDatabase();
    }

    // Phương thức xóa task trong cả hai ViewModel (áp dụng cho cả HomeViewModel và GanttChartViewModel)
    public void deleteTask(int taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete("dev_task", "TASKID = ?", new String[]{String.valueOf(taskId)});
        db.delete("task", "ID = ?", new String[]{String.valueOf(taskId)});
        db.close();

        loadTasksFromDatabase();
    }

    // Search tasks by developer name or task ID
    public void searchTasks(String query) {
        List<DevTask> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT dt.ID, dt.DEV_NAME, dt.TASKID, dt.STARTDATE, dt.ENDDATE, t.TASK_NAME, t.ESTIMATE_DAY " +
                        "FROM dev_task dt " +
                        "JOIN task t ON dt.TASKID = t.ID " +
                        "WHERE dt.DEV_NAME LIKE ? OR t.TASK_NAME LIKE ? " +
                        "ORDER BY dt.TASKID",
                new String[]{"%" + query + "%", "%" + query + "%"}
        );

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("ID"));
                @SuppressLint("Range") String devName = cursor.getString(cursor.getColumnIndex("DEV_NAME"));
                @SuppressLint("Range") int taskId = cursor.getInt(cursor.getColumnIndex("TASKID"));
                @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex("TASK_NAME"));
                @SuppressLint("Range") String startDate = cursor.getString(cursor.getColumnIndex("STARTDATE"));
                @SuppressLint("Range") String endDate = cursor.getString(cursor.getColumnIndex("ENDDATE"));
                @SuppressLint("Range") int estimateDay = cursor.getInt(cursor.getColumnIndex("ESTIMATE_DAY"));

                DevTask task = new DevTask(id, taskName, devName, taskId, startDate, endDate, estimateDay);
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        taskList.setValue(tasks);
    }



}
