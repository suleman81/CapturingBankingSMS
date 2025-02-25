package com.suleman.capturingbanking.api;

import com.suleman.capturingbanking.api.response_models.CreateDeviceRequest;
import com.suleman.capturingbanking.api.response_models.DepartmentResponse;
import com.suleman.capturingbanking.api.response_models.DeviceRecordResponse;
import com.suleman.capturingbanking.api.response_models.LoginResponse;
import com.suleman.capturingbanking.api.response_models.NewDeviceResponse;
import com.suleman.capturingbanking.model.LoginRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("signinApp")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("departmentnew")
    Call<DepartmentResponse> getDepartments(@Header("Authorization") String authToken);

    @GET("devicerecordnew/{account_number}")
    Call<DeviceRecordResponse> getDeviceRecord(
            @Path("account_number") String account_number,
            @Header("Authorization") String authToken
    );

    @GET("devicerecordnewbyid/{id}")
    Call<DeviceRecordResponse> getDeviceRecordById(
            @Path("id") String id,
            @Header("Authorization") String authToken
    );

    @POST("device-create")
    Call<NewDeviceResponse> createDevice(@Body CreateDeviceRequest request, @Header("Authorization") String authToken);

    @POST("device-updatenew")
    Call<NewDeviceResponse> updateDevice(@Body CreateDeviceRequest request, @Header("Authorization") String authToken);

}
