package com.suleman.capturingbanking.activities;

import static com.suleman.capturingbanking.utilies.Utils.above13Check;
import static com.suleman.capturingbanking.utilies.Utils.below13Check;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.fxn.stash.Stash;
import com.google.android.material.textfield.TextInputLayout;
import com.suleman.capturingbanking.BuildConfig;
import com.suleman.capturingbanking.R;
import com.suleman.capturingbanking.api.ResponseCallback;
import com.suleman.capturingbanking.api.ServerConnector;
import com.suleman.capturingbanking.api.response_models.CreateDeviceRequest;
import com.suleman.capturingbanking.databinding.ActivityMainBinding;
import com.suleman.capturingbanking.model.Department;
import com.suleman.capturingbanking.model.DeviceModel;
import com.suleman.capturingbanking.model.DeviceRecord;
import com.suleman.capturingbanking.model.NewDeviceRecord;
import com.suleman.capturingbanking.services.MessageReceiver;
import com.suleman.capturingbanking.services.MessageReceiverUpdated;
import com.suleman.capturingbanking.utilies.InAppUpdateHelper;
import com.suleman.capturingbanking.utilies.NetworkUtil;
import com.suleman.capturingbanking.utilies.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    ProgressDialog progressDialog;
    String device;
    ArrayList<String> departments = new ArrayList<>();
    ArrayList<Department> departmentsID = new ArrayList<>();
    private InAppUpdateHelper inAppUpdateHelper;
    ActivityResultLauncher activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustFontScale(MainActivity.this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Utils.checkApp(this);

        binding.version.setText(BuildConfig.VERSION_NAME);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (above13Check(this)) {
                startActivity(new Intent(this, PermissionActivity.class));
                finish();
            }
        } else {
            if (below13Check(this)) {
                startActivity(new Intent(this, PermissionActivity.class));
                finish();
            }
        }

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() != RESULT_OK) {
                    Log.d("HELLO", "Update flow failed! Result code: " + result.getResultCode());
                }
            }
        });

        inAppUpdateHelper = new InAppUpdateHelper(this, activityResultLauncher);
        inAppUpdateHelper.checkForUpdate();

        init();
        if (NetworkUtil.isNetworkAvailable(this)) authenticateUser();
        else {
            binding.errorLayout.setVisibility(View.VISIBLE);
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.save.setOnClickListener(v -> {
            if (!binding.save.getText().toString().equals("Edit")) {
                if (valid()) {
                    fetchData(binding.accountNumber.getEditText().getText().toString(), true);
                }
            } else {
                binding.save.setText("Save Info");
                enableUI(true);
                binding.accountNumber.setEnabled(false);
            }
        });
        binding.failed.setOnClickListener(v -> {
            startActivity(new Intent(this, FailedMessagesActivity.class));
        });
        binding.restore.setOnClickListener(v -> {
            restoreData();
        });
        binding.retrySeconds.setOnClickListener(v -> {
            retry();
        });
        binding.retry.setOnClickListener(v -> {
            recreate();
        });
        binding.refreshData.setOnClickListener(v -> {
            if (device == null) {
                Toast.makeText(this, "Device ID Not Found", Toast.LENGTH_SHORT).show();
                return;
            }
            refreshData();
        });
        binding.refresh.setOnClickListener(v -> {
            getDepartmentList();
        });
        binding.policy.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.blueirissoft.com/privacy-policy")));
        });
    }

    private void retry() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.retry_threshold);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        Button restore = dialog.findViewById(R.id.restore);
        Button close = dialog.findViewById(R.id.close);
        TextInputLayout account = dialog.findViewById(R.id.account);

        account.getEditText().setText(String.valueOf(Stash.getInt(MessageReceiverUpdated.RETRY_DELAY, MessageReceiverUpdated.RETRY_DELAY_MS)));

        close.setOnClickListener(v -> dialog.dismiss());

        restore.setOnClickListener(v -> {
            if (account.getEditText().getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter valid number", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
                Stash.put(MessageReceiverUpdated.RETRY_DELAY, Integer.parseInt(account.getEditText().getText().toString()));
            }
        });
    }

    private void refreshData() {
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        ServerConnector.getInstance(false).getDeviceRecordById(device, new ResponseCallback() {
            @Override
            public void onSuccess(Object response) {
                progressDialog.dismiss();
                if (response instanceof String) {
                    DeviceModel deviceModel = new DeviceModel();
                    deviceModel.setEmpty();
                    MainActivity.this.device = null;
                    updateUI(deviceModel);
                } else {
                    if (response instanceof DeviceRecord record) {
                        DeviceModel deviceModel = getDevice(record);
                        MainActivity.this.device = deviceModel.device_ID;
                        updateUI(deviceModel);
                    }
                }
            }

            @Override
            public void onError(String response) {
                progressDialog.dismiss();
                showToast(response);
            }
        });
    }

    private void init() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Authenticating Please wait...");
        progressDialog.setCancelable(false);

        //  binding.accountNumber.getEditText().setFilters(new InputFilter[]{alphanumericFilter});
        DeviceModel deviceModel = (DeviceModel) Stash.getObject(MessageReceiver.INFO, DeviceModel.class);
        if (deviceModel != null && !deviceModel.isAllEmpty()) {
            setText(deviceModel);
            enableUI(false);
            this.device = deviceModel.device_ID;
            binding.save.setText("Edit");
        }
    }

    private void enableUI(boolean b) {
        binding.deviceName.setEnabled(b);
        binding.bankName.setEnabled(b);
        binding.accountNumber.setEnabled(b);
        binding.accountTitle.setEnabled(b);
        binding.department.setEnabled(b);
    }

    private void authenticateUser() {
        progressDialog.show();
        ServerConnector.getInstance(false).loginUser(new ResponseCallback() {
            @Override
            public void onSuccess(Object response) {
                getDepartmentList();
            }

            @Override
            public void onError(String response) {
                progressDialog.dismiss();
                binding.errorLayout.setVisibility(View.VISIBLE);
                binding.description.setText(response);
                showToast(response);
            }
        });
    }

    private void restoreData() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.account_number);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();

        Button restore = dialog.findViewById(R.id.restore);
        Button close = dialog.findViewById(R.id.close);
        TextInputLayout account = dialog.findViewById(R.id.account);

        //account.getEditText().setFilters(new InputFilter[]{alphanumericFilter});

        close.setOnClickListener(v -> dialog.dismiss());

        restore.setOnClickListener(v -> {
            if (account.getEditText().getText().toString().isEmpty()) {
                Toast.makeText(this, "Account number is empty", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
                fetchData(account.getEditText().getText().toString(), false);
            }
        });
    }

    private void fetchData(String accountNumber, boolean check) {
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        ServerConnector.getInstance(false).getDeviceRecord(accountNumber, new ResponseCallback() {
            @Override
            public void onSuccess(Object response) {
                progressDialog.dismiss();
                if (check) {
                    if (response instanceof String s) {
                        create_device();
                    } else {
                        if (response instanceof DeviceRecord record) {
                            MainActivity.this.device = record.getId();
                            updateInfo();
                        }
                    }
                } else {
                    if (response instanceof DeviceRecord record) {
                        DeviceModel deviceModel = getDevice(record);
                        MainActivity.this.device = record.getId();
                        updateUI(deviceModel);
                    }
                }
            }

            @Override
            public void onError(String response) {
                progressDialog.dismiss();
                if (check) {
                    create_device();
                } else {
                    showToast(response);
                }
            }
        });
    }

    private DeviceModel getDevice(DeviceRecord record) {
        DeviceModel deviceModel = new DeviceModel();
        deviceModel.device = record.getName();
        deviceModel.bankName = record.getBankName();
        deviceModel.accountTitle = record.getAccountTitle();
        deviceModel.accountNumber = record.getAccountNumber();
        deviceModel.device_ID = record.getId();
        deviceModel.department = record.getDepartment().getName();
        deviceModel.department_ID = record.getDepartment().getId();
        return deviceModel;
    }

    private void updateUI(DeviceModel deviceModel) {
        Stash.put(MessageReceiver.INFO, deviceModel);
        Toast.makeText(this, "Data Restored", Toast.LENGTH_SHORT).show();
        enableUI(false);
        binding.save.setText("Edit");
        setText(deviceModel);
    }

    private void setText(DeviceModel deviceModel) {
        binding.deviceName.getEditText().setText(deviceModel.device);
        binding.bankName.getEditText().setText(deviceModel.bankName);
        binding.accountNumber.getEditText().setText(deviceModel.accountNumber);
        binding.accountTitle.getEditText().setText(deviceModel.accountTitle);
        binding.department.getEditText().setText(deviceModel.department);
    }

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

    private void updateInfo() {
        CreateDeviceRequest request;
        try {
            request = getCreateDeviceData(device);
        } catch (Exception e) {
            showToast(e.getMessage());
            progressDialog.dismiss();
            binding.department.getEditText().setText("");
            return;
        }

        ServerConnector.getInstance(false).updateDevice(
                request,
                new ResponseCallback() {
                    @Override
                    public void onSuccess(Object response) {
                        progressDialog.dismiss();
                        if (response instanceof NewDeviceRecord record) {
                            device = record.getId();
                            savedInfo();
                        } else if (response instanceof String s) {
                            showToast(s);
                        }
                    }

                    @Override
                    public void onError(String response) {
                        progressDialog.dismiss();
                        showToast(response);
                    }
                }
        );
    }

    private void create_device() {
        progressDialog.setMessage("Creating New Device...");
        progressDialog.show();
        CreateDeviceRequest request;

        try {
            request = getCreateDeviceData("");
        } catch (Exception e) {
            showToast(e.getMessage());
            progressDialog.dismiss();
            binding.department.getEditText().setText("");
            return;
        }

        ServerConnector.getInstance(false).createDevice(
                request,
                new ResponseCallback() {
                    @Override
                    public void onSuccess(Object response) {
                        progressDialog.dismiss();
                        if (response instanceof NewDeviceRecord record) {
                            device = record.getId();
                            savedInfo();
                        } else if (response instanceof String s) {
                            showToast(s);
                        }
                    }

                    @Override
                    public void onError(String response) {
                        progressDialog.dismiss();
                        showToast(response);
                    }
                }
        );
    }

    private CreateDeviceRequest getCreateDeviceData(String deviceID) throws Exception {
        String department_ID = getDepartmentID();
        if (department_ID.isEmpty()) {
            throw new Exception("Department is invalid");
        }
        return new CreateDeviceRequest(
                deviceID,
                binding.deviceName.getEditText().getText().toString().trim(),
                binding.bankName.getEditText().getText().toString().trim(),
                binding.accountTitle.getEditText().getText().toString().trim(),
                binding.accountNumber.getEditText().getText().toString().trim(),
                department_ID
        );
    }

    private String getDepartmentID() {
        String department_ID = "";
        for (Department department : departmentsID) {
            if (department.getName().equals(binding.department.getEditText().getText().toString().trim())) {
                department_ID = department.getId();
                break;
            }
        }
        return department_ID;
    }

    private void savedInfo() {
        DeviceModel deviceModel = new DeviceModel();
        deviceModel.device_ID = device;
        Log.d(TAG, "savedInfo: " + device);
        Log.d(TAG, "savedInfo: " + deviceModel.device_ID);
        deviceModel.department_ID = getDepartmentID();
        if (deviceModel.department_ID.isEmpty()) {
            progressDialog.dismiss();
            binding.department.getEditText().setText("");
            Toast.makeText(this, "Department is invalid", Toast.LENGTH_SHORT).show();
            return;
        }
        deviceModel.device = binding.deviceName.getEditText().getText().toString();
        deviceModel.bankName = binding.bankName.getEditText().getText().toString();
        deviceModel.accountTitle = binding.accountTitle.getEditText().getText().toString();
        deviceModel.accountNumber = binding.accountNumber.getEditText().getText().toString();
        deviceModel.department = binding.department.getEditText().getText().toString();
        Stash.put(MessageReceiver.INFO, deviceModel);
        Toast.makeText(this, "Info saved", Toast.LENGTH_SHORT).show();
        enableUI(false);
        binding.save.setText("Edit");
    }

    private void showToast(String response) {
        Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
    }

    private void getDepartmentList() {
        progressDialog.setMessage("Getting Department List");
        progressDialog.show();
        ServerConnector.getInstance(false).getDepartments(new ResponseCallback() {
            @Override
            public void onSuccess(Object response) {
                progressDialog.dismiss();
                if (response instanceof List) {
                    List<Department> departments = (List<Department>) response;
                    setDepartmentAdapter(departments);
                }
            }

            @Override
            public void onError(String response) {
                progressDialog.dismiss();
                showToast(response);
            }
        });
    }

    private void setDepartmentAdapter(List<Department> departmentList) {
        departmentsID.clear();
        departments.clear();
        departmentsID.addAll(departmentList);

        for (Department department : departmentList) {
            departments.add(department.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_dropdown_item_1line, departments);
        binding.departmentList.setAdapter(adapter);
    }

    private boolean valid() {
        if (binding.deviceName.getEditText().getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Device name is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.bankName.getEditText().getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Bank name is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.accountTitle.getEditText().getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Account Title is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.accountNumber.getEditText().getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Account number is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.department.getEditText().getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Department is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    InputFilter alphanumericFilter = (source, start, end, dest, dstart, dend) -> {
        if (source.toString().matches("[a-zA-Z0-9]*")) {
            return null;
        }
        return "";
    };

    private static final String TAG = "MainActivity";

}