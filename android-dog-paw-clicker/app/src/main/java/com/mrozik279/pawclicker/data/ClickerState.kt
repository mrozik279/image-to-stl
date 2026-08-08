package com.mrozik279.pawclicker.data

/**
 * [tapCount] is the literal click counter: it increases by exactly 1 per physical tap
 * and is never spent or reduced by upgrades. [bones] is the spendable currency earned
 * per tap (affected by upgrades and combo multiplier).
 */
data class ClickerState(
    val tapCount: Long = 0,
    val bones: Long = 0,
    val bestCombo: Int = 0,
    val treatLevel: Int = 0,
    val puppyLevel: Int = 0,
    val selectedBreed: String = "kundelek",
    val unlockedBreeds: Set<String> = setOf("kundelek"),
    val unlockedMilestones: Set<Long> = emptySet(),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val lastOpenedDayEpoch: Long = 0,
    val dailyStreak: Int = 0
) {
    val tapPower: Long get() = 1L + treatLevel
    val bonesPerSecond: Long get() = puppyLevel.toLong()
}
