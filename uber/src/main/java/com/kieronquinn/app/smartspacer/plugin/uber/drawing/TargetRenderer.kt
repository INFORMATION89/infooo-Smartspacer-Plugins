package com.kieronquinn.app.smartspacer.plugin.uber.drawing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Typeface
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.scale
import com.kieronquinn.app.smartspacer.plugin.uber.R
import com.kieronquinn.app.smartspacer.plugin.uber.model.NotificationData
import com.kieronquinn.app.smartspacer.plugin.uber.utils.extensions.makeSquare
import com.kieronquinn.app.smartspacer.plugin.uber.utils.extensions.resize
import kotlin.math.atan2
import com.kieronquinn.app.shared.R as SharedR

private const val CANVAS_WIDTH = 768
private const val CANVAS_HEIGHT = 432
private const val CANVAS_PADDING = 64f
private const val CANVAS_ROUNDED_CORNERS = 32f
private const val OUTLINE_STROKE_WIDTH = 8f
private const val VEHICLE_SIZE = 96
private const val IMAGES_WIDTH = 312
private const val IMAGE_SIZE = 118
private const val FONT_BIGGER = 53f
private const val FONT_SMALLER = 40f
private const val FONT_PROGRESS = 32f
private const val PROGRESS_LABEL_GAP = 8f

private val drawPaint = Paint().apply {
    strokeWidth = OUTLINE_STROKE_WIDTH
    strokeCap = Paint.Cap.ROUND
    style = Paint.Style.STROKE
}

private val fillPaint = Paint().apply {
    style = Paint.Style.FILL
}

private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
    textAlign = Paint.Align.CENTER
}

fun Context.drawNotificationData(
    data: NotificationData
): Bitmap = Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Bitmap.Config.ARGB_8888).apply {
    val canvas = Canvas(this)
    canvas.drawNotificationData(this@drawNotificationData, data)
}

private fun Canvas.drawNotificationData(
    context: Context,
    data: NotificationData
) {
    val font = context.getFont()
    fillPaint.color = context.getBackground()
    drawRect(0f, 0f, width.toFloat(), height.toFloat(), fillPaint)
    val progressCar = data.progressCar?.makeSquare()?.scale(VEHICLE_SIZE, VEHICLE_SIZE)?.apply {
        density = 0
    }
    drawProgress(
        data.progress,
        progressCar,
        context.getUnfilledLineColour(),
        context.getFilledLineColour()
    )
    drawProgressPercentage(data.progress, context, font)
    val driver = data.driver?.scale(IMAGE_SIZE, IMAGE_SIZE)?.apply { density = 0 }
    val car = data.car?.resize(IMAGES_WIDTH, IMAGE_SIZE)?.apply { density = 0 }
    val hasImages = driver != null || car != null
    val title = data.expandedTitle
    val subtitle = data.expandedSubtitle
    //Without images, centre the text block in the space inside the progress track
    val textBlockHeight = FONT_BIGGER + if(subtitle != null) 8f + FONT_SMALLER else 0f
    val titleTop = if(hasImages) {
        val imagesTop = CANVAS_PADDING * 1.5f
        drawImages(driver, car, imagesTop)
        imagesTop + IMAGE_SIZE + 16f + FONT_BIGGER
    } else {
        (height - textBlockHeight) / 2f + FONT_BIGGER * 0.8f
    }
    val subtitleTop = titleTop + 8f + FONT_SMALLER
    val maxTextWidth = width - (CANVAS_PADDING * 2f) - 32f
    textPaint.color = context.getTextColour()
    if(title != null) {
        textPaint.typeface = Typeface.create(font, Typeface.BOLD)
        textPaint.textSize = FONT_BIGGER
        drawText(title.ellipsize(maxTextWidth), width / 2f, titleTop, textPaint)
    }
    if(subtitle != null) {
        textPaint.typeface = font
        textPaint.textSize = FONT_SMALLER
        drawText(subtitle.ellipsize(maxTextWidth), width / 2f, subtitleTop, textPaint)
    }
}

