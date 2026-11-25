package dev.marco.smartspacer.transitgo

import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.lerp
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.basicMarquee
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue




/**
 * Settings screen, opened from the Target's settings in Smartspacer or the app drawer.
 * Material 3 Expressive with wallpaper (dynamic) colors and Google Sans Text.
 */
/**
 * Reusable building blocks for the Transit GO settings screen.
 * Keep common row structure and icon treatment out of the screen coordinator.
 */

    @Composable
    internal fun SectionHeader(text: Int) = Text(
        stringResource(text),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )


    internal enum class Position { First, Middle, Last, Single }

    // Grouped rows: big outer corners, small inner ones (Android 16 settings style)
    internal fun Position.shape(): Shape {
        val big = 24.dp; val small = 6.dp
        return when (this) {
            Position.First -> RoundedCornerShape(big, big, small, small)
            Position.Middle -> RoundedCornerShape(small)
            Position.Last -> RoundedCornerShape(small, small, big, big)
            Position.Single -> RoundedCornerShape(big)
        }
    }

    /**
     * @param colorfulWhenOn give the icon a random bright color while on, like Android's
     *   colorful settings icons (picked again each time it's turned on)
     */

    @Composable
    internal fun AccessRow(granted: Boolean, iconShape: Shape, onClick: () -> Unit) {
        val colors = MaterialTheme.colorScheme
        SettingRow(
            if (granted) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
            iconShape, R.string.setting_access,
            if (granted) R.string.setting_access_on else R.string.setting_access_off,
            Position.Single, if (granted) TransitGoSettingsTokens.ACCESS_GREEN else colors.error, onClick
        ) {
            Surface(
                shape = CircleShape,
                color = if (granted) TransitGoSettingsTokens.ACCESS_GREEN else colors.errorContainer,
                contentColor = if (granted) Color.White else colors.onErrorContainer
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
                ) {
                    Icon(
                        if (granted) Icons.Rounded.Check else Icons.Rounded.ErrorOutline, null,
                        Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        stringResource(if (granted) R.string.setting_access_badge_on else R.string.setting_access_badge_off),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }


    @Composable
    internal fun SettingRow(
        icon: ImageVector, iconShape: Shape, title: Int, summary: Int, position: Position,
        badgeColor: Color?, onClick: () -> Unit, trailing: @Composable () -> Unit
    ) {
        Surface(
            shape = position.shape(),
            color = MaterialTheme.colorScheme.surfaceBright,
            modifier = Modifier.fillMaxWidth().clip(position.shape()).clickable(onClick = onClick)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                IconBadge(icon, iconShape, badgeColor)
                RowTexts(title, summary, Modifier.weight(1f).padding(horizontal = 16.dp))
                trailing()
            }
        }
    }

    /**
     * The row's icon on an Expressive shape. With [accent], the color spreads from the center
     * out to fill the shape (and shrinks back to the center when it's removed).
     */

    @Composable
    internal fun IconBadge(icon: ImageVector, shape: Shape, accent: Color? = null) {
        // Keep the last color so it can shrink away after the accent is removed
        var shown by remember { mutableStateOf(accent ?: Color.Transparent) }
        if (accent != null) shown = accent
        val on = accent != null

        // Color spreading from the center: a soft spring on the way in, quick ease on the way out
        val reveal by animateFloatAsState(
            targetValue = if (on) 1f else 0f,
            animationSpec = if (on) spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow)
            else tween(220, easing = FastOutSlowInEasing),
            label = "reveal"
        )
        // The shape turns a little as it fills (the icon itself stays upright)
        val turn by animateFloatAsState(
            targetValue = if (on) 40f else 0f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
            label = "turn"
        )
        // Squish then bounce back when turned on (not when the screen first opens)
        val pop = remember { Animatable(1f) }
        var firstRun by remember { mutableStateOf(true) }
        LaunchedEffect(accent) {
            if (firstRun) { firstRun = false; return@LaunchedEffect }
            if (accent != null) {
                pop.animateTo(0.82f, tween(90))
                pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
            }
        }

        // A new icon (like the eye opening or closing) arrives with a blink: the old one squashes
        // shut, the new one swaps in and springs open
        var drawn by remember { mutableStateOf(icon) }
        val blink = remember { Animatable(1f) }
        LaunchedEffect(icon) {
            if (icon == drawn) return@LaunchedEffect
            blink.animateTo(0.1f, tween(110, easing = FastOutLinearInEasing))
            drawn = icon
            blink.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }

        val base = MaterialTheme.colorScheme.onSecondaryContainer
        // White icon on dark colors, near-black on light ones (like yellow)
        val onAccent = if (shown.luminance() > 0.5f) Color(0xFF1F1F1F) else Color.White
        val container = MaterialTheme.colorScheme.secondaryContainer
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(44.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value }
        ) {
            // Shape layer: rotates, with the color circle growing inside it
            Box(
                Modifier.matchParentSize()
                    .graphicsLayer { rotationZ = turn }
                    .clip(shape)
                    .background(container)
                    .drawBehind {
                        if (reveal > 0f) drawCircle(
                            shown.copy(alpha = reveal.coerceIn(0f, 1f)),
                            radius = size.maxDimension * 0.75f * reveal
                        )
                    }
            )
            Icon(
                drawn, null, tint = lerp(base, onAccent, reveal.coerceIn(0f, 1f)),
                modifier = Modifier.size(22.dp).graphicsLayer { scaleY = blink.value }
            )
        }
    }


    @Composable
    internal fun RowTexts(title: Int, summary: Int, modifier: Modifier) = Column(modifier) {
        Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(summary),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

/**
     * One line of text. If it's too long to fit, it fades out at both edges instead of being
     * cut, and slowly scrolls to show the rest (the start fade appears once it's moving).
     */
    @Composable
    internal fun FadingText(text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
        val measurer = rememberTextMeasurer()
        val fade = with(LocalDensity.current) { TransitGoSettingsTokens.FADE_EDGE.toPx() }
        // Whether the text is wider than its space; checked while laying out, with the width the
        // parent offers (a layout modifier, so it also works where parents ask for intrinsic sizes)
        var fits by remember(text, style) { mutableStateOf(true) }
        Text(
            text, style = style, color = color, maxLines = 1, softWrap = false,
            modifier = modifier
                .layout { measurable, constraints ->
                    val width = measurer.measure(text, style, maxLines = 1, softWrap = false).size.width
                    val nowFits = width <= constraints.maxWidth
                    if (nowFits != fits) fits = nowFits
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
                }
                .then(
                    if (fits) Modifier else Modifier
                        // Draw offscreen so the gradient can erase the edges (DstIn keeps
                        // the text only where the mask is opaque)
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()
                            val edge = (fade / size.width).coerceAtMost(0.3f)
                            drawRect(
                                Brush.horizontalGradient(
                                    0f to Color.Transparent, edge to Color.Black,
                                    1f - edge to Color.Black, 1f to Color.Transparent
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        }
                        .basicMarquee(iterations = Int.MAX_VALUE, initialDelayMillis = 1500, velocity = 40.dp)
                )
        )
    }
