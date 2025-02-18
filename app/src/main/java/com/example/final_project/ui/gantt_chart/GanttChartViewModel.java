package com.example.final_project.ui.gantt_chart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.final_project.DatabaseHelper;
import com.example.final_project.model.DevTask;

import java.util.ArrayList;
import java.util.List;



public class GanttChartViewModel extends ViewModel {
    private MutableLiveData<List<DevTask>> tasks;
    private String startDate, endDate;  // Thêm biến endDate
    private DatabaseHelper dbHelper;

    public GanttChartViewModel(DatabaseHelper dbHelper) {
        this.tasks = new MutableLiveData<>();
        this.dbHelper = dbHelper;
    }

    public LiveData<List<DevTask>> getTasks() {
        return tasks;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public String getStartDate() {
        return startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndDate() { // Phương thức getter cho endDate
        return endDate;
    }



    public void loadFilteredTasks() {
        if (startDate == null || endDate == null) {
            // Có thể hiển thị thông báo cho người dùng rằng họ cần chọn cả hai ngày

            tasks.setValue(new ArrayList<>()); // Cập nhật LiveData với danh sách rỗng
            return; // Không thực hiện truy vấn
        }

        List<DevTask> filteredTasks = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT dt.ID, dt.DEV_NAME, dt.TASKID, dt.STARTDATE, dt.ENDDATE, t.TASK_NAME, t.ESTIMATE_DAY " +
                        "FROM dev_task dt " +
                        "JOIN task t ON dt.TASKID = t.ID " +
                        "WHERE dt.STARTDATE >= ? AND dt.ENDDATE <= ? " +
                        "ORDER BY dt.TASKID",
                new String[]{startDate, endDate} // Sử dụng startDate và endDate
        );

        if (cursor.moveToFirst()) {
            do {
                // Lấy thông tin tác vụ và thêm vào danh sách
                @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex("ID"));
                @SuppressLint("Range") String devName = cursor.getString(cursor.getColumnIndex("DEV_NAME"));
                @SuppressLint("Range") int taskId = cursor.getInt(cursor.getColumnIndex("TASKID"));
                @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex("TASK_NAME"));
                @SuppressLint("Range") String startDate = cursor.getString(cursor.getColumnIndex("STARTDATE"));
                @SuppressLint("Range") String endDate = cursor.getString(cursor.getColumnIndex("ENDDATE"));
                @SuppressLint("Range") int estimateDay = cursor.getInt(cursor.getColumnIndex("ESTIMATE_DAY"));

                // Tạo DevTask mới
                DevTask task = new DevTask(id, taskName, devName, taskId, startDate, endDate, estimateDay);
                filteredTasks.add(task);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // Cập nhật LiveData với danh sách tác vụ đã lọc
        tasks.setValue(filteredTasks);
    }

    // Phương thức tải lại danh sách các task từ cơ sở dữ liệu

    // Phương thức xóa task trong cả hai ViewModel (áp dụng cho cả HomeViewModel và GanttChartViewModel)
    public void deleteTask(int taskId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Xóa nhiệm vụ từ bảng dev_task dựa trên TASKID
        db.delete("dev_task", "TASKID = ?", new String[]{String.valueOf(taskId)});

        // Xóa nhiệm vụ từ bảng task dựa trên TASKID
        db.delete("task", "ID = ?", new String[]{String.valueOf(taskId)});

        db.close();

        // Cập nhật lại LiveData sau khi xóa nhiệm vụ
        loadFilteredTasks(); // Tải lại danh sách các task từ cơ sở dữ liệu
    }

}
