package com.example.project3temp.data

data class DessertCategory(
    val id: String,
    val label: String,
    val emoji: String,
)

data class MenuItem(
    val id: String,
    val categoryId: String,
    val name: String,
    val price: Int? = null,
    val stock: Int? = null,
)

data class Dessert(
    val id: String,
    val storeName: String,
    val areaLabel: String,
    val district: String,
    val categoryId: String,
    val stockToday: Int,
    val soldOutCount: Int,
    val imageUrl: String,
    val intro: String? = null,
    val menuItems: List<MenuItem> = emptyList(),
)

object Categories {
    const val ALL_ID = "all"

    val list: List<DessertCategory> = listOf(
        DessertCategory(ALL_ID, "전체", "🍰"),
        DessertCategory("cake", "케이크", "🎂"),
        DessertCategory("macaron", "마카롱", "🍬"),
        DessertCategory("tart", "타르트", "🥧"),
        DessertCategory("pudding", "푸딩", "🍮"),
        DessertCategory("cookie", "쿠키", "🍪"),
    )

    val selectable: List<DessertCategory> = list.filter { it.id != ALL_ID }

    fun byId(id: String): DessertCategory =
        list.firstOrNull { it.id == id } ?: list.first()
}

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
