package dev.marco.smartspacer.transitgo

import android.content.Context
import androidx.compose.animation.core.animate
import androidx.compose.ui.unit.lerp
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp


/**
 * Settings screen, opened from the Target's settings in Smartspacer or the app drawer.
 * Material 3 Expressive with wallpaper (dynamic) colors and Google Sans Text.
 */
 
/**
 * Live/sample trip card preview used by the plugin-owned settings screen.
 * This is deliberately separate from the settings coordinator and reusable setting rows.
 */
/**
     * Mirrors the Smartspacer card: title, then the card icon (same order as the Target:
     * route chip > step icon > Transit icon), the subtitle, and Transit's logo + name.
     * With no live trip it shows an example trip, so the settings' effect is still visible.
     * Switching between the two springs (bouncy pop-in and height change).
     */
    @Composable
    internal fun TransitGoSettingsPreview(activity: Context, settings: Settings, settingsVersion: Int) {
        settingsVersion // re-read the settings when one changes
        val live = GoStore.state
        // New random example each time the settings open, and again after a trip ends
        val sample = remember(SampleTrip.generation) { SampleTrip.create(activity) }
        val glyph = remember { TransitIcon.glyph(activity) }
        val appIcon = remember { TransitIcon.get(activity) }
        val onCard = MaterialTheme.colorScheme.onPrimaryContainer

        // Darker fill with a lighter outline, both tinted from the wallpaper's primary color
        val colors = MaterialTheme.colorScheme
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = lerp(colors.primaryContainer, colors.surfaceContainerLowest, 0.55f),
            border = BorderStroke(1.5.dp, lerp(colors.primaryContainer, colors.primary, 0.6f)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                .animateContentSize(spring<IntSize>(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow))
        ) {
            AnimatedContent(
                targetState = live != null,
                transitionSpec = {
                    (fadeIn(tween(180)) +
                        scaleIn(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), initialScale = 0.85f)) togetherWith
                        fadeOut(tween(90))
                },
                label = "preview"
            ) { isLive ->
                val state = if (isLive) GoStore.state ?: sample else sample
                Column(Modifier.fillMaxWidth().padding(24.dp)) {
                    // One line (like the real card), cross-fading when it changes
                    AnimatedContent(
                        targetState = state.title,
                        transitionSpec = { fadeIn(tween(320, delayMillis = 90)) togetherWith fadeOut(tween(200)) },
                        label = "title"
                    ) { title ->
                        FadingText(
                            title,
                            MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                            onCard
                        )
                    }
                    // Fixed height and a fixed icon slot, so changing a setting never resizes the card
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp).height(32.dp)
                    ) {
                        val chip = state.chip?.takeIf { settings.showRouteChip }
                        val step = state.stepIcon
                        val iconKind = when {
                            chip != null -> IconKind.Chip
                            step != null -> IconKind.Step
                            appIcon != null -> IconKind.App
                            else -> IconKind.None
                        }
                        // Icon swap: the new one pops in with a small bounce, the old one fades.
                        // The slot itself stays 28dp, so nothing next to it moves.
                        AnimatedContent(
                            targetState = iconKind,
                            transitionSpec = {
                                // Cross-fade (old out, new in a little later) with a slight pop
                                (fadeIn(tween(320, delayMillis = 80)) + scaleIn(springy(), initialScale = 0.8f)) togetherWith
                                    (fadeOut(tween(220)) + scaleOut(tween(220), targetScale = 0.8f))
                            },
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(28.dp),
                            label = "icon"
                        ) { kind ->
                            Box(Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                                when (kind) {
                                    IconKind.Chip -> chip?.let { Image(it.asImageBitmap(), state.routeName, Modifier.size(28.dp)) }
                                    IconKind.Step -> step?.let { Image(it.asImageBitmap(), null, Modifier.size(24.dp), colorFilter = ColorFilter.tint(onCard)) }
                                    IconKind.App -> appIcon?.let { Image(it.asImageBitmap(), "Transit", Modifier.size(24.dp)) }
                                    IconKind.None -> Unit
                                }
                            }
                        }
                        Spacer(Modifier.size(8.dp))
                        // Subtitle change (e.g. "· 3 h 07 min" -> "· 6:44 AM"): the text
                        // cross-fades while its width springs to the new size, so the Transit
                        // logo after it slides smoothly instead of jumping
                        AnimatedContent(
                            targetState = cardSubtitle(state, settings).orEmpty(),
                            transitionSpec = {
                                (fadeIn(tween(320, delayMillis = 90)) togetherWith fadeOut(tween(200)))
                                    .using(SizeTransform(clip = false) { _, _ -> springy() })
                            },
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.weight(1f, fill = false),
                            label = "subtitle"
                        ) { text ->
                            FadingText(text, MaterialTheme.typography.titleMedium, onCard)
                        }
                        glyph?.let {
                            // "· [logo] Transit", like the real card
                            Spacer(Modifier.size(8.dp))
                            Text(SOURCE_BULLET.trim(), style = MaterialTheme.typography.titleMedium, color = onCard)
                            Spacer(Modifier.size(8.dp))
                            Image(it.asImageBitmap(), null, Modifier.size(22.dp), colorFilter = ColorFilter.tint(onCard))
                            Spacer(Modifier.size(6.dp))
                            Text("Transit", style = MaterialTheme.typography.titleMedium, color = onCard)
                        }
                    }
                }
            }
        }
        // Note under the card (outside it, so the card itself looks exactly like the real one)
        if (live == null) {
            Text(
                stringResource(R.string.preview_example),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
            )
        }
    }

/** Which icon the card shows, so the preview can animate when it changes */
    private enum class IconKind { Chip, Step, App, None }

    /** Gentle bounce used by the preview's in-card transitions */
    private fun <T> springy() = spring<T>(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow)
