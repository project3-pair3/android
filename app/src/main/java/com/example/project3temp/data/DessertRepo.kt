package com.example.project3temp.data

import androidx.compose.runtime.mutableStateListOf

object DessertRepo {
    private val _items = mutableStateListOf<Dessert>().apply {
        addAll(DummyData.desserts)
    }

    val items: List<Dessert> get() = _items

    fun addAtTop(dessert: Dessert) {
        _items.add(0, dessert)
    }
}
