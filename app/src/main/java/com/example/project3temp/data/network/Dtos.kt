package com.example.project3temp.data.network

import kotlinx.serialization.Serializable

@Serializable
data class CafeMenusResponse(
    val cafeId: Int,
    val cafeName: String,
    val addressCity: String? = null,
    val addressDistrict: String? = null,
    val addressDetail: String? = null,
    val description: String? = null,
    val open: String? = null,
    val close: String? = null,
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
    val open: String,   // ISO 8601 datetime (예: "2026-05-11T09:00:00")
    val close: String,  // ISO 8601 datetime
    val imageUrl: String,
    val menu: List<MenuItemDto>,
)


@Serializable
data class CafeListItem(
    val id: Int,
    val cafeName: String,
    val addressCity: String? = null,
    val addressDistrict: String? = null,
    val addressDetail: String? = null,
    val open: String? = null,
    val close: String? = null,
    val imageUrl: String? = null,
    val totalCount: Int = 0,
    val createdAt: String? = null,
)
