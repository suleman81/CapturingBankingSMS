package com.suleman.capturingbanking.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fxn.stash.Stash;
import com.google.android.material.textfield.TextInputLayout;
import com.suleman.capturingbanking.API;
import com.suleman.capturingbanking.R;
import com.suleman.capturingbanking.databinding.ActivityMainBinding;
import com.suleman.capturingbanking.model.Department;
import com.suleman.capturingbanking.model.DeviceModel;
import com.suleman.capturingbanking.services.MessageReceiver;
import com.suleman.capturingbanking.services.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ProgressDialog progressDialog;
    String device;
    ArrayList<String> departments = new ArrayList<>();
    ArrayList<Department> departmentsID = new ArrayList<>();
    ArrayList<Department> devices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adjustFontScale(MainActivity.this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting Department List...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (above13Check()) {
                startActivity(new Intent(this, PermissionActivity.class));
            }
        } else {
            if (below13Check()) {
                startActivity(new Intent(this, PermissionActivity.class));
            }
        }

        binding.restore.setOnClickListener(v -> {
            restoreData();
        });

        binding.accountNumber.getEditText().setFilters(new InputFilter[]{alphanumericFilter});

        getDepartmentList();
        getDevices();

        if (Stash.getObject(MessageReceiver.INFO, DeviceModel.class) != null) {
            DeviceModel deviceModel = (DeviceModel) Stash.getObject(MessageReceiver.INFO, DeviceModel.class);
            binding.deviceName.getEditText().setText(deviceModel.device);
            binding.bankName.getEditText().setText(deviceModel.bankName);
            binding.accountNumber.getEditText().setText(deviceModel.accountNumber);
            binding.accountTitle.getEditText().setText(deviceModel.accountTitle);
            binding.department.getEditText().setText(deviceModel.department);

            binding.deviceName.setEnabled(false);
            binding.bankName.setEnabled(false);
            binding.accountNumber.setEnabled(false);
            binding.accountTitle.setEnabled(false);
            binding.department.setEnabled(false);
            binding.save.setText("Edit");
        }

        binding.save.setOnClickListener(v -> {
            if (!binding.save.getText().toString().equals("Edit")) {
                if (valid()) {
                    fetchData(binding.accountNumber.getEditText().getText().toString(), true);
                }
            } else {
                binding.save.setText("Save Info");
                binding.deviceName.setEnabled(true);
                binding.bankName.setEnabled(true);
                binding.accountNumber.setEnabled(false);
                binding.accountTitle.setEnabled(true);
                binding.department.setEnabled(true);
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

        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, API.getRestoreLink(accountNumber), null, response -> {
            progressDialog.dismiss();
            try {
                JSONObject result = response.getJSONObject("result");
                String message = response.getString("message");
                if (check) {
                    if (message.equals("Record not found")) {
                        progressDialog.setMessage("Creating New Device...");
                        progressDialog.show();
                        create_device();
                    } else {
                        this.device = result.getString("_id");
                        updateInfo();
                    }
                } else {
                    if (!message.equals("Record not found")) {
                        DeviceModel deviceModel = new DeviceModel();
                        deviceModel.device = result.getString("name");
                        deviceModel.bankName = result.getString("bank_name");
                        deviceModel.accountTitle = result.getString("account_title");
                        deviceModel.accountNumber = result.getString("account_number");
                        deviceModel.device_ID = result.getString("_id");
                        deviceModel.department = result.getJSONObject("department").getString("name");
                        deviceModel.department_ID = result.getJSONObject("department").getString("_id");

                        this.device = deviceModel.device_ID;

                        updateUI(deviceModel);
                    } else {
                        Toast.makeText(this, "Account number is invalid", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.d(TAG, "fetchData: " + error.getLocalizedMessage());
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Account number is invalid", Toast.LENGTH_SHORT).show();
//                progressDialog.setMessage("Creating New Device...");
//                progressDialog.show();
//                create_device();
            });
        });
        requestQueue.add(objectRequest);

    }

    private void updateUI(DeviceModel deviceModel) {
        Stash.put(MessageReceiver.INFO, deviceModel);
        Toast.makeText(this, "Data Restored", Toast.LENGTH_SHORT).show();
        binding.deviceName.setEnabled(false);
        binding.bankName.setEnabled(false);
        binding.accountNumber.setEnabled(false);
        binding.accountTitle.setEnabled(false);
        binding.department.setEnabled(false);
        binding.save.setText("Edit");

        binding.deviceName.getEditText().setText(deviceModel.device);
        binding.bankName.getEditText().setText(deviceModel.bankName);
        binding.accountNumber.getEditText().setText(deviceModel.accountNumber);
        binding.accountTitle.getEditText().setText(deviceModel.accountTitle);
        binding.department.getEditText().setText(deviceModel.department);

        getDevices();
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
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        JSONObject json = new JSONObject();
        try {
            json.put("device_id", this.device);
            json.put("device", binding.deviceName.getEditText().getText().toString().trim());
            json.put("bank_name", binding.bankName.getEditText().getText().toString().trim());
            json.put("account_title", binding.accountTitle.getEditText().getText().toString().trim());
            json.put("account_number", binding.accountNumber.getEditText().getText().toString().trim());
            String department_ID = getDepartmentID();
            if (department_ID.isEmpty()) {
                progressDialog.dismiss();
                binding.department.getEditText().setText("");
                Toast.makeText(this, "Department is invalid", Toast.LENGTH_SHORT).show();
                return;
            }
            json.put("department", department_ID);
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, API.getLink("device-update"), json,
                    response -> {
                        progressDialog.dismiss();
                        Log.d("TOKEN_CHECK", "Response DEVICE : " + response.toString());
                        try {
                            JSONObject result = response.getJSONObject("result");
                            device = result.getString("_id");
//                            create_department();
                            savedInfo();
                        } catch (JSONException e) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            });
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "This device is already exists", Toast.LENGTH_SHORT).show();
                        });
                        Log.e("TOKEN_CHECK", "Error  : " + error.getLocalizedMessage());
                    }
            );
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void getDevices() {
        devices.clear();
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, API.getLink("devices-list"), null, response -> {
            progressDialog.dismiss();
            try {
                JSONArray result = response.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject object = result.getJSONObject(i);
                    devices.add(new Department(object.getString("_id"), object.getString("name")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Error : " + error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });
        });
        requestQueue.add(objectRequest);
    }

    private void create_device() {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        JSONObject json = new JSONObject();
        try {
            json.put("device", binding.deviceName.getEditText().getText().toString().trim());
            json.put("bank_name", binding.bankName.getEditText().getText().toString().trim());
            json.put("account_title", binding.accountTitle.getEditText().getText().toString().trim());
            json.put("account_number", binding.accountNumber.getEditText().getText().toString().trim());
            String department_ID = getDepartmentID();

            if (department_ID.isEmpty()) {
                progressDialog.dismiss();
                binding.department.getEditText().setText("");
                Toast.makeText(this, "Department is invalid", Toast.LENGTH_SHORT).show();
                return;
            }
            json.put("department", department_ID);
            Log.d(TAG, "create_device: " + json);
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, API.getLink("device-create"), json,
                    response -> {
                        progressDialog.dismiss();
                        Log.d("TOKEN_CHECK", "Response DEVICE : " + response.toString());
                        try {
                            JSONObject result = response.getJSONObject("result");
                            device = result.getString("_id");
//                            create_department();
                            savedInfo();
                        } catch (JSONException e) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            });
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "This device is already exists", Toast.LENGTH_SHORT).show();
                        });
                        Log.e("TOKEN_CHECK", "Error  : " + error.getLocalizedMessage());
                    }
            );
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
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

