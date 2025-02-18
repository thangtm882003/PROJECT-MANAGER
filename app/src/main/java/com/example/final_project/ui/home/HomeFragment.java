package com.example.final_project.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_project.R;
import com.example.final_project.databinding.FragmentHomeBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.final_project.model.DevTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.text.TextWatcher;
import android.text.Editable;


public class HomeFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private TaskAdapter taskAdapter;
    private ArrayList<DevTask> taskList;
    private EditText editStartDate, editEndDate;
    private TextView estimateDayTextView;
    private SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize HomeViewModel
        homeViewModel = new ViewModelProvider(this, new HomeViewModelFactory(requireActivity().getApplication())).get(HomeViewModel.class);
        // Inflate layout for Fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Lấy trạng thái của Switch từ SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean isEstimateDayVisible = sharedPreferences.getBoolean("show_estimate_day", true);

        // Tham chiếu đến TextView Estimate Day
        estimateDayTextView = root.findViewById(R.id.estimate_day_textview);

        // Ẩn/hiển thị Estimate Day dựa trên trạng thái của Switch
        if (isEstimateDayVisible) {
            estimateDayTextView.setVisibility(View.VISIBLE);
        } else {
            estimateDayTextView.setVisibility(View.GONE);
        }

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(getContext(), taskList, this);
        binding.recyclerViewTasks.setAdapter(taskAdapter);
        RecyclerView recyclerView = binding.recyclerViewTasks;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Add DividerItemDecoration
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Observe data from HomeViewModel
        homeViewModel.getTaskList().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                taskList.clear();
                taskList.addAll(tasks);
                taskAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "No tasks available", Toast.LENGTH_SHORT).show();
            }
        });

        // Floating Action Button to add a new task
        FloatingActionButton fab = binding.fabAddTask;
        fab.setOnClickListener(v -> showAddTaskDialog());

        return root;
    }

    // Show dialog to add a new task
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        final EditText editTaskName = dialogView.findViewById(R.id.add_task_name);
        final EditText editEstimateDay = dialogView.findViewById(R.id.add_estimate_day);
        final EditText editDevName = dialogView.findViewById(R.id.add_dev_name);
        editStartDate = dialogView.findViewById(R.id.add_start_date);
        editEndDate = dialogView.findViewById(R.id.add_end_date);

        // Logic để disable/enable Estimate Day
        TextWatcher dateTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                String startDate = editStartDate.getText().toString().trim();
                String endDate = editEndDate.getText().toString().trim();

                // Nếu cả Start Date và End Date đã được nhập, vô hiệu hóa Estimate Day
                if (!startDate.isEmpty() && !endDate.isEmpty()) {
                    editEstimateDay.setText(""); // Clear nội dung của Estimate Day
                    editEstimateDay.setEnabled(false); // Vô hiệu hóa trường Estimate Day
                } else {
                    // Cho phép nhập Estimate Day nếu một trong hai ngày hoặc cả hai chưa được nhập
                    editEstimateDay.setEnabled(true);
                }
            }
        };

        // Thêm TextWatcher cho cả Start Date và End Date
        editStartDate.addTextChangedListener(dateTextWatcher);
        editEndDate.addTextChangedListener(dateTextWatcher);

        // Mở DatePickerDialog khi người dùng nhấn vào editStartDate hoặc editEndDate
        editStartDate.setOnClickListener(v -> showDatePickerDialog(editStartDate, null));
        editEndDate.setOnClickListener(v -> showDatePickerDialog(editEndDate, null));

        builder.setPositiveButton("Save", (dialog, which) -> {
            String taskName = editTaskName.getText().toString().trim();
            String devName = editDevName.getText().toString().trim();
            String startDate = editStartDate.getText().toString().trim();
            String endDate = editEndDate.getText().toString().trim();

            // Kiểm tra nếu task name rỗng
            if (taskName.isEmpty()) {
                Toast.makeText(getContext(), "Task name is required", Toast.LENGTH_LONG).show();
                return; // Không thực hiện lưu task nếu tên task chưa được nhập
            }

            int estimateDay = 0;
            if (!editEstimateDay.getText().toString().trim().isEmpty()) {
                try {
                    estimateDay = Integer.parseInt(editEstimateDay.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid estimate day", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Kiểm tra tên task không trùng
            if (isTaskNameDuplicate(taskName)) {
                Toast.makeText(getContext(), "Task name already exists", Toast.LENGTH_LONG).show();
                return;
            }

            // Kiểm tra logic Start Date và End Date
            Date parsedStartDate = parseDate(startDate);
            Date parsedEndDate = parseDate(endDate);

            // Kiểm tra nếu chỉ có End Date mà không có Start Date
            if (startDate.isEmpty() && !endDate.isEmpty()) {
                Toast.makeText(getContext(), "Please enter Start Date if End Date is set", Toast.LENGTH_LONG).show();
                return;
            }

            // Nếu người dùng đã nhập Start Date nhưng không nhập End Date
            if (!startDate.isEmpty() && endDate.isEmpty()) {
                Toast.makeText(getContext(), "Please enter End Date if Start Date is set", Toast.LENGTH_LONG).show();
                return;
            }

            // Kiểm tra nếu một trong hai ngày là null
            if (parsedStartDate != null && parsedEndDate != null) {
                if (parsedEndDate.before(parsedStartDate)) {
                    Toast.makeText(getContext(), "End Date cannot be before Start Date", Toast.LENGTH_LONG).show();
                    return;
                }

                // Tính toán Estimate Day tự động nếu Start Date và End Date có giá trị
                long diffInMillis = parsedEndDate.getTime() - parsedStartDate.getTime();
                estimateDay = (int) (diffInMillis / (1000 * 60 * 60 * 24) + 1); // Tính số ngày giữa hai ngày

                // Kiểm tra chồng lặp với các task hiện có chỉ khi có cả Start Date và End Date
                DevTask overlappingTask = getOverlappingTask(parsedStartDate, parsedEndDate, taskList, 0); // Kiểm tra chồng lặp
                if (overlappingTask != null) {
                    // Hiển thị thông báo chồng lặp nhưng vẫn cho phép thêm
                    String message = "Task " + taskName + " causes an overlap with task: " + overlappingTask.getTaskName() +
                            " (" + overlappingTask.getStartDate() + " - " + overlappingTask.getEndDate() + ")";
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    // Không return ở đây, tiếp tục thêm task
                }
            } else if (!editEstimateDay.getText().toString().trim().isEmpty()) {
                // Nếu Start Date và End Date trống nhưng Estimate Day được nhập
                try {
                    estimateDay = Integer.parseInt(editEstimateDay.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid estimate day", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                // Nếu không nhập cả Start Date, End Date, và Estimate Day
                Toast.makeText(getContext(), "Please enter either Estimate Day or Start/End Dates", Toast.LENGTH_LONG).show();
                return;
            }

            // Thêm task vào
            homeViewModel.addTask(taskName, estimateDay, devName, startDate, endDate);
            Toast.makeText(getContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }


    // Kiểm tra task name trùng lặp không phân biệt chữ hoa/chữ thường
    private boolean isTaskNameDuplicate(String taskName) {
        for (DevTask task : taskList) {
            if (task.getTaskName().equalsIgnoreCase(taskName)) {
                return true; // Task name đã tồn tại
            }
        }
        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        // Tải lại danh sách nhiệm vụ từ cơ sở dữ liệu khi Fragment hiển thị lại
        homeViewModel.loadTasksFromDatabase();
    }


    // Hàm kiểm tra chồng lặp thời gian
    private DevTask getOverlappingTask(Date newStartDate, Date newEndDate, List<DevTask> taskList, int currentTaskId) {
        for (DevTask task : taskList) {
            if (task.getId() == currentTaskId) {
                continue;
            }

            Date existingStartDate = parseDate(task.getStartDate());
            Date existingEndDate = parseDate(task.getEndDate());

            // Kiểm tra null trước khi thực hiện phép so sánh
            if (existingStartDate != null && existingEndDate != null) {
                if (newStartDate.before(existingEndDate) && newEndDate.after(existingStartDate)) {
                    return task; // Trả về task gây chồng lặp
                }
            }
        }
        return null; // Không có task nào chồng lặp
    }



    private Date parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        try {
            return format.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace(); // In ra log lỗi nếu có vấn đề trong quá trình parse
            return null; // Trả về null nếu không thể chuyển đổi
        }
    }


    private void showTaskDetailsDialog(DevTask task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_task_details, null);
        builder.setView(dialogView);

        EditText editTaskName = dialogView.findViewById(R.id.edit_task_name);
        EditText editDevName = dialogView.findViewById(R.id.edit_assignee);
        EditText editEstimateDay = dialogView.findViewById(R.id.edit_estimate_day);
        EditText editStartDate = dialogView.findViewById(R.id.edit_start_date);
        EditText editEndDate = dialogView.findViewById(R.id.edit_end_date);

        // Hiển thị thông tin của task
        editTaskName.setText(task.getTaskName());
        editDevName.setText(task.getDevName());
        editStartDate.setText(task.getStartDate());
        editEstimateDay.setText(String.valueOf(task.getEstimateDay()));
        editEndDate.setText(task.getEndDate());

        // Mở DatePickerDialog khi người dùng nhấn vào editStartDate hoặc editEndDate
        editStartDate.setOnClickListener(v -> showDatePickerDialog(editStartDate, task.getStartDate()));
        editEndDate.setOnClickListener(v -> showDatePickerDialog(editEndDate, task.getEndDate()));

        // Xử lý sự kiện cho nút Update
        builder.setPositiveButton("Update", null);  // Đặt null để xử lý sự kiện sau khi dialog được tạo

        // Xử lý sự kiện cho nút Delete
        builder.setNegativeButton("Delete", (dialog, which) -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        homeViewModel.deleteTask(task.getTaskId());
                        Toast.makeText(getContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        builder.setNeutralButton("Cancel", null);  // Hủy dialog chính

        // Tạo dialog và thêm sự kiện cho nút Update
        AlertDialog dialog = builder.create();
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Update", (dialogInterface, which) -> {
            // Cập nhật thông tin khi nhấn nút Update
            String updatedTaskName = editTaskName.getText().toString().trim();
            String updatedDevName = editDevName.getText().toString().trim();
            String updatedStartDate = editStartDate.getText().toString().trim();
            String updatedEndDate = editEndDate.getText().toString().trim();
            int updatedEstimateDay;
            try {
                updatedEstimateDay = Integer.parseInt(editEstimateDay.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Invalid estimate day", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra ràng buộc: End Date phải có Start Date (nếu người dùng nhập End Date)
            if (!updatedEndDate.isEmpty() && updatedStartDate.isEmpty()) {
                Toast.makeText(getContext(), "Start Date is required if End Date is set", Toast.LENGTH_LONG).show();
                return; // Không cho phép cập nhật nếu chỉ có End Date
            }

            // Chuyển đổi chuỗi thành đối tượng Date
            Date parsedStartDate = parseDate(updatedStartDate);
            Date parsedEndDate = parseDate(updatedEndDate);

            // Kiểm tra ràng buộc: End Date phải >= Start Date
            if (parsedStartDate != null && parsedEndDate != null && parsedEndDate.before(parsedStartDate)) {
                Toast.makeText(getContext(), "End Date cannot be before Start Date", Toast.LENGTH_LONG).show();
                return; // Không cho phép cập nhật nếu End Date nhỏ hơn Start Date
            }

            // Sử dụng hàm kiểm tra chồng lặp trước khi cập nhật (nếu có StartDate và EndDate)
            if (parsedStartDate != null && parsedEndDate != null) {
                DevTask overlappingTask = getOverlappingTask(parsedStartDate, parsedEndDate, taskList, task.getId());
                if (overlappingTask != null) {
                    // Hiển thị thông báo nhưng vẫn cho phép cập nhật
                    String message = "Task " + updatedTaskName + " causes an overlap to task: "
                            + overlappingTask.getTaskName() + " ("
                            + overlappingTask.getStartDate() + " - "
                            + overlappingTask.getEndDate() + ")";
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            }

            // Kiểm tra tên task đã tồn tại hay chưa (không phân biệt hoa thường)
            for (DevTask existingTask : taskList) {
                if (existingTask.getId() != task.getId() && existingTask.getTaskName().equalsIgnoreCase(updatedTaskName)) {
                    Toast.makeText(getContext(), "Task name already exists. Please choose a different name.", Toast.LENGTH_LONG).show();
                    return; // Không cho phép cập nhật nếu tên task bị trùng
                }
            }

            // Cập nhật thông tin vào ViewModel
            homeViewModel.updateTask(
                    task.getId(),              // id: ID từ bảng dev_task
                    updatedDevName,            // devName: Tên người được giao nhiệm vụ
                    task.getTaskId(),          // taskId: ID nhiệm vụ
                    updatedTaskName,           // taskName: Tên nhiệm vụ đã cập nhật
                    updatedEstimateDay,        // estimateDay: Số ngày dự tính hoàn thành
                    updatedStartDate,          // startDate: Ngày bắt đầu đã chỉnh sửa
                    updatedEndDate             // endDate: Ngày kết thúc đã chỉnh sửa
            );
            Toast.makeText(getContext(), "Task updated successfully", Toast.LENGTH_SHORT).show();
        });

        dialog.show();  // Hiển thị dialog
    }



    // Hiển thị DatePickerDialog để chọn ngày
    private void showDatePickerDialog(EditText editText, @Nullable String initialDate) {
        final Calendar calendar = Calendar.getInstance();

        // Nếu có ngày ban đầu (khi update), lấy năm, tháng, ngày từ đó
        if (initialDate != null && !initialDate.isEmpty()) {
            try {
                String[] parts = initialDate.split("/");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]) - 1; // Tháng bắt đầu từ 0
                int day = Integer.parseInt(parts[2]);
                calendar.set(year, month, day);
            } catch (Exception e) {
                // Nếu có lỗi parsing ngày, chúng ta sẽ mặc định ngày hiện tại
                Toast.makeText(getContext(), "Invalid date format, using current date", Toast.LENGTH_SHORT).show();
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Mở DatePickerDialog để chọn ngày
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            // Chuyển đổi thành định dạng yyyy/MM/dd
            String selectedDate = String.format(Locale.getDefault(), "%04d/%02d/%02d", year1, month1 + 1, dayOfMonth);
            editText.setText(selectedDate); // Cập nhật ngày vào EditText
        }, year, month, day);

        datePickerDialog.show();
    }




    @Override
    public void onTaskClick(DevTask task) {
        showTaskDetailsDialog(task); // Hiển thị thông tin chi tiết của task khi nhấn
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clear binding when fragment is destroyed
    }
}
