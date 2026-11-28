package com.kieronquinn.app.smartspacer.plugin.uber.ui.screens.configuration

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Icon as AndroidIcon
import com.kieronquinn.app.smartspacer.plugin.uber.R
import com.kieronquinn.app.smartspacer.plugin.uber.model.TargetData
import com.kieronquinn.app.smartspacer.plugin.uber.targets.UberTarget
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate

private const val PREVIEW_TARGET_ID = "uber-settings-preview"

/**
 * Builds a preview-only target model. It deliberately omits the source notification key,
 * notification metadata and pending intent so rendering cannot dismiss or open a real trip.
 */
internal fun Context.createUberPreviewTarget(
    data: TargetData?,
    showExpandedInfo: Boolean,
    exampleImage: Bitmap?
): SmartspaceTarget {
    val title = data?.title ?: getString(R.string.preview_example_title)
    val subtitle = data?.subtitle ?: getString(R.string.preview_example_subtitle)
    val targetIcon = Icon(AndroidIcon.createWithResource(this, R.drawable.ic_uber))
    val component = ComponentName(this, UberTarget::class.java)
    val image = when {
        !showExpandedInfo -> null
        data != null -> data.image
        else -> exampleImage
    }

    return if(image != null) {
        TargetTemplate.Image(
            this,
            PREVIEW_TARGET_ID,
            component,
            SmartspaceTarget.FEATURE_COMMUTE_TIME,
            Text(title),
            Text(subtitle),
            targetIcon,
            Icon(AndroidIcon.createWithBitmap(image)),
            TapAction(pendingIntent = null)
        ).create()
    } else {
        TargetTemplate.Basic(
            PREVIEW_TARGET_ID,
            component,
            SmartspaceTarget.FEATURE_UNDEFINED,
            Text(title),
            Text(subtitle),
            targetIcon,
            TapAction(pendingIntent = null)
        ).create()
    }
}
