package com.example.project3temp.data

data class DessertCategory(
    val id: String,
    val typeId: Int,
    val label: String,
    val emoji: String,
)

data class MenuItem(
    val id: String,
    val typeId: Int,
    val itemName: String,
    val cost: Int? = null,
    val stock: Int? = null,
)

object Categories {
    const val ALL_ID = "all"
    const val ALL_TYPE_ID = 0

    // 디저트 대분류
    val list: List<DessertCategory> = listOf(
        DessertCategory(ALL_ID, ALL_TYPE_ID, "전체", "🍰"),
        DessertCategory("cake", 1, "케이크", "🎂"),
        DessertCategory("macaron", 2, "마카롱", "🍬"),
        DessertCategory("tart", 3, "타르트", "🥧"),
        DessertCategory("pudding", 4, "푸딩", "🍮"),
        DessertCategory("cookie", 5, "쿠키", "🍪"),
    )

    val selectable: List<DessertCategory> = list.filter { it.id != ALL_ID }

    fun byId(id: String): DessertCategory =
        list.firstOrNull { it.id == id } ?: list.first()

    fun byTypeId(typeId: Int): DessertCategory =
        list.firstOrNull { it.typeId == typeId } ?: list.first()
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
    const val BASIC = "basic" // 기본 - db 순서 (또는 가나다 순으로 변경 가능)
    const val RECENTLY_UPDATED = "recentlyUpdated" // 업데이트 순 - 최근 업데이트를 먼저 보여줌

    data class Option(val value: String, val label: String)

    val options: List<Option> = listOf(
        Option(BASIC, "기본"),
        Option(RECENTLY_UPDATED, "최근 업데이트"),
    )

    fun labelOf(value: String): String =
        options.firstOrNull { it.value == value }?.label ?: value
}
