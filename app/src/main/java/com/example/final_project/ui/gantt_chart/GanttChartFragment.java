package com.example.final_project.ui.gantt_chart;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.final_project.DatabaseHelper;
import com.example.final_project.R;
import com.example.final_project.model.DevTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GanttChartFragment extends Fragment {

    private Button startDatePicker, endDatePicker;
    private GanttChartViewModel viewModel;
    private DatabaseHelper dbHelper;

    private TableLayout taskTable;

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gantt_chart, container, false);

        startDatePicker = view.findViewById(R.id.startDatePicker);
        endDatePicker = view.findViewById(R.id.endDatePicker);

        taskTable = view.findViewById(R.id.taskTable); // Lấy table cho các nhiệm vụ

        dbHelper = new DatabaseHelper(getContext());
        viewModel = new ViewModelProvider(this, new GanttChartViewModelFactory(dbHelper)).get(GanttChartViewModel.class);

        // Date picker listeners for start and end date
        startDatePicker.setOnClickListener(v -> showDatePickerDialog((date) -> {
            startCalendar.setTime(date);
            String formattedDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(startCalendar.getTime());
            startDatePicker.setText("Start Date: " + formattedDate);
            viewModel.setStartDate(formattedDate);
            viewModel.loadFilteredTasks();  // Gọi để tải các tác vụ đã lọc
            updateDateColumns(taskTable);  // Cập nhật cột ngày
            updateTaskRows(taskTable, viewModel.getTasks().getValue());
        }));

        endDatePicker.setOnClickListener(v -> showDatePickerDialog((date) -> {
            endCalendar.setTime(date);
            String formattedDate = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(endCalendar.getTime());
            endDatePicker.setText("End Date: " + formattedDate);
            viewModel.setEndDate(formattedDate);
            viewModel.loadFilteredTasks();  // Gọi để tải các tác vụ đã lọc
            updateDateColumns(taskTable);  // Cập nhật cột ngày
            updateTaskRows(taskTable, viewModel.getTasks().getValue());
        }));

        // Observe task list from ViewModel
        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> updateTaskRows(taskTable, tasks));

        return view;
    }

    private void showDatePickerDialog(OnDateSetListener listener) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, dayOfMonth);
                    listener.onDateSet(calendar.getTime()); // Pass the correct Date type
                },
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void updateDateColumns(TableLayout taskTable) {
        TableRow tableHeader = taskTable.findViewById(R.id.tableHeader);

        // Kiểm tra xem tableHeader có null không
        if (tableHeader == null) {
            return; // Nếu null, không làm gì cả
        }

        // Xóa tất cả các cột hiện tại, giữ lại header
        tableHeader.removeViews(1, tableHeader.getChildCount() - 1); // Remove existing date columns

        Calendar calendar = (Calendar) startCalendar.clone(); // Create a clone of the startCalendar

        // Generate dates from startDate to endDate
        while (!calendar.after(endCalendar)) {
            TextView dateColumn = new TextView(getContext());
            dateColumn.setLayoutParams(new TableRow.LayoutParams(100, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            dateColumn.setGravity(View.TEXT_ALIGNMENT_CENTER);
            dateColumn.setText(new SimpleDateFormat("MMM dd", Locale.getDefault()).format(calendar.getTime()));
            tableHeader.addView(dateColumn);
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Increment to the next day
        }

        // Thêm một cột cho devName
        TextView devNameHeader = new TextView(getContext());
        devNameHeader.setLayoutParams(new TableRow.LayoutParams(140, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        devNameHeader.setGravity(View.TEXT_ALIGNMENT_CENTER);
        devNameHeader.setText("Dev Name"); // Tiêu đề cho cột devName
        tableHeader.addView(devNameHeader);

        // Thêm một cột cho hành động (CheckBox)
        TextView actionHeader = new TextView(getContext());
        actionHeader.setLayoutParams(new TableRow.LayoutParams(120, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        actionHeader.setGravity(View.TEXT_ALIGNMENT_CENTER);
        actionHeader.setText("Action"); // Tiêu đề cho cột hành động
        tableHeader.addView(actionHeader);
    }


    private void updateTaskRows(TableLayout taskTable, List<DevTask> tasks) {
        // Xóa tất cả các hàng hiện tại, giữ lại header
        taskTable.removeViews(1, taskTable.getChildCount() - 1); // Remove all rows except header

        if (tasks == null || tasks.isEmpty()) {
            return; // Không có tác vụ để hiển thị
        }

        for (DevTask task : tasks) {
            TableRow taskRow = new TableRow(getContext());
            taskRow.setPadding(4, 4, 4, 4); // Padding cho mỗi dòng

            // Tạo TextView cho tên tác vụ
            TextView taskName = new TextView(getContext());
            taskName.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            taskName.setText(task.getTaskName());
            taskRow.addView(taskName);



            // Tính toán ngày bắt đầu và kết thúc của tác vụ
            Calendar taskStartCalendar = Calendar.getInstance();
            Calendar taskEndCalendar = Calendar.getInstance();
            try {
                taskStartCalendar.setTime(new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(task.getStartDate()));
                taskEndCalendar.setTime(new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(task.getEndDate()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Thêm các cột màu cho từng ngày trong khoảng thời gian của tác vụ
            Calendar currentCalendar = (Calendar) startCalendar.clone();
            while (!currentCalendar.after(endCalendar)) {
                TextView dateCell = new TextView(getContext());
                dateCell.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                dateCell.setGravity(View.TEXT_ALIGNMENT_CENTER);

                // Kiểm tra xem ngày hiện tại có nằm trong khoảng thời gian của tác vụ không
                if (!currentCalendar.before(taskStartCalendar) && !currentCalendar.after(taskEndCalendar)) {
                    dateCell.setBackgroundColor(getResources().getColor(R.color.purple_700)); // Màu cho thời gian tác vụ
                } else {
                    dateCell.setBackgroundColor(getResources().getColor(android.R.color.transparent)); // Không có tác vụ
                }

                dateCell.setText(""); // Có thể để trống hoặc hiển thị thông tin khác
                taskRow.addView(dateCell); // Thêm ô ngày vào hàng tác vụ
                currentCalendar.add(Calendar.DAY_OF_MONTH, 1); // Chuyển sang ngày tiếp theo
            }
            // Tạo TextView cho tên developer
            TextView devName = new TextView(getContext());
            devName.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            devName.setText(task.getDevName());
            taskRow.addView(devName); // Thêm devName vào hàng
            // Thêm Checkbox vào cột cuối cùng
            CheckBox taskCheckBox = new CheckBox(getContext());
            taskCheckBox.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            taskRow.addView(taskCheckBox); // Thêm checkbox vào cuối cùng của hàng

            taskTable.addView(taskRow); // Thêm hàng tác vụ vào bảng

            // Thiết lập sự kiện click cho checkbox
            taskCheckBox.setOnClickListener(v -> {
                // Hiển thị dialog xác nhận xóa
                showDeleteConfirmationDialog(task.getTaskId(), taskRow, taskCheckBox);
            });
        }
    }


    // Hàm hiển thị dialog xác nhận xóa
    private void showDeleteConfirmationDialog(int taskId, TableRow taskRow, CheckBox taskCheckBox) {
        new AlertDialog.Builder(getContext())
                .setTitle("Confirm delete")
                .setMessage("Are you sure to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    viewModel.deleteTask(taskId); // Gọi hàm xóa trong ViewModel
                    taskTable.removeView(taskRow); // Xóa dòng hiện tại khỏi bảng
                })
                .setNegativeButton("No", (dialog, which) -> {
                    taskCheckBox.setChecked(false); // Đặt lại checkbox về false
                })
                .show();
    }

    public interface OnDateSetListener {
        void onDateSet(Date date);
    }

    @Override
    public void onResume() {
        super.onResume();
        resetData(); // Gọi hàm reset dữ liệu
    }

    // Hàm reset dữ liệu
    private void resetData() {
        // Đặt lại các giá trị của startDatePicker và endDatePicker về trạng thái mặc định
        startDatePicker.setText("Start Date");
        endDatePicker.setText("End Date");

        // Xóa tất cả các hàng trong bảng (trừ header)
        if (taskTable.getChildCount() > 1) {
            taskTable.removeViews(1, taskTable.getChildCount() - 1);
        }

        // Đặt lại giá trị của các biến lịch
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        // Xóa dữ liệu trong ViewModel (nếu cần)
        viewModel.setStartDate(null);
        viewModel.setEndDate(null);
        viewModel.loadFilteredTasks();
    }

}
