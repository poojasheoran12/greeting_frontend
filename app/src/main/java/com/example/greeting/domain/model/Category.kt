package com.example.greeting.domain.model

object Category {
    const val HINDI_QUOTES = "hindi quotes"
    const val ENGLISH_QUOTES = "english quotes"
    const val BIRTHDAYS = "birthdays"
    const val PATRIOTIC = "patrotic"
    const val DAYS = "days"

    val ALL = listOf(
        HINDI_QUOTES,
        ENGLISH_QUOTES,
        BIRTHDAYS,
        PATRIOTIC,
        DAYS
    )

    fun getTitle(category: String): String {
        return when (category) {
            HINDI_QUOTES -> "Hindi Quotes"
            ENGLISH_QUOTES -> "English Quotes"
            BIRTHDAYS -> "Birthdays"
            PATRIOTIC -> "Patrotic"
            DAYS -> "Days"
            else -> category.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
        }
    }
}
