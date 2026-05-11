package com.example.project3temp.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CafeApi {
    // 메인 화면 - 카페 정보 get
    @GET("cafes")
    suspend fun listCafes( // 4개 param 모두 not null
        @Query("categoryId") categoryId: Int,
        @Query("addressCity") addressCity: String,
        @Query("addressDistrict") addressDistrict: String,
        @Query("listingType") listingType: String,
    ): List<CafeListItem>

    // 카페 상세정보
    @GET("cafes/{id}/menus")
    suspend fun getMenus(@Path("id") cafeId: Int): CafeMenusResponse

    // 신규 카페 등록 (메뉴 포함)
    @POST("cafes/menus")
    suspend fun createCafeWithMenu(
        @Body body: CreateCafeRequest,
    ): CafeMenusResponse
}
