package com.example.project3temp.data.network

import kotlinx.serialization.Serializable

// 하나의 카페 (게시물)에 대한 정보
@Serializable
data class CafeMenusResponse(
    val cafeId: Int,
    val cafeName: String,
    val addressCity: String? = null,
    val addressDistrict: String? = null,
    val addressDetail: String? = null,
    val description: String? = null,
    val open: String? = null,   // 백엔드 응답: "HH:mm" 형식 (예: "09:00")
    val close: String? = null,  // 백엔드 응답: "HH:mm" 형식
    val imageUrl: String? = null,
    val menu: List<MenuItemDto> = emptyList(),
)

// 하나의 메뉴 포맷
@Serializable
data class MenuItemDto(
    val itemName: String,
    val typeId: Int,
    val cost: Int? = null,
    val stock: Int? = null,
)


// 신규 카페 등록 (POST /cafes/menu) - 카페 정보 + 메뉴를 한 번에 생성
@Serializable
data class CreateCafeRequest(
    val cafeName: String,
    val addressCity: String,
    val addressDistrict: String,
    val addressDetail: String,
    val description: String? = null,
    val open: String,   // "HH:mm:ss" 형식, 초는 항상 00 (예: "09:00:00")
    val close: String,  // "HH:mm:ss" 형식, 초는 항상 00
    val imageUrl: String,
    val menu: List<MenuItemDto>,
)


// 회원가입 (POST /users/join) - 응답 body 없음 (201)
@Serializable
data class JoinRequest(
    val userId: String,
    val password: String,
    val nickname: String,
)

// 로그인 (POST /users/login)
@Serializable
data class LoginRequest(
    val userId: String,
    val password: String,
)

// 로그인 응답 (200)
@Serializable
data class LoginResponse(
    val id: Int,          // pk
    val userId: String,
    val nickname: String,
)

@Serializable
data class CafeListItem(
    val id: Int,
    val cafeName: String,
    val addressCity: String? = null,
    val addressDistrict: String? = null,
    val addressDetail: String? = null,
    val open: String? = null,   // 백엔드 응답: "HH:mm" 형식
    val close: String? = null,  // 백엔드 응답: "HH:mm" 형식
    val imageUrl: String? = null,
    val totalCount: Int = 0,
    val createdAt: String? = null,
)
