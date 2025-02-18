package com.example.final_project.ui.search;

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

public class SearchViewModel extends ViewModel {
    private MutableLiveData<List<DevTask>> searchResults;
    private DatabaseHelper dbHelper;
    private MutableLiveData<Boolean> noResults = new MutableLiveData<>();
    // Getter cho noResults
    public LiveData<Boolean> getNoResults() {
        return noResults;
    }

    public SearchViewModel(Application application) {
        searchResults = new MutableLiveData<>();
        dbHelper = new DatabaseHelper(application);
    }

    public LiveData<List<DevTask>> getSearchResults() {
        return searchResults;
    }

    // Method to search tasks by developer name or task name
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
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("ID")); // ID from dev_task
                @SuppressLint("Range") String devName = cursor.getString(cursor.getColumnIndex("DEV_NAME")); // Developer name
                @SuppressLint("Range") int taskId = cursor.getInt(cursor.getColumnIndex("TASKID")); // Task ID
                @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex("TASK_NAME")); // Task name
                @SuppressLint("Range") String startDate = cursor.getString(cursor.getColumnIndex("STARTDATE")); // Start date
                @SuppressLint("Range") String endDate = cursor.getString(cursor.getColumnIndex("ENDDATE")); // End date
                @SuppressLint("Range") int estimateDay = cursor.getInt(cursor.getColumnIndex("ESTIMATE_DAY")); // Estimate day

                // Create DevTask object
                DevTask task = new DevTask(id, taskName, devName, taskId, startDate, endDate, estimateDay);
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // Kiểm tra nếu không có kết quả, thông báo không có kết quả
        if (tasks.isEmpty()) {
            noResults.setValue(true);
        } else {
            noResults.setValue(false);
        }

        // Update LiveData with search results
        searchResults.setValue(tasks);

    }
    public void updateTask(int id, String devName, int taskId, String taskName, int estimateDay, String startDate, String endDate) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Cập nhật bảng dev_task
        ContentValues devTaskValues = new ContentValues();
        devTaskValues.put("DEV_NAME", devName);
        devTaskValues.put("TASKID", taskId);
        devTaskValues.put("STARTDATE", startDate);
        devTaskValues.put("ENDDATE", endDate);

        db.update("dev_task", devTaskValues, "ID = ?", new String[]{String.valueOf(id)});

        // Cập nhật bảng task dựa trên taskId
        ContentValues taskValues = new ContentValues();
        taskValues.put("TASK_NAME", taskName); // Cập nhật tên task
        taskValues.put("ESTIMATE_DAY", estimateDay); // Cập nhật estimate day

        db.update("task", taskValues, "ID = ?", new String[]{String.valueOf(taskId)});

        db.close();
        loadTasksFromDatabase(); // Reload data after updating a task


    }
    public void deleteTask(int taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Xóa nhiệm vụ từ bảng dev_task dựa trên TASKID
        db.delete("dev_task", "TASKID = ?", new String[]{String.valueOf(taskId)});

        // Xóa nhiệm vụ từ bảng task dựa trên TASKID
        db.delete("task", "ID = ?", new String[]{String.valueOf(taskId)});

        db.close();

        loadTasksFromDatabase(); // Reload data after deleting a task
    }
    public void loadTasksFromDatabase() {
        List<DevTask> tasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Sử dụng JOIN để kết hợp thông tin từ hai bảng, bổ sung ESTIMATE_DAY từ bảng task
        Cursor cursor = db.rawQuery(
                "SELECT dt.ID, dt.DEV_NAME, dt.TASKID, dt.STARTDATE, dt.ENDDATE, t.TASK_NAME, t.ESTIMATE_DAY " +
                        "FROM dev_task dt " +
                        "JOIN task t ON dt.TASKID = t.ID " +
                        "ORDER BY dt.TASKID", null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("ID")); // ID từ dev_task
                @SuppressLint("Range") String devName = cursor.getString(cursor.getColumnIndex("DEV_NAME")); // Tên người được giao
                @SuppressLint("Range") int taskId = cursor.getInt(cursor.getColumnIndex("TASKID")); // ID nhiệm vụ
                @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex("TASK_NAME")); // Tên nhiệm vụ
                @SuppressLint("Range") String startDate = cursor.getString(cursor.getColumnIndex("STARTDATE")); // Ngày bắt đầu
                @SuppressLint("Range") String endDate = cursor.getString(cursor.getColumnIndex("ENDDATE")); // Ngày kết thúc
                @SuppressLint("Range") int estimateDay = cursor.getInt(cursor.getColumnIndex("ESTIMATE_DAY")); // Ngày ước lượng

                // Tạo DevTask với tên nhiệm vụ
                DevTask task = new DevTask(id,taskName, devName, taskId, startDate, endDate,estimateDay);
                tasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        // Cập nhật LiveData
        searchResults.setValue(tasks);
    }

    public void resetNoResults() {
        noResults.setValue(false); // Đặt lại giá trị sau khi Toast hiển thị
    }

}

