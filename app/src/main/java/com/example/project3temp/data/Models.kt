package com.example.project3temp.data

// 디저트 대분류 - 백엔드와 typeId(Int)로 통신
enum class DessertCategory(
    val typeId: Int,
    val label: String,
    val emoji: String,
) {
    CAKE(1, "케이크", "🎂"),
    MACARON(2, "마카롱", "🍬"),
    TART(3, "타르트", "🥧"),
    PUDDING(4, "푸딩", "🍮"),
    COOKIE(5, "쿠키", "🍪");

    companion object {
        // 백엔드가 잘못된 typeId를 주면 null - 호출 측에서 에러 처리
        fun byTypeId(typeId: Int): DessertCategory? =
            entries.firstOrNull { it.typeId == typeId }
    }
}

data class MenuItem(
    val id: String,
    val typeId: Int,
    val itemName: String,
    val cost: Int? = null,
    val stock: Int? = null,
)

// 카테고리 필터링 UI에서 쓰는 "전체" 의사 카테고리
object Categories {
    const val ALL_TYPE_ID = 0 // 백엔드 필터 API에 "전체" 의미로 보내는 값
    const val ALL_LABEL = "전체"
    const val ALL_EMOJI = "🍰"
}

// 지역 필터링 - "구"
object Districts {
    const val ALL_LABEL = "전체"

    val seoul: List<String> = listOf(
        ALL_LABEL,
        "강남구",
        "서초구",
        "마포구",
        "종로구",
        "성동구",
        "용산구",
        "송파구",
        "서대문구",
    )
}

// 정렬 기준
object ListingTypes {
    // const val BASIC = "basic" // 기본 - db 순서 (또는 가나다 순으로 변경 가능)
    const val RECENTLY_UPDATED = "recentlyUpdated" // 업데이트 순 - 최근 업데이트를 먼저 보여줌

    const val OLDEST = "oldest" // 업데이트 순 - 역순

    data class Option(val value: String, val label: String)

    val options: List<Option> = listOf(
        //Option(BASIC, "기본"),
        Option(RECENTLY_UPDATED, "최근 업데이트"),
        Option(OLDEST, "오래된 순"),
    )

    fun labelOf(value: String): String =
        options.firstOrNull { it.value == value }?.label ?: value
}
