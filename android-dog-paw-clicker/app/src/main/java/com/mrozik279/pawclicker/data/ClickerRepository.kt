package com.mrozik279.pawclicker.data

import android.content.Context
import android.content.SharedPreferences

/** Thin persistence wrapper around SharedPreferences. All reads/writes are local only. */
class ClickerRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): ClickerState = ClickerState(
        tapCount = prefs.getLong(KEY_TAP_COUNT, 0),
        bones = prefs.getLong(KEY_BONES, 0),
        bestCombo = prefs.getInt(KEY_BEST_COMBO, 0),
        treatLevel = prefs.getInt(KEY_TREAT_LEVEL, 0),
        puppyLevel = prefs.getInt(KEY_PUPPY_LEVEL, 0),
        selectedBreed = prefs.getString(KEY_SELECTED_BREED, "kundelek") ?: "kundelek",
        unlockedBreeds = prefs.getStringSet(KEY_UNLOCKED_BREEDS, setOf("kundelek"))
            ?: setOf("kundelek"),
        unlockedMilestones = prefs.getStringSet(KEY_UNLOCKED_MILESTONES, emptySet())
            .orEmpty().mapNotNull { it.toLongOrNull() }.toSet(),
        soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
        vibrationEnabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true),
        lastOpenedDayEpoch = prefs.getLong(KEY_LAST_OPENED_DAY, 0),
        dailyStreak = prefs.getInt(KEY_DAILY_STREAK, 0)
    )

    fun save(state: ClickerState) {
        prefs.edit()
            .putLong(KEY_TAP_COUNT, state.tapCount)
            .putLong(KEY_BONES, state.bones)
            .putInt(KEY_BEST_COMBO, state.bestCombo)
            .putInt(KEY_TREAT_LEVEL, state.treatLevel)
            .putInt(KEY_PUPPY_LEVEL, state.puppyLevel)
            .putString(KEY_SELECTED_BREED, state.selectedBreed)
            .putStringSet(KEY_UNLOCKED_BREEDS, state.unlockedBreeds)
            .putStringSet(
                KEY_UNLOCKED_MILESTONES,
                state.unlockedMilestones.map { it.toString() }.toSet()
            )
            .putBoolean(KEY_SOUND_ENABLED, state.soundEnabled)
            .putBoolean(KEY_VIBRATION_ENABLED, state.vibrationEnabled)
            .putLong(KEY_LAST_OPENED_DAY, state.lastOpenedDayEpoch)
            .putInt(KEY_DAILY_STREAK, state.dailyStreak)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "paw_clicker_prefs"
        const val KEY_TAP_COUNT = "tap_count"
        const val KEY_BONES = "bones"
        const val KEY_BEST_COMBO = "best_combo"
        const val KEY_TREAT_LEVEL = "treat_level"
        const val KEY_PUPPY_LEVEL = "puppy_level"
        const val KEY_SELECTED_BREED = "selected_breed"
        const val KEY_UNLOCKED_BREEDS = "unlocked_breeds"
        const val KEY_UNLOCKED_MILESTONES = "unlocked_milestones"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        const val KEY_LAST_OPENED_DAY = "last_opened_day"
        const val KEY_DAILY_STREAK = "daily_streak"
    }
}
