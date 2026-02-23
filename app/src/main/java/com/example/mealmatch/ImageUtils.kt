package com.example.mealmatch

object ImageUtils {

    fun ingredientImageUrl(rawName: String, size: String = "small"): String {
        val safe = rawName
            .trim()
            .lowercase()
            .replace(" ", "_")
            .replace("/", "_")
            .replace("\\", "_")

        val suffix = when (size.lowercase()) {
            "small" -> "-small"
            "medium" -> "-medium"
            "large" -> "-large"
            else -> ""
        }

        return "https://www.themealdb.com/images/ingredients/$safe$suffix.png"
    }
}