package com.suleman.capturingbanking.activities;

import static com.suleman.capturingbanking.utilies.Utils.TOKEN;
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
import com.suleman.capturingbanking.R;
import com.suleman.capturingbanking.api.API;
import com.suleman.capturingbanking.api.ResponseCallback;
import com.suleman.capturingbanking.databinding.ActivityMainBinding;
import com.suleman.capturingbanking.model.Department;
import com.suleman.capturingbanking.model.DeviceModel;
import com.suleman.capturingbanking.services.MessageReceiver;
import com.suleman.capturingbanking.utilies.InAppUpdateHelper;
import com.suleman.capturingbanking.utilies.NetworkUtils;
import com.suleman.capturingbanking.utilies.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    ProgressDialog progressDialog;
    String device;
    ArrayList<String> departments = new ArrayList<>();
    ArrayList<Department> departmentsID = new ArrayList<>();
    public String token = "";
    private InAppUpdateHelper inAppUpdateHelper;
    ActivityResultLauncher activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustFontScale(MainActivity.this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Utils.checkApp(this);

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

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() != RESULT_OK) {
                            Log.d("HELLO", "Update flow failed! Result code: " + result.getResultCode());
                        }
                    }
                }
        );

        inAppUpdateHelper = new InAppUpdateHelper(this, activityResultLauncher);
        inAppUpdateHelper.checkForUpdate();

        init();
        if (NetworkUtils.isInternetAvailable(this))
            authenticateUser();
        else {
            binding.errorLayout.setVisibility(View.VISIBLE);
        }

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

        binding.restore.setOnClickListener(v -> {
            restoreData();
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

    private void refreshData() {
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();
        API.getInstance(this).restoreDevice(device, new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                progressDialog.dismiss();
                try {
                    String message = response.getString("message");
                    if (message.equals(getString(R.string.succes_message))) {
                        JSONObject result = response.getJSONObject("result");
                        DeviceModel deviceModel = getDevice(result);
                        MainActivity.this.device = deviceModel.device_ID;
                        updateUI(deviceModel);
                    } else {
                        DeviceModel deviceModel = new DeviceModel();
                        deviceModel.setEmpty();
                        MainActivity.this.device = null;
                        updateUI(deviceModel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String response) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Authenticating Please wait...");
        progressDialog.setCancelable(false);

        binding.accountNumber.getEditText().setFilters(new InputFilter[]{alphanumericFilter});
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
        try {
            API.getInstance(this).authenticateUser(new ResponseCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    progressDialog.dismiss();
                    try {
                        token = response.getString("token");
                        Stash.put(TOKEN, token);
                        getDepartmentList();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String response) {
                    progressDialog.dismiss();
                    binding.errorLayout.setVisibility(View.VISIBLE);
                    binding.description.setText(response);
                    Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            e.printStackTrace();
        }
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

        account.getEditText().setFilters(new InputFilter[]{alphanumericFilter});

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
        API.getInstance(this).fetchData(accountNumber, new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                progressDialog.dismiss();
                handleFetchData(response, check);
            }

            @Override
            public void onError(String response) {
                if (check) {
                    create_device();
                } else {
                    Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleFetchData(JSONObject response, boolean check) {
        try {
            String message = response.getString("message");
            Log.d(TAG, "handleFetchData: " + message);
            if (check) {
                if (message.equals(getString(R.string.fail_message))) {
                    create_device();
                } else {
                    JSONObject result = response.getJSONObject("result");
                    MainActivity.this.device = result.getString("_id");
                    updateInfo();
                }
            } else {
                if (message.equals(getString(R.string.succes_message))) {
                    JSONObject result = response.getJSONObject("result");
                    DeviceModel deviceModel = getDevice(result);
                    MainActivity.this.device = deviceModel.device_ID;
                    updateUI(deviceModel);
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DeviceModel getDevice(JSONObject result) throws Exception {
        DeviceModel deviceModel = new DeviceModel();
        deviceModel.device = result.getString("name");
        deviceModel.bankName = result.getString("bank_name");
        deviceModel.accountTitle = result.getString("account_title");
        deviceModel.accountNumber = result.getString("account_number");
        Log.d(TAG, "getDevice: " + deviceModel.accountNumber);
        deviceModel.device_ID = result.getString("_id");
        deviceModel.department = result.getJSONObject("department").getString("name");
        deviceModel.department_ID = result.getJSONObject("department").getString("_id");
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
        try {
            JSONObject json = getUpdateInfo();
            API.getInstance(this).updateDevice(json, new ResponseCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    progressDialog.dismiss();
                    try {
                        JSONObject result = response.getJSONObject("result");
                        device = result.getString("_id");
                        savedInfo();
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        });
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String response) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            binding.department.getEditText().setText("");
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private JSONObject getUpdateInfo() throws Exception {
        JSONObject json = new JSONObject();
        json.put("device_id", this.device);
        json.put("device", binding.deviceName.getEditText().getText().toString().trim());
        json.put("bank_name", binding.bankName.getEditText().getText().toString().trim());
        json.put("account_title", binding.accountTitle.getEditText().getText().toString().trim());
        json.put("account_number", binding.accountNumber.getEditText().getText().toString().trim());
        String department_ID = getDepartmentID();
        if (department_ID.isEmpty()) {
            throw new Exception("Department is invalid");
        }
        json.put("department", department_ID);
        return json;
    }

    private void create_device() {
        progressDialog.setMessage("Creating New Device...");
        progressDialog.show();
        try {
            JSONObject json = getCreateDeviceData();
            API.getInstance(this).createDevice(json, new ResponseCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    progressDialog.dismiss();
                    try {
                        JSONObject result = response.getJSONObject("result");
                        device = result.getString("_id");
                        savedInfo();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String response) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            binding.department.getEditText().setText("");
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private JSONObject getCreateDeviceData() throws Exception {
        JSONObject json = new JSONObject();
        json.put("device", binding.deviceName.getEditText().getText().toString().trim());
        json.put("bank_name", binding.bankName.getEditText().getText().toString().trim());
        json.put("account_title", binding.accountTitle.getEditText().getText().toString().trim());
        json.put("account_number", binding.accountNumber.getEditText().getText().toString().trim());
        String department_ID = getDepartmentID();
        if (department_ID.isEmpty()) {
            throw new Exception("Department is invalid");
        }
        json.put("department", department_ID);
        return json;
    }

    private String getDepartmentID() {
        String department_ID = "";
        for (Department department : departmentsID) {
            if (department.name.equals(binding.department.getEditText().getText().toString().trim())) {
                department_ID = department.id;
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

    private void getDepartmentList() {
        progressDialog.setMessage("Getting Department List");
        progressDialog.show();
        API.getInstance(this).getDepartmentList(new ResponseCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                progressDialog.dismiss();
                try {
                    JSONArray result = response.getJSONArray("result");
                    setDepartmentAdapter(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Failed to fetch department list", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String response) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setDepartmentAdapter(JSONArray result) throws Exception {
        departmentsID.clear();
        departments.clear();
        for (int i = 0; i < result.length(); i++) {
            JSONObject object = result.getJSONObject(i);
            departmentsID.add(new Department(object.getString("_id"), object.getString("name")));
            departments.add(object.getString("name"));
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