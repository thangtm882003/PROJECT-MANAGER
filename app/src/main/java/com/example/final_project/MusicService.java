package com.example.final_project;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;

public class MusicService extends Service {

    private static final String TAG = "MusicService";
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        // Khởi tạo MediaPlayer và thiết lập nhạc
        mediaPlayer = MediaPlayer.create(this, R.raw.music); // Thay your_music_file bằng tên file nhạc trong thư mục res/raw
        mediaPlayer.setLooping(true); // Phát nhạc lặp lại
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Music Service Started");
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start(); // Bắt đầu phát nhạc
        }
        return START_STICKY; // Đảm bảo Service sẽ được tự động khởi động lại nếu bị hệ thống dừng
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release(); // Giải phóng tài nguyên
            Log.d(TAG, "Music Service Stopped");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
