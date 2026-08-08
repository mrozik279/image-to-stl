package com.mrozik279.pawclicker.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import com.mrozik279.pawclicker.ClickerViewModel
import com.mrozik279.pawclicker.R
import com.mrozik279.pawclicker.data.BREEDS
import java.text.NumberFormat
import kotlin.random.Random

private data class FloatingPlus(val id: Long, val text: String, val xOffsetDp: Int)

@Composable
fun ClickerScreen(viewModel: ClickerViewModel, modifier: Modifier = Modifier) {
    val state = viewModel.state
    val context = LocalContext.current
    val numberFormat = remember { NumberFormat.getIntegerInstance() }

    var bumpTrigger by remember { mutableStateOf(0) }
    var pressScale by remember { mutableStateOf(1f) }
    val animatedPressScale by animateFloatAsState(
        targetValue = pressScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "pressScale"
    )

    val floaters = remember { mutableStateOf(listOf<FloatingPlus>()) }
    val breed = remember(state.selectedBreed) {
        BREEDS.find { it.id == state.selectedBreed } ?: BREEDS.first()
    }

    val comboActive = viewModel.comboCount >= 5

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.tap_counter_label),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = numberFormat.format(state.tapCount),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.bones_label) + ": " + numberFormat.format(state.bones),
                style = MaterialTheme.typography.bodyLarge
            )

            AnimatedVisibility(visible = comboActive) {
                Text(
                    text = stringResource(R.string.combo_label, "2"),
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(R.drawable.ic_paw),
                    contentDescription = stringResource(R.string.tap_hint),
                    colorFilter = ColorFilter.tint(breed.tint),
                    modifier = Modifier
                        .size(220.dp)
                        .aspectRatio(1f)
                        .scale(animatedPressScale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            val earned = viewModel.onTap()
                            bumpTrigger++
                            pressScale = 1.18f
                            floaters.value = floaters.value + FloatingPlus(
                                id = System.nanoTime(),
                                text = "+$earned",
                                xOffsetDp = Random.nextInt(-40, 40)
                            )
                            if (state.vibrationEnabled) vibrate(context)
                        }
                )

                floaters.value.forEach { floater ->
                    FloatingPlusText(floater) {
                        floaters.value = floaters.value.filterNot { it.id == floater.id }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.tap_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }

    // Settle the press-scale bounce back to normal shortly after each tap.
    androidx.compose.runtime.LaunchedEffect(bumpTrigger) {
        if (bumpTrigger > 0) {
            kotlinx.coroutines.delay(90)
            pressScale = 1f
        }
    }
}

@Composable
private fun FloatingPlusText(floater: FloatingPlus, onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(true) }
    var risen by remember { mutableStateOf(false) }
    val offsetY by animateFloatAsState(
        targetValue = if (risen) -120f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "floatUp",
        finishedListener = { onFinished() }
    )

    androidx.compose.runtime.LaunchedEffect(floater.id) {
        risen = true
        kotlinx.coroutines.delay(700)
        visible = false
    }

    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(tween(200)) + slideOutVertically(tween(200))
    ) {
        Text(
            text = floater.text,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.offset(x = floater.xOffsetDp.dp, y = offsetY.dp)
        )
    }
}

private fun vibrate(context: Context) {
    val effect = VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator?.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.vibrate(effect)
    }
}
