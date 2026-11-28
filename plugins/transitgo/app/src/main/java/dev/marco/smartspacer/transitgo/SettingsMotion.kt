package dev.marco.smartspacer.transitgo

import androidx.compose.animation.core.animate
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue




/**
 * Settings screen, opened from the Target's settings in Smartspacer or the app drawer.
 * Material 3 Expressive with wallpaper (dynamic) colors and Google Sans Text.
 */

private object TransitGoSettingsMotionTokens {
    const val PULL_SCREEN_SHARE = 0.41f
    const val PULL_ROOM_SHARE = 0.7f
    const val PULL_MIN_SHARE = 0.18f
    val FLICK_VELOCITY = 1200.dp
    const val FLICK_MIN_PULL = 0.08f
    const val FLICK_FINISH_MS = 260
}

/** Pull-past-the-end gesture for the settings screen's Source code chip. */
/** Pull-past-the-end state: [progress] 0..1, and the scroll connection that drives it. */
internal class PullToOpen(val connection: NestedScrollConnection, private val pull: () -> Float, private val max: () -> Float) {
        val progress get() = (pull() / max()).coerceIn(0f, 1f)
    }

    /**
     * Normal scrolling and flinging are untouched. A pull only starts when the finger touched
     * down while the list was already at the end ([armed]), so swiping or flicking down to the
     * bottom never triggers it. Then the drag left over past the end is the pull, 1:1 with the
     * finger: dragging about [TransitGoSettingsMotionTokens.PULL_SCREEN_SHARE] of the screen's height (from near the bottom to
     * near the top) calls [onOpen] once, with a vibration. Letting go springs it back to 0.
     * A swipe that starts higher up ([startY]) needs less, so it can open from anywhere: most
     * of the room above the finger, but never less than [TransitGoSettingsMotionTokens.PULL_MIN_SHARE] of the screen.
     */
    @Composable
internal fun rememberPullToOpen(armed: () -> Boolean, startY: () -> Float, onOpen: () -> Unit): PullToOpen {
        val screen = LocalWindowInfo.current.containerSize.height.toFloat()
        val currentStartY by rememberUpdatedState(startY)
        val maxFor = { y: Float -> (y * TransitGoSettingsMotionTokens.PULL_ROOM_SHARE).coerceIn(screen * TransitGoSettingsMotionTokens.PULL_MIN_SHARE, screen * TransitGoSettingsMotionTokens.PULL_SCREEN_SHARE) }
        val maxPx = screen * TransitGoSettingsMotionTokens.PULL_SCREEN_SHARE
        val currentArmed by rememberUpdatedState(armed)
        val haptics = LocalHapticFeedback.current
        val flickPx = with(LocalDensity.current) { TransitGoSettingsMotionTokens.FLICK_VELOCITY.toPx() }
        val pull = remember { mutableFloatStateOf(0f) }
        // Distance this pull needs, set when it starts so it can't change halfway through
        val needed = remember { mutableFloatStateOf(maxPx) }
        val opened = remember { mutableStateOf(false) }
        val currentOnOpen by rememberUpdatedState(onOpen)
        // The spring back runs on its own, so a new drag can stop it and take over from there
        val scope = rememberCoroutineScope()
        val springBack = remember { mutableStateOf<Job?>(null) }
        val grab = {
            springBack.value?.cancel()
            springBack.value = null
        }
        val connection = remember(maxPx) {
            object : NestedScrollConnection {
                // Dragging back down while pulled: shrink the pull before the list scrolls
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (source == NestedScrollSource.UserInput) grab()
                    if (pull.floatValue > 0f && available.y > 0f && source == NestedScrollSource.UserInput) {
                        val used = minOf(available.y, pull.floatValue)
                        pull.floatValue -= used
                        if (pull.floatValue < needed.floatValue) opened.value = false
                        return Offset(0f, used)
                    }
                    return Offset.Zero
                }

                // Drag left over after the list hit its end: that's the pull
                override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                    if (available.y < 0f && source == NestedScrollSource.UserInput && currentArmed()) {
                        if (pull.floatValue <= 0f) needed.floatValue = maxFor(currentStartY())
                        pull.floatValue = (pull.floatValue - available.y).coerceAtMost(needed.floatValue)
                        if (pull.floatValue >= needed.floatValue && !opened.value) {
                            opened.value = true
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentOnOpen()
                        }
                        return Offset(0f, available.y)
                    }
                    return Offset.Zero
                }

                // Finger lifted: spring the chip back
                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (pull.floatValue <= 0f) return Velocity.Zero
                    grab()
                    // A quick flick up (not just a slow let-go) finishes the pull by itself:
                    // the chip fills the screen and the repo opens, then it springs back
                    val flicked = -available.y >= flickPx && pull.floatValue >= needed.floatValue * TransitGoSettingsMotionTokens.FLICK_MIN_PULL
                    springBack.value = scope.launch {
                        try {
                            if (flicked && !opened.value) {
                                animate(pull.floatValue, needed.floatValue, animationSpec = tween(TransitGoSettingsMotionTokens.FLICK_FINISH_MS, easing = FastOutSlowInEasing)) { v, _ ->
                                    pull.floatValue = v
                                }
                                opened.value = true
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentOnOpen()
                            }
                            animate(pull.floatValue, 0f, animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) { v, _ ->
                                pull.floatValue = v.coerceAtLeast(0f) // the bounce never goes past "no pull"
                            }
                        } finally {
                            // Also when a new drag stops the spring back: ready to open again
                            if (pull.floatValue < needed.floatValue) opened.value = false
                        }
                    }
                    return available
                }
            }
        }
        return remember(connection) { PullToOpen(connection, { pull.floatValue }, { needed.floatValue }) }
    }
