package com.example.greeting.data.mapper

import com.example.greeting.data.remote.dto.PhotoSlotDto
import com.example.greeting.data.remote.dto.TemplateDto
import com.example.greeting.data.remote.dto.TextSlotDto
import com.example.greeting.domain.model.PhotoSlot
import com.example.greeting.domain.model.Template
import com.example.greeting.domain.model.TextSlot

fun TemplateDto.toDomain(): Template {
    return Template(
        id = id ?: "",
        imageUrl = imageUrl ?: "",
        category = category ?: "",
        isPremium = isPremium ?: false,
        photoSlot = photoSlot?.toDomain() ?: PhotoSlot(),
        textSlot = textSlot?.toDomain() ?: TextSlot()
    )
}

fun PhotoSlotDto.toDomain(): PhotoSlot {
    return PhotoSlot(
        x = x?.toFloat() ?: 0f,
        y = y?.toFloat() ?: 0f,
        size = size?.toFloat() ?: 0f
    )
}

fun TextSlotDto.toDomain(): TextSlot {
    return TextSlot(
        x = x?.toFloat() ?: 0f,
        y = y?.toFloat() ?: 0f
    )
}
