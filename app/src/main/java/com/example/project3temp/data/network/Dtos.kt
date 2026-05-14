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

// 카페 정보 조회 (GET /cafes/info/{userId}) - 사용자의 카페 상태 + 기존 데이터
// statusCode 의미:
//   1 = 등록된 카페 없음 (필드 모두 빈 값으로 옴)
//   2 = 데이터는 있지만 아직 업로드 전 (prefill 후 "카페 정보 업로드" 버튼)
//   3 = 이미 업로드된 카페 있음 (prefill 후 "카페 정보 수정" 버튼)
@Serializable
data class CafeInfoResponse(
    val statusCode: Int,
    val cafeName: String = "",
    val addressCity: String = "",
    val addressDistrict: String = "",
    val addressDetail: String = "",
    val description: String? = null,
    val open: String? = null,    // "HH:mm"
    val close: String? = null,   // "HH:mm"
    val imageUrl: String? = null,
    val menu: List<MenuItemDto> = emptyList(),
)

// GET /s3/presigned-url 응답
@Serializable
data class PresignedUrlResponse(
    val presignedUrl: String,
    val imageUrl: String,
)

// PATCH /cafes/update/{userId}/image - imageUrl만 업데이트
@Serializable
data class ImageUrlRequest(
    val imageUrl: String,
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
