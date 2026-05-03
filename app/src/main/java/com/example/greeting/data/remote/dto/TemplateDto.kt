package com.example.greeting.data.remote.dto

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

data class TemplateDto(
    @DocumentId
    val id: String? = null,
    val imageUrl: String? = null,
    val category: String? = null,
    @get:PropertyName("isPremium")
    @PropertyName("isPremium")
    val isPremium: Boolean? = null,
    
    val photoSlot: PhotoSlotDto? = null,
    val textSlot: TextSlotDto? = null
)
