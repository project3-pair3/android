package com.example.project3temp.data.network

import retrofit2.HttpException

fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> {
        val body = runCatching { response()?.errorBody()?.string() }.getOrNull()
        if (body.isNullOrBlank()) "HTTP ${code()} ${message()}"
        else "HTTP ${code()}\n${body.take(400)}"
    }
    else -> message ?: "알 수 없는 오류"
}
