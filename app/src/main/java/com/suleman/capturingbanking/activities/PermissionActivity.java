package com.suleman.capturingbanking.activities;

import static com.suleman.capturingbanking.Utlis.above13Check;
import static com.suleman.capturingbanking.Utlis.below13Check;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.suleman.capturingbanking.databinding.ActivityPermissionBinding;

public class PermissionActivity extends AppCompatActivity {
    ActivityPermissionBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustFontScale(PermissionActivity.this);
        binding = ActivityPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.policy.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.blueirissoft.com/privacy-policy")));
        });

        binding.notification.setVisibility(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                        View.VISIBLE : View.GONE
        );

        binding.grant.setOnClickListener(v -> askForPermissions());
    }

    private void askForPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (above13Check(this)) {
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.RECEIVE_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE);
                ActivityCompat.requestPermissions(this, permissions13, 2);
            } else {
                Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (below13Check(this)) {
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.RECEIVE_SMS);
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_PHONE_STATE);
                ActivityCompat.requestPermissions(this, permissions, 2);
            } else {
                Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED){
                startActivity(new Intent(PermissionActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "All permission are required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    String[] permissions13 = new String[]{
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.POST_NOTIFICATIONS,
    };
    String[] permissions = new String[]{
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECEIVE_SMS,
    };

    public static void adjustFontScale(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (configuration.fontScale > 1.00) {
            configuration.fontScale = 1.00f;
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(metrics);
            metrics.scaledDensity = configuration.fontScale * metrics.density;
            context.getResources().updateConfiguration(configuration, metrics);
        }
    }

}