package com.mrozik279.pawclicker

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mrozik279.pawclicker.data.BREEDS
import com.mrozik279.pawclicker.data.ClickerRepository
import com.mrozik279.pawclicker.data.ClickerState
import com.mrozik279.pawclicker.data.MILESTONES
import com.mrozik279.pawclicker.data.UpgradeConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/** How long a burst of taps stays "hot" for combo purposes. */
private const val COMBO_WINDOW_MS = 1_500L

/** Minimum taps inside the window before the combo multiplier kicks in. */
private const val COMBO_THRESHOLD = 5

private const val COMBO_MULTIPLIER = 2L

private const val MS_PER_DAY = 86_400_000L

class ClickerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ClickerRepository(application)
    private val toneGenerator by lazy { ToneGenerator(AudioManager.STREAM_MUSIC, 60) }

    var state by mutableStateOf(repository.load())
        private set

    /** Recent tap timestamps (ms), used only to compute the live combo count. */
    private val recentTapTimestamps = ArrayDeque<Long>()

    var comboCount by mutableStateOf(0)
        private set

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    init {
        applyDailyBonusIfNeeded()
        startPassiveIncomeTicker()
    }

    fun onTap(): Long {
        val now = System.currentTimeMillis()
        recentTapTimestamps.addLast(now)
        while (recentTapTimestamps.isNotEmpty() && now - recentTapTimestamps.first() > COMBO_WINDOW_MS) {
            recentTapTimestamps.removeFirst()
        }
        comboCount = recentTapTimestamps.size
        val comboActive = comboCount >= COMBO_THRESHOLD
        val earned = state.tapPower * (if (comboActive) COMBO_MULTIPLIER else 1L)

        val newTapCount = state.tapCount + 1
        val newBestCombo = maxOf(state.bestCombo, comboCount)

        val newMilestones = MILESTONES.filter {
            it.threshold <= newTapCount && it.threshold !in state.unlockedMilestones
        }
        val newBreeds = BREEDS.filter {
            it.unlockAt <= newTapCount && it.id !in state.unlockedBreeds
        }

        state = state.copy(
            tapCount = newTapCount,
            bones = state.bones + earned,
            bestCombo = newBestCombo,
            unlockedMilestones = state.unlockedMilestones + newMilestones.map { it.threshold },
            unlockedBreeds = state.unlockedBreeds + newBreeds.map { it.id }
        )
        persist()
        playTapSound()

        newMilestones.forEach { emitEvent("Osiągnięcie odblokowane: ${it.title}!") }
        newBreeds.forEach { emitEvent("Nowa rasa odblokowana: ${it.displayName}!") }

        return earned
    }

    fun buyTreat() {
        val cost = UpgradeConfig.costForLevel(
            UpgradeConfig.TREAT_BASE_COST,
            UpgradeConfig.TREAT_GROWTH,
            state.treatLevel
        )
        if (state.bones >= cost) {
            state = state.copy(bones = state.bones - cost, treatLevel = state.treatLevel + 1)
            persist()
        }
    }

    fun buyPuppy() {
        val cost = UpgradeConfig.costForLevel(
            UpgradeConfig.PUPPY_BASE_COST,
            UpgradeConfig.PUPPY_GROWTH,
            state.puppyLevel
        )
        if (state.bones >= cost) {
            state = state.copy(bones = state.bones - cost, puppyLevel = state.puppyLevel + 1)
            persist()
        }
    }

    fun selectBreed(id: String) {
        if (id in state.unlockedBreeds) {
            state = state.copy(selectedBreed = id)
            persist()
        }
    }

    fun toggleSound(enabled: Boolean) {
        state = state.copy(soundEnabled = enabled)
        persist()
    }

    fun toggleVibration(enabled: Boolean) {
        state = state.copy(vibrationEnabled = enabled)
        persist()
    }

    fun resetProgress() {
        repository.clear()
        recentTapTimestamps.clear()
        comboCount = 0
        state = ClickerState()
        persist()
    }

    private fun applyDailyBonusIfNeeded() {
        val today = System.currentTimeMillis() / MS_PER_DAY
        val last = state.lastOpenedDayEpoch
        when {
            last == 0L -> {
                state = state.copy(lastOpenedDayEpoch = today, dailyStreak = 1)
            }
            today == last -> Unit
            today == last + 1 -> {
                val newStreak = state.dailyStreak + 1
                val bonus = 10L * newStreak
                state = state.copy(
                    lastOpenedDayEpoch = today,
                    dailyStreak = newStreak,
                    bones = state.bones + bonus
                )
                emitEvent("Bonus dnia! +$bonus kości za powrót (passa: $newStreak)")
            }
            else -> {
                state = state.copy(lastOpenedDayEpoch = today, dailyStreak = 1)
            }
        }
        persist()
    }

    private fun startPassiveIncomeTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                if (state.bonesPerSecond > 0) {
                    state = state.copy(bones = state.bones + state.bonesPerSecond)
                    persist()
                }
                // Combo cools down once the tap burst goes quiet.
                val now = System.currentTimeMillis()
                while (recentTapTimestamps.isNotEmpty() && now - recentTapTimestamps.first() > COMBO_WINDOW_MS) {
                    recentTapTimestamps.removeFirst()
                }
                comboCount = recentTapTimestamps.size
            }
        }
    }

    private fun playTapSound() {
        if (!state.soundEnabled) return
        runCatching { toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 40) }
    }

    private fun emitEvent(message: String) {
        _events.tryEmit(message)
    }

    private fun persist() {
        repository.save(state)
    }

    override fun onCleared() {
        runCatching { toneGenerator.release() }
        super.onCleared()
    }
}
