package com.example.greeting.domain.model

data class Template(
    val id: String,
    val imageUrl: String,
    val category: String,
    val isPremium: Boolean,
    val photoSlot: PhotoSlot,
    val textSlot: TextSlot
)
