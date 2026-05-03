package com.example.greeting.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.greeting.domain.model.PhotoSlot
import com.example.greeting.domain.model.Template
import com.example.greeting.domain.model.TextSlot

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val imageUrl: String,
    val category: String,
    val isPremium: Boolean,
    // Slot positions flattened for easy storage
    val photoX: Float,
    val photoY: Float,
    val photoSize: Float,
    val textX: Float,
    val textY: Float,
    val orderInRoom: Int = 0 // To maintain sequence for the first 3
)

fun TemplateEntity.toDomain(): Template {
    return Template(
        id = id,
        imageUrl = imageUrl,
        category = category,
        isPremium = isPremium,
        photoSlot = PhotoSlot(photoX, photoY, photoSize),
        textSlot = TextSlot(textX, textY)
    )
}

fun Template.toEntity(order: Int = 0): TemplateEntity {
    return TemplateEntity(
        id = id,
        imageUrl = imageUrl,
        category = category,
        isPremium = isPremium,
        photoX = photoSlot.x,
        photoY = photoSlot.y,
        photoSize = photoSlot.size,
        textX = textSlot.x,
        textY = textSlot.y,
        orderInRoom = order
    )
}
