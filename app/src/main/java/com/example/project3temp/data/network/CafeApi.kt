package com.example.project3temp.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    // 신규 카페 등록 (메뉴 포함) - 더 이상 사용 안 함 (PUT /cafes/update/{userId}로 통합).
    //  지우진 않고 남겨둠 (사용자가 나중에 정리)
    @POST("cafes/menus")
    suspend fun createCafeWithMenu(
        @Body body: CreateCafeRequest,
    ): CafeMenusResponse

    // 사용자의 카페 상태 + 기존 데이터 조회
    // userId = user의 pk
    @GET("cafes/info/{userId}")
    suspend fun getCafeInfo(@Path("userId") userId: Int): CafeInfoResponse

    // 카페 정보 업로드/수정 (statusCode 1/2/3 모두 이 엔드포인트 사용)
    // 성공 시 201, body 없음 → 반환형 Unit
    @PUT("cafes/update/{userId}")
    suspend fun updateCafe(
        @Path("userId") userId: Int,
        @Body body: CreateCafeRequest,
    )
}
