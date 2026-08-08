package com.mrozik279.pawclicker.data

import androidx.compose.ui.graphics.Color

/** A dog breed skin unlocked at a given lifetime tap count. */
data class Breed(
    val id: String,
    val displayName: String,
    val tint: Color,
    val unlockAt: Long
)

val BREEDS = listOf(
    Breed("kundelek", "Kundelek", Color(0xFF8D5A3C), unlockAt = 0),
    Breed("labrador", "Labrador", Color(0xFFE0B267), unlockAt = 100),
    Breed("husky", "Husky", Color(0xFF6E7B8B), unlockAt = 500),
    Breed("corgi", "Corgi", Color(0xFFD98836), unlockAt = 2_000),
    Breed("dachshund", "Jamnik", Color(0xFF4A2E1E), unlockAt = 10_000)
)

/** A named milestone tied to the lifetime tap counter (never decreases). */
data class Milestone(
    val threshold: Long,
    val title: String
)

val MILESTONES = listOf(
    Milestone(10, "Pierwsze kroki"),
    Milestone(50, "Rozgrzewka"),
    Milestone(100, "Przyjaciel Labradora"),
    Milestone(500, "Zimowa wyprawa"),
    Milestone(1_000, "Kolekcjoner smyczy"),
    Milestone(2_000, "Krótkie nóżki, wielkie serce"),
    Milestone(5_000, "Mistrz klikania"),
    Milestone(10_000, "Jamnik na medal"),
    Milestone(50_000, "Legenda łapek")
)

/** Base cost and growth factor for the two shop upgrades. */
object UpgradeConfig {
    const val TREAT_BASE_COST = 20L
    const val TREAT_GROWTH = 1.55
    const val PUPPY_BASE_COST = 50L
    const val PUPPY_GROWTH = 1.6

    fun costForLevel(baseCost: Long, growth: Double, level: Int): Long =
        (baseCost * Math.pow(growth, level.toDouble())).toLong().coerceAtLeast(baseCost)
}
