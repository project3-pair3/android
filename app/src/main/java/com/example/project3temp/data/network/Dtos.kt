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


// 메뉴와 함께 카페 정보 등록
@Serializable
data class CreateMenusRequest(
    val imageUrl: String,
    val description: String,
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
