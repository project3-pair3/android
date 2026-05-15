package com.example.project3temp.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface S3Api {
    // presigned URL + image URL 발급
    @GET("s3/presigned-url")
    suspend fun getPresignedUrl(
        @Query("fileName") fileName: String,
        @Query("contentType") contentType: String,
    ): PresignedUrlResponse
}