private fun Canvas.drawProgressPercentage(
    progress: Float?,
    context: Context,
    font: Typeface?
) {
    val label = progress.toProgressPercentage() ?: return
    textPaint.color = context.getTextColour()
    textPaint.typeface = font
    textPaint.textSize = FONT_PROGRESS
    textPaint.textAlign = Paint.Align.RIGHT
    drawText(
        label,
        width - CANVAS_PADDING,
        CANVAS_PADDING + FONT_PROGRESS + PROGRESS_LABEL_GAP,
        textPaint
    )
    textPaint.textAlign = Paint.Align.CENTER
}

private fun Canvas.drawImages(driver: Bitmap?, car: Bitmap?, top: Float) {
    when {
        car != null && driver != null -> {
            //Original layout: driver photo overlaps the left of the car image
            val imagesLeft = (width / 2f) - (car.width / 2f)
            drawBitmap(car, imagesLeft, top, null)
            drawBitmap(driver, imagesLeft - 8f, top, null)
        }
        car != null -> drawBitmap(car, (width / 2f) - (car.width / 2f), top, null)
        driver != null -> drawBitmap(driver, (width / 2f) - (driver.width / 2f), top, null)
    }
}

private fun String.ellipsize(maxWidth: Float): String {
    return TextUtils.ellipsize(this, textPaint, maxWidth, TextUtils.TruncateAt.END).toString()
}

private fun Context.getBackground(): Int {
    return ContextCompat.getColor(this, R.color.colour_background)
}

private fun Context.getTextColour(): Int {
    return ContextCompat.getColor(this, R.color.colour_text)
}

private fun Context.getFilledLineColour(): Int {
    return ContextCompat.getColor(this, R.color.progress_bar_filled)
}

private fun Context.getUnfilledLineColour(): Int {
    return ContextCompat.getColor(this, R.color.progress_bar_unfilled)
}

private fun Context.getFont(): Typeface? {
    return ResourcesCompat.getFont(this, SharedR.font.google_sans_text)
}

private fun Canvas.drawProgress(
    end: Float?,
    car: Bitmap?,
    unfilledColour: Int,
    filledColour: Int
) {
    save()
    translate(0f, height.toFloat())
    scale(1f, -1f)
    val startOffset = -0.03f
    val endOffset = 0.01f
    drawPaint.color = unfilledColour
    drawPath(createRoundedPath(0f + endOffset, 1f + startOffset), drawPaint)
    //No progress available: just show the empty track
    if(end == null) {
        restore()
        return
    }
    val offsetEnd = 1f - end.coerceIn(0.05f, 1f)
    drawPaint.color = filledColour
    val progressPath = createRoundedPath(offsetEnd + endOffset, 1f + startOffset)
    drawPath(progressPath, drawPaint)
    if(car != null) {
        drawBitmap(car, progressPath.getVehiclePosition(), null)
    }
    restore()
}

private fun createRoundedPath(start: Float, end: Float): Path {
    val roundedRectPath = Path()
    roundedRectPath.addRoundRect(
        CANVAS_PADDING,
        CANVAS_PADDING,
        CANVAS_WIDTH - CANVAS_PADDING,
        CANVAS_HEIGHT - CANVAS_PADDING,
        CANVAS_ROUNDED_CORNERS,
        CANVAS_ROUNDED_CORNERS,
        Path.Direction.CW
    )
    val pathMeasure = PathMeasure(roundedRectPath, true)
    return Path().apply {
        val startD = start * pathMeasure.length
        val stopD = end * pathMeasure.length
        pathMeasure.getSegment(startD, stopD, this, true)
    }
}

private fun Path.getVehiclePosition() = Matrix().apply {
    val pathMeasure = PathMeasure(this@getVehiclePosition, true)
    val rotationMatrix = Matrix()
    val tan = floatArrayOf(0f, 0f)
    val pos = floatArrayOf(0f, 0f)
    pathMeasure.getPosTan(1f, pos, tan)
    pathMeasure.getMatrix(1f, rotationMatrix, 0)
    val degrees = atan2(tan[1], tan[0]) * 180.0 / Math.PI
    val vehiclePivot = VEHICLE_SIZE / 2f
    postRotate(degrees.toFloat() + 180f, vehiclePivot, vehiclePivot)
    postTranslate(pos[0] - vehiclePivot, pos[1] - vehiclePivot)
}