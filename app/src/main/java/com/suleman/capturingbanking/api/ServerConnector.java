package com.suleman.capturingbanking.api;

import static com.suleman.capturingbanking.utilies.Utils.TOKEN;

import com.fxn.stash.Stash;
import com.suleman.capturingbanking.api.response_models.CreateDeviceRequest;
import com.suleman.capturingbanking.api.response_models.DepartmentResponse;
import com.suleman.capturingbanking.api.response_models.DeviceRecordResponse;
import com.suleman.capturingbanking.api.response_models.LoginResponse;
import com.suleman.capturingbanking.api.response_models.NewDeviceResponse;
import com.suleman.capturingbanking.model.LoginRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServerConnector {
    private static ServerConnector instance;
    private static final String BASE_URL = "https://www.btocsms.com/api/";
    private static final String STAGE_URL = "https://stage.btocsms.com/api/";
    private static final String PRODUCTION_EMAIL = "bankingsms@blueirissoft.com";
    private static final String PRODUCTION_PASSWORD = "BankSMS76&60$$";
    private static final String STAGING_EMAIL = "stage@blueirissoft.com";
    private static final String STAGING_PASSWORD = "stage123";
    private static final boolean IS_STAGING = true;
    private final ApiService apiService;

    public static synchronized ServerConnector getInstance() {
        if (instance == null) {
            instance = new ServerConnector();
        }
        return instance;
    }

    private ServerConnector() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(IS_STAGING ? STAGE_URL : BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    public void getDepartments(ResponseCallback callback) {
        String token = getToken();
        if (token.isEmpty()) {
            callback.onError("Token not found. Please log in again.");
            return;
        }
        String authHeader = "Bearer " + token;
        apiService.getDepartments(authHeader).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<DepartmentResponse> call, Response<DepartmentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().getResult());
                } else {
                    callback.onError("Failed to fetch departments");
                }
            }

            @Override
            public void onFailure(Call<DepartmentResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getDeviceRecord(String accountNumber, ResponseCallback callback) {
        String token = getToken();
        if (token.isEmpty()) {
            callback.onError("Token not found. Please log in again.");
            return;
        }
        String authHeader = "Bearer " + token;
        apiService.getDeviceRecord(accountNumber, authHeader).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<DeviceRecordResponse> call, Response<DeviceRecordResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getMessage().equals("Record fetched successfully")) {
                        callback.onSuccess(response.body().getResult());
                    } else
                        callback.onError(response.body().getMessage());
                } else {
                    callback.onError("Failed to fetch Device Record");
                }
            }

            @Override
            public void onFailure(Call<DeviceRecordResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getDeviceRecordById(String id, ResponseCallback callback) {
        String token = getToken();
        if (token.isEmpty()) {
            callback.onError("Token not found. Please log in again.");
            return;
        }
        String authHeader = "Bearer " + token;
        apiService.getDeviceRecordById(id, authHeader).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<DeviceRecordResponse> call, Response<DeviceRecordResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getMessage().equals("Record fetched successfully")) {
                        callback.onSuccess(response.body().getResult());
                    } else
                        callback.onSuccess(response.body().getMessage());
                } else {
                    callback.onError("Failed to fetch Device Record");
                }
            }

            @Override
            public void onFailure(Call<DeviceRecordResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createDevice(CreateDeviceRequest device, ResponseCallback callback) {
        String token = getToken();
        if (token.isEmpty()) {
            callback.onError("Token not found. Please log in again.");
            return;
        }
        String authHeader = "Bearer " + token;
        apiService.createDevice(device, authHeader).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<NewDeviceResponse> call, Response<NewDeviceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getMessage().equals("Device successfully added")) {
                        callback.onSuccess(response.body().getResult());
                    } else
                        callback.onSuccess(response.body().getMessage());
                } else {
                    callback.onError("Failed to create Device");
                }
            }

            @Override
            public void onFailure(Call<NewDeviceResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateDevice(CreateDeviceRequest device, ResponseCallback callback) {
        String token = getToken();
        if (token.isEmpty()) {
            callback.onError("Token not found. Please log in again.");
            return;
        }
        String authHeader = "Bearer " + token;
        apiService.updateDevice(device, authHeader).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<NewDeviceResponse> call, Response<NewDeviceResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getMessage().equals("Device successfully updated")) {
                        callback.onSuccess(response.body().getResult());
                    } else
                        callback.onSuccess(response.body().getMessage());
                } else {
                    callback.onError("Failed to update device");
                }
            }

            @Override
            public void onFailure(Call<NewDeviceResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void loginUser(ResponseCallback callback) {
        String email = IS_STAGING ? STAGING_EMAIL : PRODUCTION_EMAIL;
        String password = IS_STAGING ? STAGING_PASSWORD : PRODUCTION_PASSWORD;

        LoginRequest request = new LoginRequest(email, password);

        apiService.login(request).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    Stash.put(TOKEN, token);
                    callback.onSuccess("");
                } else {
                    callback.onError("Authentication Failed");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    private String getToken() {
        return Stash.getString(TOKEN, "");
    }
}
