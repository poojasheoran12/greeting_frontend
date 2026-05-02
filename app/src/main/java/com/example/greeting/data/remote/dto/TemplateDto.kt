package com.example.greeting.data.remote.dto

import com.google.firebase.firestore.DocumentId

data class TemplateDto(
    @DocumentId
    val id: String? = null,
    val imageUrl: String? = null,
    val category: String? = null,
    val isPremium: Boolean? = null,
    val photoSlot: PhotoSlotDto? = null,
    val textSlot: TextSlotDto? = null
)
