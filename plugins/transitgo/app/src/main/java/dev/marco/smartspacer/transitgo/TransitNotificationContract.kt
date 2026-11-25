package dev.marco.smartspacer.transitgo

/**
 * Observed integration points for Transit GO notifications.
 *
 * These identifiers belong to Transit, not this plugin. They are private implementation
 * details and may change when Transit updates; keep them centralized and device-verified.
 */
internal object TransitNotificationContract {
    const val PACKAGE_NAME = "com.thetransitapp.droid"
    const val GO_CHANNEL_ID = "go_mode"

    const val ROUTE_CHIP = "floatingServiceName"
    const val ROUTE_CHIP_CONTAINER = "floatingServiceNameContainer"
    const val ROUTE_CHIP_TEXT = "text"
    const val STEP_ICON = "image"
    const val ARRIVAL_TEXT = "leftTextView"
    const val TIME_LEFT_TEXT = "rightTextView"
    const val PREVIOUS_INSTRUCTION = "previous_instruction"

    /** Horizontal notification inset used when measuring the expanded RemoteViews layout. */
    const val MEASURE_HORIZONTAL_INSET_DP = 32
    /** Maximum edge for a bitmap passed from the notification layout to Smartspacer. */
    const val MAX_RENDERED_BITMAP_EDGE_PX = 512
}
