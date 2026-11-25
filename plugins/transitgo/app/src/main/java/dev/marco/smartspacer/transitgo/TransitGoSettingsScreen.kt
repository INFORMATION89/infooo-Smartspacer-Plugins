package dev.marco.smartspacer.transitgo

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue




/**
 * Settings screen, opened from the Target's settings in Smartspacer or the app drawer.
 * Material 3 Expressive with wallpaper (dynamic) colors and Google Sans Text.
 */
/**
 * Compose content and interaction logic for the plugin-owned settings screen.
 *
 * The Activity owns Android lifecycle and system settings intents; this class owns
 * screen composition, preview behavior, and settings interactions.
 */
internal class TransitGoSettingsScreen(private val activity: SettingsActivity) {

    private companion object {
        const val SOURCE_URL = "https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/tree/main/plugins/transitgo"
    }
    private var settingsVersion by mutableIntStateOf(0)

    @Composable
    fun Content(accessGranted: Boolean) {
        SettingsScreen(accessGranted)
    }

    private fun openSource() =
        activity.startActivity(Intent(Intent.ACTION_VIEW, SOURCE_URL.toUri()))

    private fun openNotificationAccess() = activity.openNotificationAccess()

    /** Saves a change, redraws the preview and tells Smartspacer to redraw the card. */
    private fun onSettingChanged(rerollExample: Boolean = true) {
        settingsVersion++
        // With no live trip, show a different example each time, so the change is easy to see
        // (not for settings the preview doesn't show, like the lock screen one)
        if (rerollExample && GoStore.state == null) SampleTrip.reroll()
        SmartspacerTargetProvider.notifyChange(activity, TransitGoTarget::class.java)
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun SettingsScreen(accessGranted: Boolean) {
        val settings = remember { Settings(activity) }
        val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val listState = rememberLazyListState()
        // The list blurs through the header once it scrolls under it
        val haze = rememberHazeState()

        // Where the list stops: the space around the Source code chip is sized so that, scrolled
        // to the end, a row sits just under the header (any section label above it tucked away
        // beneath it), whatever the language or text size makes the rows' height. The first
        // Card row if it fits; if the rows are too tall for that, the next row, and so on.
        // Measured from the list's layout (all rows from the same frame): for each of those
        // rows, its distance to the bottom of Notification access, once both are on screen.
        val density = LocalDensity.current
        val screenPx = LocalWindowInfo.current.containerSize.height
        val headerBottomPx = with(density) {
            (WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + TopAppBarDefaults.TopAppBarExpandedHeight).toPx()
        }
        // Heights of the rows from the first Card row down, recorded as each one is laid out.
        // Scrolling down, every row has been on screen before the end of the list comes into
        // view, so the end is settled before you reach it and doesn't move under your finger.
        val itemSizes = remember { mutableStateMapOf<String, Int>() }
        LaunchedEffect(listState) {
            snapshotFlow {
                listState.layoutInfo.visibleItemsInfo
                    .filter { it.key in TransitGoSettingsTokens.END_ITEMS }
                    .associate { it.key as String to it.size }
            }.collect { itemSizes.putAll(it) }
        }
        val rowSpacingPx = with(LocalDensity.current) { TransitGoSettingsTokens.ROW_SPACING.roundToPx() }
        // From a row's top to the bottom of Notification access, or null until all are known
        val spanFrom = { key: String ->
            val keys = TransitGoSettingsTokens.END_ITEMS.drop(TransitGoSettingsTokens.END_ITEMS.indexOf(key))
            val sizes = keys.map { itemSizes[it] }
            if (sizes.any { it == null }) null else sizes.sumOf { it!! } + rowSpacingPx * (keys.size - 1)
        }
        val shadowColor = Color.Black
        // 0 at the top of the list, 1 once it has scrolled TransitGoSettingsTokens.HEADER_BLUR_DISTANCE
        val blurDistancePx = with(LocalDensity.current) { TransitGoSettingsTokens.HEADER_BLUR_DISTANCE.toPx() }
        val headerBlur = {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / blurDistancePx).coerceIn(0f, 1f)
        }
        // Whether the current touch started with the list already at the bottom
        var touchStartedAtEnd by remember { mutableStateOf(false) }
        // Where that touch started, so a swipe from anywhere has room to open the repo
        var touchStartY by remember { mutableFloatStateOf(Float.MAX_VALUE) }
        val pullToOpen = rememberPullToOpen(armed = { touchStartedAtEnd }, startY = { touchStartY }) { openSource() }
        // Pull phases: first the chip grows into a full-width button, then that button grows
        // until it fills the whole screen, and the repo opens. A light spring follows the finger,
        // smoothing out uneven touch events, and each phase is eased so it starts and settles
        // gently instead of moving at a constant rate.
        val progress by animateFloatAsState(
            pullToOpen.progress,
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
            label = "pull"
        )
        val chipPhase = FastOutSlowInEasing.transform((progress / TransitGoSettingsTokens.CHIP_PHASE).coerceIn(0f, 1f))
        val takeover = FastOutSlowInEasing.transform(((progress - TransitGoSettingsTokens.CHIP_PHASE) / (1f - TransitGoSettingsTokens.CHIP_PHASE)).coerceIn(0f, 1f))
        var chipBounds by remember { mutableStateOf<Rect?>(null) }
        Box(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                LargeTopAppBar(
                    title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold) },
                    scrollBehavior = scroll,
                    // Clear bar; with the list under it, a frosted (blurred, tinted) material
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    // Always frosted; the blur gets stronger the further the list is scrolled
                    modifier = Modifier
                        // Very soft shadow just under the header's edge, so it reads as sitting on
                        // top of the list. Hidden until the header has collapsed halfway, then it
                        // fades in, reaching full strength once the header is fully collapsed.
                        .drawWithContent {
                            drawContent()
                            val shown = ((scroll.state.collapsedFraction - TransitGoSettingsTokens.SHADOW_FROM) / (1f - TransitGoSettingsTokens.SHADOW_FROM)).coerceIn(0f, 1f)
                            if (shown > 0f) drawRect(
                                Brush.verticalGradient(
                                    0f to shadowColor.copy(alpha = TransitGoSettingsTokens.HEADER_SHADOW_ALPHA * shown),
                                    1f to Color.Transparent,
                                    startY = size.height, endY = size.height + TransitGoSettingsTokens.HEADER_SHADOW.toPx()
                                ),
                                topLeft = Offset(0f, size.height),
                                size = Size(size.width, TransitGoSettingsTokens.HEADER_SHADOW.toPx())
                            )
                        }
                        // The screen's own background color, mostly opaque, and no grain: the
                        // header matches the background exactly, with only the blurred list
                        // showing through it
                        .hazeEffect(
                            haze,
                            HazeStyle(
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                                tint = HazeTint(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = TransitGoSettingsTokens.HEADER_TINT_ALPHA)),
                                noiseFactor = 0f
                            )
                        ) {
                        blurRadius = lerp(TransitGoSettingsTokens.HEADER_BLUR_MIN, TransitGoSettingsTokens.HEADER_BLUR_MAX, headerBlur())
                    }
                )
            }
        ) { padding ->
            val nav = padding.calculateBottomPadding()
            // Gap above the chip; the gap below is this plus TransitGoSettingsTokens.NAV_BAR_OPTICAL. From the first row's
            // top to the screen's bottom: rows + gap + chip + gap + optical = the room under the
            // header, so: gap = (room - rows - chip - optical) / 2
            val gaps = TransitGoSettingsTokens.END_ROWS.mapNotNull { key ->
                spanFrom(key)?.let { span ->
                    // A row under a section label sits a little below the header (the label
                    // hides beneath it); a row right after another card sits with that card's
                    // bottom exactly at the header's edge, so no sliver of it shows
                    val below = if (key in TransitGoSettingsTokens.ROWS_AFTER_CARD) TransitGoSettingsTokens.ROW_SPACING else TransitGoSettingsTokens.END_ROW_GAP
                    with(density) {
                        ((screenPx - headerBottomPx - below.toPx() - span - (TransitGoSettingsTokens.CHIP_HEIGHT + TransitGoSettingsTokens.NAV_BAR_OPTICAL).toPx()) / 2f).toDp()
                    }
                }
            }
            val chipGap = (gaps.firstOrNull { it >= nav + TransitGoSettingsTokens.CHIP_GAP_MIN } ?: gaps.lastOrNull())
                ?.coerceIn(nav + TransitGoSettingsTokens.CHIP_GAP_MIN, nav + TransitGoSettingsTokens.CHIP_GAP_MAX)
                ?: (nav + TransitGoSettingsTokens.LIST_BOTTOM_GAP)
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding(),
                    // The gesture bar takes up more visual space than its inset, so a little
                    // extra room below keeps the Source code chip looking centered
                    bottom = chipGap + TransitGoSettingsTokens.NAV_BAR_OPTICAL
                ),
                verticalArrangement = Arrangement.spacedBy(TransitGoSettingsTokens.ROW_SPACING),
                state = listState,
                modifier = Modifier.fillMaxSize()
                    .hazeSource(haze)
                    // Note where each touch starts, without taking the touch from the list
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            touchStartedAtEnd = !listState.canScrollForward
                            touchStartY = down.position.y
                        }
                    }
                    .nestedScroll(pullToOpen.connection)
            ) {
                item { Column { TransitGoSettingsPreview(activity, settings, settingsVersion) } }

                item { SectionHeader(R.string.section_card) }
                item(key = TransitGoSettingsTokens.KEY_FIRST_ROW) {
                    ToggleRow(
                        Icons.Rounded.DirectionsBus, MaterialShapes.Cookie9Sided.toShape(),
                        R.string.setting_chip_title, R.string.setting_chip_summary,
                        settings.showRouteChip, Position.First, colorfulWhenOn = true
                    ) { settings.showRouteChip = it }
                }
                item(key = TransitGoSettingsTokens.KEY_EXTRA_ROW) { ExtraRow(settings, MaterialShapes.Arch.toShape()) }

                item(key = TransitGoSettingsTokens.KEY_LOCK_HEADER) { SectionHeader(R.string.section_lock_screen) }
                item(key = TransitGoSettingsTokens.KEY_HIDE_ROW) {
                    ToggleRow(
                        Icons.Rounded.VisibilityOff, MaterialShapes.Sunny.toShape(),
                        R.string.setting_hide_title, R.string.setting_hide_summary,
                        settings.hideNotification, Position.Single, rerollExample = false,
                        // Eye closes (hidden) or opens (shown) with a blink
                        iconWhenOff = Icons.Rounded.Visibility
                    ) { settings.hideNotification = it }
                }

                item(key = TransitGoSettingsTokens.KEY_PERMISSIONS_HEADER) { SectionHeader(R.string.section_permissions) }
                item(key = TransitGoSettingsTokens.KEY_LAST_ROW) {
                    AccessRow(accessGranted, MaterialShapes.Pill.toShape()) { openNotificationAccess() }
                }

                // Sits at the end of the list; grows as you pull past the end (see rememberPullToOpen)
                item {
                    // Same gap above the resting chip as below it (to the screen's bottom edge,
                    // past the gesture bar): footer = that gap + the chip, minus the row spacing
                    // the list already adds above it
                    Box(Modifier.fillMaxWidth().height(chipGap - TransitGoSettingsTokens.ROW_SPACING + TransitGoSettingsTokens.CHIP_HEIGHT)) {
                        SourceChip(
                            chipPhase,
                            Modifier.align(Alignment.BottomCenter)
                                // Grows up past the footer's top as it's pulled, so the list's
                                // length never changes
                                .wrapContentHeight(Alignment.Bottom, unbounded = true)
                                .onGloballyPositioned { chipBounds = it.boundsInRoot() }
                                // Hidden while the full-screen version takes over from it
                                .graphicsLayer { alpha = if (takeover > 0f) 0f else 1f }
                        )
                    }
                }
            }
        }
        SourceTakeover(takeover, chipBounds)
        }
    }

    /**
     * Second half of the pull: the full-width Source code button grows from where it sits until
     * it fills the whole screen (corners straightening, icon and text getting bigger), right as
     * the repo opens. Letting go shrinks it back into the button.
     */
    @Composable
    private fun SourceTakeover(t: Float, from: Rect?) {
        if (t <= 0f || from == null) return
        val colors = MaterialTheme.colorScheme
        Surface(
            shape = RoundedCornerShape(lerp(32.dp, 0.dp, t)),
            color = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
            modifier = Modifier
                .fillMaxSize()
                // Place a box between the button's bounds and the full screen
                .layout { measurable, c ->
                    val w = c.maxWidth.toFloat(); val h = c.maxHeight.toFloat()
                    val left = from.left * (1 - t)
                    val top = from.top * (1 - t)
                    val right = from.right + (w - from.right) * t
                    val bottom = from.bottom + (h - from.bottom) * t
                    val placeable = measurable.measure(
                        Constraints.fixed((right - left).roundToInt(), (bottom - top).roundToInt())
                    )
                    layout(c.maxWidth, c.maxHeight) { placeable.place(left.roundToInt(), top.roundToInt()) }
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                SourceLabel(scale = TransitGoSettingsTokens.LABEL_FULL_BUTTON + (TransitGoSettingsTokens.LABEL_FULL_SCREEN - TransitGoSettingsTokens.LABEL_FULL_BUTTON) * t)
            }
        }
    }

    private fun randomAccent(not: Color? = null) =
        (TransitGoSettingsTokens.ACCENT_PALETTE - setOfNotNull(not)).random()

    /** Arrival time / Time left / Off, as a segmented button under the row's text. */
    @Composable
    private fun ExtraRow(settings: Settings, iconShape: Shape) {
        var selected by remember { mutableStateOf(settings.extra) }
        val options = listOf(
            Settings.Extra.ARRIVAL to R.string.setting_extra_arrival,
            Settings.Extra.TIME_LEFT to R.string.setting_extra_time_left,
            Settings.Extra.NONE to R.string.setting_extra_none
        )
        Surface(
            shape = Position.Last.shape(),
            color = MaterialTheme.colorScheme.surfaceBright,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge(Icons.Rounded.Schedule, iconShape)
                    RowTexts(R.string.setting_extra_title, R.string.setting_extra_summary, Modifier.weight(1f).padding(start = 16.dp))
                }
                // Full width (not indented under the text) so "Arrival Time" fits
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(top = 14.dp)) {
                    options.forEachIndexed { i, (value, label) ->
                        SegmentedButton(
                            selected = selected == value,
                            onClick = {
                                selected = value
                                settings.extra = value
                                // Keep the same example so the change to its time is easy to see
                                onSettingChanged(rerollExample = false)
                            },
                            shape = SegmentedButtonDefaults.itemShape(i, options.size)
                        ) { FadingText(stringResource(label), LocalTextStyle.current, LocalContentColor.current) }
                    }
                }
            }
        }
    }

    /**
     * "[GitHub] Source code" chip at the bottom, opening the repo in the browser.
     * At [progress] 0 it's a small chip; it grows as you pull past the end of the list until, at
     * 1 (when the repo opens), it's a full-width button: taller, bigger icon and text, tinted.
     */
    @Composable
    private fun SourceChip(progress: Float, modifier: Modifier) {
        val colors = MaterialTheme.colorScheme
        val p = progress
        Surface(
            onClick = { openSource() },
            shape = RoundedCornerShape(50),
            color = lerp(colors.surfaceContainerHigh, colors.primaryContainer, p),
            contentColor = lerp(colors.onSurface, colors.onPrimaryContainer, p),
            border = BorderStroke(1.dp, lerp(colors.outlineVariant, colors.primary.copy(alpha = 0.5f), p)),
            modifier = modifier
                .fillMaxWidth()
                // Width goes from the chip's natural width to the full row
                .layout { measurable, constraints ->
                    val natural = measurable.maxIntrinsicWidth(constraints.maxHeight)
                    val w = (natural + (constraints.maxWidth - natural) * p).toInt()
                        .coerceIn(natural.coerceAtMost(constraints.maxWidth), constraints.maxWidth)
                    val placeable = measurable.measure(constraints.copy(minWidth = w, maxWidth = w))
                    layout(constraints.maxWidth, placeable.height) {
                        placeable.place((constraints.maxWidth - w) / 2, 0)
                    }
                }
                .height(lerp(TransitGoSettingsTokens.CHIP_HEIGHT, TransitGoSettingsTokens.CHIP_HEIGHT_FULL, p))
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                SourceLabel(scale = 1f + (TransitGoSettingsTokens.LABEL_FULL_BUTTON - 1f) * p)
            }
        }
    }

    /**
     * "[GitHub] Source code", laid out once at its smallest size and enlarged with [scale] on the
     * GPU, so growing it doesn't re-measure the text on every frame.
     */
    @Composable
    private fun SourceLabel(scale: Float) = Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Icon(painterResource(R.drawable.ic_github), null, Modifier.size(18.dp))
        Spacer(Modifier.size(8.dp))
        // "Source code · v1.3.0", the version a bit dimmer; read from versionName so it
        // updates with every release
        val dim = LocalContentColor.current.copy(alpha = 0.7f)
        Text(
            buildAnnotatedString {
                append(stringResource(R.string.source_code))
                withStyle(SpanStyle(color = dim)) { append(" · v${BuildConfig.VERSION_NAME}") }
            },
            style = MaterialTheme.typography.labelLarge, maxLines = 1
        )
    }

    @Composable
    private fun ToggleRow(
        icon: ImageVector, iconShape: Shape, title: Int, summary: Int,
        initial: Boolean, position: Position, rerollExample: Boolean = true,
        colorfulWhenOn: Boolean = false, iconWhenOff: ImageVector? = null, onChange: (Boolean) -> Unit
    ) {
        var checked by remember { mutableStateOf(initial) }
        var accent by remember { mutableStateOf(randomAccent()) }
        val toggle = { value: Boolean ->
            if (value && !checked) accent = randomAccent(accent)
            checked = value
            onChange(value)
            onSettingChanged(rerollExample)
        }
        val badgeColor = if (colorfulWhenOn && checked) accent else null
        val shownIcon = if (checked || iconWhenOff == null) icon else iconWhenOff
        SettingRow(shownIcon, iconShape, title, summary, position, badgeColor, onClick = { toggle(!checked) }) {
            Switch(
                checked = checked,
                onCheckedChange = toggle,
                thumbContent = if (checked) {
                    { Icon(Icons.Rounded.Check, null, Modifier.size(SwitchDefaults.IconSize)) }
                } else null
            )
        }
    }

    @Composable
    private fun LinkRow(icon: ImageVector, iconShape: Shape, title: Int, summary: Int, onClick: () -> Unit) =
        SettingRow(icon, iconShape, title, summary, Position.Single, null, onClick) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

    /**
     * Notification access, with its state easy to read at a glance: on, a green badge and an
     * "On ✓" pill; off, a red badge, a "Turn on" pill and an arrow to the switch.
     */

}
