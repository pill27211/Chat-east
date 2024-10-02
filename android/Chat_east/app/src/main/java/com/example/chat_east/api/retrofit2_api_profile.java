package com.example.chat_east.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface retrofit2_api_profile {
    @Multipart
    @POST("upload")
    Call<ResponseBody> uploadFile(
            @Part MultipartBody.Part file,
            @Part("user_id") RequestBody user_id,
            @Part("path_type") RequestBody path_type
    );
}
