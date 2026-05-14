package com.example.project3temp.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException

object NetworkModule {
    // Android 에뮬레이터에서 호스트 머신의 localhost
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            },
        )
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val cafeApi: CafeApi = retrofit.create(CafeApi::class.java)
    val userApi: UserApi = retrofit.create(UserApi::class.java)
    val s3Api: S3Api = retrofit.create(S3Api::class.java)

    // S3 presigned URL에 이미지 바이너리를 직접 PUT
    suspend fun uploadToS3(presignedUrl: String, imageBytes: ByteArray, contentType: String) {
        withContext(Dispatchers.IO) {
            val body = imageBytes.toRequestBody(contentType.toMediaTypeOrNull())
            val request = Request.Builder()
                .url(presignedUrl)
                .put(body)
                .header("Content-Type", contentType)
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("S3 업로드 실패 (${response.code})")
            }
        }
    }
}
