package dev.marco.smartspacer.ubereats

import android.content.Intent
import android.os.Build
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.net.toUri
import androidx.compose.animation.core.animate
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import android.os.Bundle
import android.provider.Settings as AndroidSettings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeliveryDining
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.basicMarquee
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.text.rememberTextMeasurer
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider

/**
 * Settings screen, opened from the Target's settings in Smartspacer or the app drawer.
 * Material 3 Expressive with wallpaper (dynamic) colors and Google Sans Text.
 */
class SettingsActivity : ComponentActivity() {

    private companion object {
        const val SOURCE_URL = "https://github.com/INFORMATION89/UberEats-Smartspacer"
        /** Notification access is on: the same green in light and dark, so "working" reads at a glance */
        val ACCESS_GREEN = Color(0xFF1E8E3E)
        /** Share of the screen's height to drag past the end for the Source code chip to open the repo */
        const val PULL_SCREEN_SHARE = 0.41f
        /** Width of the fade at each edge of preview text that doesn't fit */
        val FADE_EDGE = 24.dp
        /** A swipe starting higher up needs this share of the room above the finger... */
        const val PULL_ROOM_SHARE = 0.7f
        /** ...but at least this share of the screen's height */
        const val PULL_MIN_SHARE = 0.18f
        /** Letting go faster than this (upward, per second) finishes the pull and opens the repo */
        val FLICK_VELOCITY = 1200.dp
        /** ...if the chip was pulled at least this share of the way, so a tiny twitch doesn't count */
        const val FLICK_MIN_PULL = 0.08f
        /** How long the chip takes to finish filling the screen after a flick */
        const val FLICK_FINISH_MS = 260
        /** Share of the pull spent growing the chip into a full-width button (the rest fills the screen) */
        const val CHIP_PHASE = 0.35f
        /** Source code label size on the full-width button and on the full screen (1 = chip) */
        const val LABEL_FULL_BUTTON = 1.3f
        const val LABEL_FULL_SCREEN = 2.2f
        /** Gap above the Source code chip until the rows have been measured (then it's computed) */
        val LIST_BOTTOM_GAP = 18.dp
        /** Limits for the computed gap above the chip (on top of the gesture bar's height). The
         *  maximum is generous on purpose: a clean end under the header matters more than the
         *  chip's spacing, which grows in languages with longer rows (about 95dp in French) */
        val CHIP_GAP_MIN = 8.dp
        val CHIP_GAP_MAX = 160.dp
        /** Scrolled to the end, space between the header and the first Card row */
        val END_ROW_GAP = 8.dp
        const val KEY_FIRST_ROW = "first_card_row"
        const val KEY_EXTRA_ROW = "extra_row"
        const val KEY_HIDE_ROW = "hide_row"
        const val KEY_LAST_ROW = "notification_access"
        const val KEY_LOCK_HEADER = "lock_screen_header"
        const val KEY_PERMISSIONS_HEADER = "permissions_header"
        /** Everything from the first Card row to Notification access, in order */
        val END_ITEMS = listOf(KEY_FIRST_ROW, KEY_EXTRA_ROW, KEY_LOCK_HEADER, KEY_HIDE_ROW, KEY_PERMISSIONS_HEADER, KEY_LAST_ROW)
        /** Rows the list can stop at (just under the header), in order of preference */
        val END_ROWS = listOf(KEY_FIRST_ROW, KEY_EXTRA_ROW, KEY_HIDE_ROW, KEY_LAST_ROW)
        /** Of those, the rows that come right after another card (no section label above) */
        val ROWS_AFTER_CARD = setOf(KEY_EXTRA_ROW)
        /** Extra space under the list only, to balance the gesture bar's visual weight */
        val NAV_BAR_OPTICAL = 16.dp
        /** Header blur at the top of the list, and once scrolled [HEADER_BLUR_DISTANCE] */
        val HEADER_BLUR_MIN = 8.dp
        val HEADER_BLUR_MAX = 28.dp
        val HEADER_BLUR_DISTANCE = 240.dp
        /** Shadow under the header: how far it reaches, and how dark it starts */
        val HEADER_SHADOW = 10.dp
        const val HEADER_SHADOW_ALPHA = 0.025f
        /** How much of the background color covers the blurred list behind the header */
        const val HEADER_TINT_ALPHA = 0.72f
        /** How collapsed the header is (0..1) when its shadow starts to appear */
        const val SHADOW_FROM = 0.5f
        /** Space between rows in the list */
        val ROW_SPACING = 2.dp
        /** Source code chip at rest, and as the full-width button it grows into */
        val CHIP_HEIGHT = 40.dp
        val CHIP_HEIGHT_FULL = 64.dp
    }

