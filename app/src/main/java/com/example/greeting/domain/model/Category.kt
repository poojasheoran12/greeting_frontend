package com.example.greeting.domain.model

enum class Category(
    val id: String,
    val displayName: String
) {
    HINDI_QUOTES(
        id = "hindi quotes",
        displayName = "Hindi Quotes"
    ),
    ENGLISH_QUOTES(
        id = "english quotes",
        displayName = "English Quotes"
    ),
    BIRTHDAYS(
        id = "birthdays",
        displayName = "Birthdays"
    ),
    PATRIOTIC(
        id = "patrotic",
        displayName = "Patrotic"
    ),
    DAYS(
        id = "days",
        displayName = "Special Days"
    )
}