/*    private void create_department() {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        JSONObject json = new JSONObject();
        try {
            json.put("department", binding.department.getEditText().getText().toString().trim());
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, "http://3.29.241.160:8000/create-department", json,
                    response -> {
                        progressDialog.dismiss();
                        Log.d("TOKEN_CHECK", "Response department : " + response.toString());
                        try {
                            JSONObject result = response.getJSONObject("result");
                            department = result.getString("_id");
                            savedInfo();
                        } catch (JSONException e) {
                            runOnUiThread(() -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            });
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        });
                        Log.e("TOKEN_CHECK", "Error  : " + error.getLocalizedMessage());
                    }
            );
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }*/

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
        binding.deviceName.setEnabled(false);
        binding.bankName.setEnabled(false);
        binding.accountNumber.setEnabled(false);
        binding.accountTitle.setEnabled(false);
        binding.department.setEnabled(false);
        binding.save.setText("Edit");

        getDevices();
    }

    private void getDepartmentList() {
        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, API.getLink("department"), null, response -> {
            progressDialog.dismiss();
            try {
                JSONArray result = response.getJSONArray("result");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject object = result.getJSONObject(i);
                    departmentsID.add(new Department(object.getString("_id"), object.getString("name")));
                    departments.add(object.getString("name"));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, departments);
                binding.departmentList.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            runOnUiThread(() -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Error : " + error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            });
        });
        requestQueue.add(objectRequest);
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
            Toast.makeText(this, "Account Name is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.department.getEditText().getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Department is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    InputFilter alphanumericFilter = new InputFilter() {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            // Regex to allow only numbers and alphanumeric characters
            if (source.toString().matches("[a-zA-Z0-9]*")) {
                return null; // Acceptable input
            }
            return ""; // Reject the input
        }
    };

    private static final String TAG = "MainActivity";

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private boolean above13Check() {
        return ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
    }

    private boolean below13Check() {
        return ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED;
    }

}