    private var accessGranted by mutableStateOf(false)
    /** Bumped on every setting change so the preview re-reads the settings */
    private var settingsVersion by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { SettingsTheme { SettingsScreen() } }
    }

    override fun onResume() {
        super.onResume()
        // Refresh when coming back from the notification access screen
        accessGranted = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        // After an app update Android sometimes leaves the listener "bound" but disconnected;
        // asking for a rebind fixes that (does nothing if it's already connected)
        if (accessGranted) runCatching {
            android.service.notification.NotificationListenerService.requestRebind(
                android.content.ComponentName(this, EatsListener::class.java)
            )
        }
    }

    private fun openSource() = startActivity(Intent(Intent.ACTION_VIEW, SOURCE_URL.toUri()))

    /** Opens the plugin's own notification access switch (or the full list if that page is missing). */
    private fun openNotificationAccess() {
        val detail = Intent(AndroidSettings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS).putExtra(
            AndroidSettings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME,
            android.content.ComponentName(this, EatsListener::class.java).flattenToString()
        )
        runCatching { startActivity(detail) }
            .onFailure { startActivity(Intent(AndroidSettings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
    }

    /** Saves a change, redraws the preview and tells Smartspacer to redraw the card. */
    private fun onSettingChanged(rerollExample: Boolean = true) {
        settingsVersion++
        // With no live order, show a different example each time, so the change is easy to see
        // (not for settings the preview doesn't show, like the lock screen one)
        if (rerollExample && OrderStore.state == null) SampleOrder.reroll()
        SmartspacerTargetProvider.notifyChange(this, UberEatsTarget::class.java)
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
    @Composable
    private fun SettingsScreen() {
        val settings = remember { Settings(this) }
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
                    .filter { it.key in END_ITEMS }
                    .associate { it.key as String to it.size }
            }.collect { itemSizes.putAll(it) }
        }
        val rowSpacingPx = with(LocalDensity.current) { ROW_SPACING.roundToPx() }
        // From a row's top to the bottom of Notification access, or null until all are known
        val spanFrom = { key: String ->
            val keys = END_ITEMS.drop(END_ITEMS.indexOf(key))
            val sizes = keys.map { itemSizes[it] }
            if (sizes.any { it == null }) null else sizes.sumOf { it!! } + rowSpacingPx * (keys.size - 1)
        }
        val shadowColor = Color.Black
        // 0 at the top of the list, 1 once it has scrolled HEADER_BLUR_DISTANCE
        val blurDistancePx = with(LocalDensity.current) { HEADER_BLUR_DISTANCE.toPx() }
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
        val chipPhase = FastOutSlowInEasing.transform((progress / CHIP_PHASE).coerceIn(0f, 1f))
        val takeover = FastOutSlowInEasing.transform(((progress - CHIP_PHASE) / (1f - CHIP_PHASE)).coerceIn(0f, 1f))
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
                            val shown = ((scroll.state.collapsedFraction - SHADOW_FROM) / (1f - SHADOW_FROM)).coerceIn(0f, 1f)
                            if (shown > 0f) drawRect(
                                Brush.verticalGradient(
                                    0f to shadowColor.copy(alpha = HEADER_SHADOW_ALPHA * shown),
                                    1f to Color.Transparent,
                                    startY = size.height, endY = size.height + HEADER_SHADOW.toPx()
                                ),
                                topLeft = Offset(0f, size.height),
                                size = Size(size.width, HEADER_SHADOW.toPx())
                            )
                        }
                        // The screen's own background color, mostly opaque, and no grain: the
                        // header matches the background exactly, with only the blurred list
                        // showing through it
                        .hazeEffect(
                            haze,
                            HazeStyle(
                                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                                tint = HazeTint(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = HEADER_TINT_ALPHA)),
                                noiseFactor = 0f
                            )
                        ) {
                        blurRadius = lerp(HEADER_BLUR_MIN, HEADER_BLUR_MAX, headerBlur())
                    }
                )
            }
        ) { padding ->
            val nav = padding.calculateBottomPadding()
            // Gap above the chip; the gap below is this plus NAV_BAR_OPTICAL. From the first row's
            // top to the screen's bottom: rows + gap + chip + gap + optical = the room under the
            // header, so: gap = (room - rows - chip - optical) / 2
            val gaps = END_ROWS.mapNotNull { key ->
                spanFrom(key)?.let { span ->
                    // A row under a section label sits a little below the header (the label
                    // hides beneath it); a row right after another card sits with that card's
                    // bottom exactly at the header's edge, so no sliver of it shows
                    val below = if (key in ROWS_AFTER_CARD) ROW_SPACING else END_ROW_GAP
                    with(density) {
                        ((screenPx - headerBottomPx - below.toPx() - span - (CHIP_HEIGHT + NAV_BAR_OPTICAL).toPx()) / 2f).toDp()
                    }
                }
            }
            val chipGap = (gaps.firstOrNull { it >= nav + CHIP_GAP_MIN } ?: gaps.lastOrNull())
                ?.coerceIn(nav + CHIP_GAP_MIN, nav + CHIP_GAP_MAX)
                ?: (nav + LIST_BOTTOM_GAP)
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding(),
                    // The gesture bar takes up more visual space than its inset, so a little
                    // extra room below keeps the Source code chip looking centered
                    bottom = chipGap + NAV_BAR_OPTICAL
                ),
                verticalArrangement = Arrangement.spacedBy(ROW_SPACING),
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
                item { Column { PreviewCard(settings) } }

                item { SectionHeader(R.string.section_card) }
                item(key = KEY_FIRST_ROW) {
                    ToggleRow(
                        Icons.Rounded.DeliveryDining, MaterialShapes.Cookie9Sided.toShape(),
                        R.string.setting_courier_title, R.string.setting_courier_summary,
                        settings.showCourier, Position.First, colorfulWhenOn = true
                    ) { settings.showCourier = it }
                }
                item(key = KEY_EXTRA_ROW) { ExtraRow(settings, MaterialShapes.Arch.toShape()) }

                item(key = KEY_LOCK_HEADER) { SectionHeader(R.string.section_lock_screen) }
                item(key = KEY_HIDE_ROW) {
                    ToggleRow(
                        Icons.Rounded.VisibilityOff, MaterialShapes.Sunny.toShape(),
                        R.string.setting_hide_title, R.string.setting_hide_summary,
                        settings.hideNotification, Position.Single, rerollExample = false,
                        // Eye closes (hidden) or opens (shown) with a blink
                        iconWhenOff = Icons.Rounded.Visibility
                    ) { settings.hideNotification = it }
                }

                item(key = KEY_PERMISSIONS_HEADER) { SectionHeader(R.string.section_permissions) }
                item(key = KEY_LAST_ROW) {
                    AccessRow(accessGranted, MaterialShapes.Pill.toShape()) { openNotificationAccess() }
                }

                // Sits at the end of the list; grows as you pull past the end (see rememberPullToOpen)
                item {
                    // Same gap above the resting chip as below it (to the screen's bottom edge,
                    // past the gesture bar): footer = that gap + the chip, minus the row spacing
                    // the list already adds above it
                    Box(Modifier.fillMaxWidth().height(chipGap - ROW_SPACING + CHIP_HEIGHT)) {
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
                SourceLabel(scale = LABEL_FULL_BUTTON + (LABEL_FULL_SCREEN - LABEL_FULL_BUTTON) * t)
            }
        }
    }

    /**
     * Mirrors the Smartspacer card: title, then the card icon (same order as the Target:
     * courier photo > app icon > bag glyph), the subtitle, and the app's logo without its
     * background ("[Uber] Eats" for the Uber app, just the logo for Uber Eats).
     * With no live order it shows an example order, so the settings' effect is still visible.
     * Switching between the two springs (bouncy pop-in and height change).
     */
    @Composable
    private fun PreviewCard(settings: Settings) {
        settingsVersion // re-read the settings when one changes
        val live = OrderStore.state
        // New random example each time the settings open, and again after an order ends
        val sample = remember(SampleOrder.generation) { SampleOrder.create(this@SettingsActivity) }
        // The app the live order is in, or Uber Eats (else Uber) for the example
        val pkg = live?.sourcePackage ?: remember { EatsIcon.defaultPackage(this@SettingsActivity) }
        val sourceName = EatsIcon.name(pkg)
        val appIcon = remember(pkg) { EatsIcon.get(this@SettingsActivity, pkg) }
        val glyph = remember(pkg) { EatsIcon.glyph(this@SettingsActivity, pkg) }
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
                val state = if (isLive) OrderStore.state ?: sample else sample
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
                        val courier = state.courier?.takeIf { settings.showCourier }
                        val iconKind = when {
                            courier != null -> IconKind.Courier
                            appIcon != null -> IconKind.App
                            else -> IconKind.Glyph
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
                                    IconKind.Courier -> courier?.let { Image(it.asImageBitmap(), null, Modifier.size(26.dp)) }
                                    IconKind.App -> appIcon?.let { Image(it.asImageBitmap(), sourceName, Modifier.size(24.dp)) }
                                    IconKind.Glyph -> Icon(painterResource(R.drawable.ic_order), null, Modifier.size(22.dp), tint = onCard)
                                }
                            }
                        }
                        Spacer(Modifier.size(8.dp))
                        // Subtitle change (e.g. "· 12 min" -> "· 60%"): the text
                        // cross-fades while its width springs to the new size, so the app's
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
                            // "· [Uber] Eats", like the real card
                            Spacer(Modifier.size(8.dp))
                            Text(SOURCE_BULLET.trim(), style = MaterialTheme.typography.titleMedium, color = onCard)
                            Spacer(Modifier.size(8.dp))
                            Image(it.asImageBitmap(), sourceName, Modifier.size(28.dp), colorFilter = ColorFilter.tint(onCard))
                            EatsIcon.afterLogo(pkg).takeIf { t -> t.isNotEmpty() }?.let { t ->
                                Spacer(Modifier.size(4.dp))
                                Text(t, style = MaterialTheme.typography.titleMedium, color = onCard)
                            }
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

    /** Pull-past-the-end state: [progress] 0..1, and the scroll connection that drives it. */
    private class PullToOpen(val connection: NestedScrollConnection, private val pull: () -> Float, private val max: () -> Float) {
        val progress get() = (pull() / max()).coerceIn(0f, 1f)
    }

    /**
     * Normal scrolling and flinging are untouched. A pull only starts when the finger touched
     * down while the list was already at the end ([armed]), so swiping or flicking down to the
     * bottom never triggers it. Then the drag left over past the end is the pull, 1:1 with the
     * finger: dragging about [PULL_SCREEN_SHARE] of the screen's height (from near the bottom to
     * near the top) calls [onOpen] once, with a vibration. Letting go springs it back to 0.
     * A swipe that starts higher up ([startY]) needs less, so it can open from anywhere: most
     * of the room above the finger, but never less than [PULL_MIN_SHARE] of the screen.
     */
    @Composable
    private fun rememberPullToOpen(armed: () -> Boolean, startY: () -> Float, onOpen: () -> Unit): PullToOpen {
        val screen = LocalWindowInfo.current.containerSize.height.toFloat()
        val currentStartY by rememberUpdatedState(startY)
        val maxFor = { y: Float -> (y * PULL_ROOM_SHARE).coerceIn(screen * PULL_MIN_SHARE, screen * PULL_SCREEN_SHARE) }
        val maxPx = screen * PULL_SCREEN_SHARE
        val currentArmed by rememberUpdatedState(armed)
        val haptics = LocalHapticFeedback.current
        val flickPx = with(LocalDensity.current) { FLICK_VELOCITY.toPx() }
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
                    val flicked = -available.y >= flickPx && pull.floatValue >= needed.floatValue * FLICK_MIN_PULL
                    springBack.value = scope.launch {
                        try {
                            if (flicked && !opened.value) {
                                animate(pull.floatValue, needed.floatValue, animationSpec = tween(FLICK_FINISH_MS, easing = FastOutSlowInEasing)) { v, _ ->
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

    /**
     * One line of text. If it's too long to fit, it fades out at both edges instead of being
     * cut, and slowly scrolls to show the rest (the start fade appears once it's moving).
     */
    @Composable
    private fun FadingText(text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
        val measurer = rememberTextMeasurer()
        val fade = with(LocalDensity.current) { FADE_EDGE.toPx() }
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

    /** Which icon the card shows, so the preview can animate when it changes */
    private enum class IconKind { Courier, App, Glyph }

    /** Bright colors like Android's colorful settings icons; never the same one twice in a row */
    private val accents = listOf(
        Color(0xFF4285F4), Color(0xFF34A853), Color(0xFFF9AB00), Color(0xFFEA4335),
        Color(0xFFA142F4), Color(0xFF24C1E0), Color(0xFFFA7B17), Color(0xFFF538A0)
    )
    private fun randomAccent(not: Color? = null) = (accents - setOfNotNull(not)).random()

    /** Gentle bounce used by the preview's in-card transitions */
    private fun <T> springy() = spring<T>(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow)

    /** Time left / Progress / Off, as a segmented button under the row's text. */
    @Composable
    private fun ExtraRow(settings: Settings, iconShape: Shape) {
        var selected by remember { mutableStateOf(settings.extra) }
        val options = listOf(
            Settings.Extra.TIME_LEFT to R.string.setting_extra_time_left,
            Settings.Extra.PROGRESS to R.string.setting_extra_progress,
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
                // Full width (not indented under the text) so "Tiempo restante" fits
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
                .height(lerp(CHIP_HEIGHT, CHIP_HEIGHT_FULL, p))
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                SourceLabel(scale = 1f + (LABEL_FULL_BUTTON - 1f) * p)
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
        // "Source code · v0.1.0", the version a bit dimmer; read from versionName so it
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
    private fun SectionHeader(text: Int) = Text(
        stringResource(text),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )

    private enum class Position { First, Middle, Last, Single }

    // Grouped rows: big outer corners, small inner ones (Android 16 settings style)
    private fun Position.shape(): Shape {
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
    @Composable
    private fun AccessRow(granted: Boolean, iconShape: Shape, onClick: () -> Unit) {
        val colors = MaterialTheme.colorScheme
        SettingRow(
            if (granted) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
            iconShape, R.string.setting_access,
            if (granted) R.string.setting_access_on else R.string.setting_access_off,
            Position.Single, if (granted) ACCESS_GREEN else colors.error, onClick
        ) {
            Surface(
                shape = CircleShape,
                color = if (granted) ACCESS_GREEN else colors.errorContainer,
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
    private fun SettingRow(
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
    private fun IconBadge(icon: ImageVector, shape: Shape, accent: Color? = null) {
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
    private fun RowTexts(title: Int, summary: Int, modifier: Modifier) = Column(modifier) {
        Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(summary),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    // Wallpaper colors need Android 12; older versions get the standard Material colors
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialExpressiveTheme(
        colorScheme = colors,
        motionScheme = MotionScheme.expressive(),
        typography = GoogleSansTypography,
        content = content
    )
}

/** Google Sans Text, the font Smartspacer and its other plugins use in their settings. */
private val GoogleSansText = FontFamily(
    Font(R.font.google_sans_text_regular, FontWeight.Normal),
    Font(R.font.google_sans_text_medium, FontWeight.Medium),
    Font(R.font.google_sans_text_medium, FontWeight.SemiBold),
    Font(R.font.google_sans_text_bold, FontWeight.Bold)
)

/** The default Material 3 type scale with every style switched to Google Sans Text. */
private val GoogleSansTypography: Typography = Typography().run {
    fun TextStyle.gs() = copy(fontFamily = GoogleSansText)
    Typography(
        displayLarge = displayLarge.gs(), displayMedium = displayMedium.gs(), displaySmall = displaySmall.gs(),
        headlineLarge = headlineLarge.gs(), headlineMedium = headlineMedium.gs(), headlineSmall = headlineSmall.gs(),
        titleLarge = titleLarge.gs(), titleMedium = titleMedium.gs(), titleSmall = titleSmall.gs(),
        bodyLarge = bodyLarge.gs(), bodyMedium = bodyMedium.gs(), bodySmall = bodySmall.gs(),
        labelLarge = labelLarge.gs(), labelMedium = labelMedium.gs(), labelSmall = labelSmall.gs()
    )
}
