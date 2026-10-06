package com.xplorea.utils;

import com.xplorea.model.User;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    @FormUrlEncoded
    @POST("auth/login.php")
    Call<User> login(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("auth/register.php")
    Call<User> register(
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password
    );
}
