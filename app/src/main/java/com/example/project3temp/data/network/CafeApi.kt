package com.example.project3temp.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CafeApi {
    @GET("cafes")
    suspend fun listCafes( // 4개 param 모두 not null
        @Query("categoryId") categoryId: Int,
        @Query("city") city: String,
        @Query("district") district: String,
        @Query("listingType") listingType: String,
    ): List<CafeListItem>

    // 카페 상세정보
    @GET("cafes/{id}/menus")
    suspend fun getMenus(@Path("id") cafeId: Int): CafeMenusResponse

    // 메뉴와 함께 카페 등록
    @POST("cafes/{id}/menus")
    suspend fun createMenus(
        @Path("id") cafeId: Int,
        @Body body: CreateMenusRequest,
    )
}
