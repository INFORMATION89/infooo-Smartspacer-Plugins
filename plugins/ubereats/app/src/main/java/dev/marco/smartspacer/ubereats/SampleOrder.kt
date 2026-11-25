package dev.marco.smartspacer.ubereats

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.format.DateFormat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import java.util.Date
import kotlin.random.Random

/**
 * Made-up orders for the settings preview, so the settings' effect on the card can be seen
 * when no order is on its way. Never shown on the real card.
 *
 * Each one is a random stage (preparing, picking up, on the way, almost there) with a random
 * restaurant, courier (a colored initial stands in for their photo) and times. Nothing runs
 * in the background: the preview makes a new one when it's first shown and whenever
 * [generation] changes, which [reroll] bumps on a setting change or when an order ends.
 */
object SampleOrder {

    /** Compose state: the preview creates a new example whenever this changes */
    var generation by mutableIntStateOf(0)
        private set

    /** Ask for a new example next time the preview draws. Cheap: just bumps a counter. */
    fun reroll() { generation++ }

    private val avatarColors = listOf(
        0xFF1565C0, 0xFF2E7D32, 0xFFC62828, 0xFFEF6C00, 0xFF6A1B9A, 0xFF00897B, 0xFFAD1457, 0xFF5D4037
    )

    fun create(context: Context): OrderState {
        val res = context.resources
        val restaurant = res.getStringArray(R.array.sample_restaurants).random()
        val courierName = res.getStringArray(R.array.sample_couriers).random()
        val courier = drawAvatar(courierName.first().uppercaseChar(), avatarColors.random().toInt())
        fun minutes(n: Int) = "$n min"
        // Expected time in the phone's own clock format ("7:42 PM", "19:42")
        fun at(min: Int) = DateFormat.getTimeFormat(context).format(Date(System.currentTimeMillis() + min * 60_000L))

        return when (Random.nextInt(4)) {
            // "Preparing your order / Tacos El Güero · 25 min" (no courier yet)
            0 -> Random.nextInt(18, 36).let { left ->
                OrderState(
                    title = res.getString(R.string.sample_preparing), text = restaurant,
                    contentIntent = null, timeLeft = minutes(left), progress = Random.nextInt(15, 35) / 100f
                )
            }
            // "Luis is picking up your order / Tacos El Güero"
            1 -> Random.nextInt(12, 24).let { left ->
                OrderState(
                    title = res.getString(R.string.sample_picking_up, courierName), text = restaurant,
                    contentIntent = null, courier = courier, timeLeft = minutes(left),
                    progress = Random.nextInt(40, 60) / 100f
                )
            }
            // "Arriving at 7:42 PM / Luis is heading your way"
            2 -> Random.nextInt(5, 15).let { left ->
                OrderState(
                    title = res.getString(R.string.sample_arriving_at, at(left)),
                    text = res.getString(R.string.sample_on_the_way, courierName),
                    contentIntent = null, courier = courier, timeLeft = minutes(left),
                    progress = Random.nextInt(65, 85) / 100f
                )
            }
            // "Almost there / Luis is nearby"
            else -> OrderState(
                title = res.getString(R.string.sample_almost_there),
                text = res.getString(R.string.sample_nearby, courierName),
                contentIntent = null, courier = courier, timeLeft = minutes(Random.nextInt(1, 4)),
                progress = Random.nextInt(88, 98) / 100f
            )
        }
    }

    /** A round avatar: the courier's initial in white on a color, standing in for their photo. */
    private fun drawAvatar(initial: Char, color: Int): Bitmap {
        val size = 192
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 96f
            textAlign = Paint.Align.CENTER
        }
        val baseline = size / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(initial.toString(), size / 2f, baseline, paint)
        return bitmap
    }
}
