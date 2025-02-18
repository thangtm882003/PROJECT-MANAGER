package com.example.final_project.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.final_project.MusicService;
import com.example.final_project.databinding.FragmentSettingBinding;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingFragment extends Fragment {

    private FragmentSettingBinding binding;
    private SwitchMaterial switchMusic, switchEstimateDay;  // Sử dụng SwitchMaterial
    private SharedPreferences sharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SettingViewModel settingViewModel =
                new ViewModelProvider(this).get(SettingViewModel.class);

        binding = FragmentSettingBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSetting;
        settingViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Thêm SharedPreferences để lưu trạng thái của Switch
        sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

        // Tham chiếu đến Switch Music trong layout
        switchMusic = binding.switchMusic;  // Kết nối với Switch Music trong XML

        // Lấy giá trị đã lưu trước đó và thiết lập trạng thái của Switch Music
        boolean isMusicPlaying = sharedPreferences.getBoolean("play_music", false);
        switchMusic.setChecked(isMusicPlaying);

        // Lắng nghe sự thay đổi của Switch Music
        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Lưu trạng thái mới vào SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("play_music", isChecked);
            editor.apply();

            // Phát hoặc dừng nhạc dựa trên trạng thái của Switch
            if (isChecked) {
                requireContext().startService(new Intent(requireContext(), MusicService.class));
                Toast.makeText(requireContext(), "Music is playing", Toast.LENGTH_SHORT).show();
            } else {
                requireContext().stopService(new Intent(requireContext(), MusicService.class));
                Toast.makeText(requireContext(), "Music is stopped", Toast.LENGTH_SHORT).show();
            }
        });

        // Tham chiếu đến Switch Estimate Day trong layout
        switchEstimateDay = binding.switchEstimateDay;  // Kết nối với Switch Estimate Day trong XML

        // Lấy giá trị đã lưu trước đó và thiết lập trạng thái của Switch Estimate Day
        boolean isEstimateDayVisible = sharedPreferences.getBoolean("show_estimate_day", true);
        switchEstimateDay.setChecked(isEstimateDayVisible);

        // Lắng nghe sự thay đổi của Switch Estimate Day
        switchEstimateDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Lưu trạng thái mới vào SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("show_estimate_day", isChecked);
            editor.apply();

            // Thông báo cho người dùng biết về trạng thái thay đổi
            if (isChecked) {
                Toast.makeText(getContext(), "Estimate Day is now shown", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Estimate Day is hidden", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
