package com.example.project3temp.data.network

import retrofit2.http.Body
import retrofit2.http.POST


interface UserApi {
    // 회원가입 - 성공 시 201, 응답 body 없음
    @POST("users/join")
    suspend fun join(@Body body: JoinRequest)

    // 로그인 - 성공 시 200, 응답 body {id, userId, nickname}
    @POST("users/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse
}
