package dev.marco.smartspacer.transitgo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import android.text.format.DateFormat
import androidx.annotation.PluralsRes
import java.util.Date
import kotlin.random.Random

/**
 * Made-up trips for the settings preview, so the settings' effect on the card can be seen
 * when no GO trip is running. Never shown on the real card.
 *
 * Each one is a random kind of step (waiting, riding, walking, bike station) with a random
 * route chip (name, color and shape), step icon and times. Nothing runs in the background:
 * the preview makes a new one when it's first shown (each time the settings open) and
 * whenever [generation] changes, which [reroll] bumps on a setting change or when a GO
 * trip ends.
 */
object SampleTrip {

    /** Compose state: the preview creates a new example whenever this changes */
    var generation by mutableIntStateOf(0)
        private set

    /** Ask for a new example next time the preview draws. Cheap: just bumps a counter. */
    fun reroll() { generation++ }

    private enum class Shape { ROUNDED, PILL, CIRCLE, SQUARE }

    private val routes = listOf(
        "57-A" to 0xFF2E8B1E, "12" to 0xFF1565C0, "L3" to 0xFFC62828, "T1" to 0xFFEF6C00,
        "8B" to 0xFF6A1B9A, "C-40" to 0xFF00897B, "2" to 0xFFAD1457, "M7" to 0xFF5D4037,
        "105" to 0xFF283593, "R" to 0xFFF9A825
    )

    fun create(context: Context): GoState {
        val res = context.resources
        val (route, color) = routes.random()
        val chip = drawChip(route, color.toInt(), randomShape(route))
        val tripMinutes = Random.nextInt(15, 181)
        // Arrival in the phone's own clock format and language ("6:44 PM", "18:44", "6:44 p.m.")
        val arrival = DateFormat.getTimeFormat(context)
            .format(Date(System.currentTimeMillis() + tripMinutes * 60_000L))
        val timeLeft = duration(tripMinutes)
        val stop = res.getStringArray(R.array.sample_stops).random()
        fun icon(@DrawableRes id: Int) = ContextCompat.getDrawable(context, id)?.toBitmap(96, 96)
        fun plural(@PluralsRes id: Int, n: Int) = res.getQuantityString(id, n, n)

        // Wording follows the phone's language, like Transit (English, or Spanish on a Spanish phone)
        return when (Random.nextInt(4)) {
            // Waiting for the bus: "Leave in 7 minutes / [57-A] in 18 minutes"
            0 -> {
                val leave = Random.nextInt(1, 21)
                GoState(
                    title = plural(R.plurals.sample_leave_in, leave),
                    text = plural(R.plurals.sample_in_minutes, leave + Random.nextInt(5, 16)),
                    contentIntent = null, chip = chip, routeName = route, stepIcon = icon(R.drawable.ic_sample_wait),
                    arrival = arrival, timeLeft = timeLeft
                )
            }
            // On board: "Get off in 4 stops / [12] in 9 minutes"
            1 -> GoState(
                title = plural(R.plurals.sample_get_off_in, Random.nextInt(1, 13)),
                text = plural(R.plurals.sample_in_minutes, Random.nextInt(2, 30)),
                contentIntent = null, chip = chip, routeName = route, stepIcon = icon(R.drawable.ic_transit_go),
                arrival = arrival, timeLeft = timeLeft
            )
            // Walking to the stop: "Walk 6 min / to Central Station"
            2 -> GoState(
                title = res.getString(R.string.sample_walk, Random.nextInt(2, 16)),
                text = res.getString(R.string.sample_to, stop),
                contentIntent = null, chip = chip, routeName = route, stepIcon = icon(R.drawable.ic_sample_walk),
                arrival = arrival, timeLeft = timeLeft
            )
            // Bike station (like Transit's real one): "3 bikes / Walk to …", no route chip
            else -> GoState(
                title = Random.nextInt(0, 16).let { n -> plural(R.plurals.sample_bikes, n).let { if (n < 3) "${TransitGoLogic.CAUTION} $it" else it } }, text = null,
                previous = res.getString(R.string.sample_walk_to, stop),
                contentIntent = null, stepIcon = icon(if (Random.nextBoolean()) R.drawable.ic_sample_walk else R.drawable.ic_sample_bike),
                arrival = arrival, timeLeft = timeLeft
            )
        }
    }

    /** Circles only fit short names like "2" or "L3" */
    private fun randomShape(route: String) =
        if (route.length <= 2) Shape.entries.random() else listOf(Shape.ROUNDED, Shape.PILL, Shape.SQUARE).random()

    /** Transit's style: "16 min" or "3 h 07 min" */
    private fun duration(min: Int) =
        if (min < 60) "$min min" else "%d h %02d min".format(min / 60, min % 60)

    /** A route chip like Transit's: colored shape with white bold text, centered on a square. */
    private fun drawChip(text: String, color: Int, shape: Shape): Bitmap {
        val size = 219
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 78f
        }
        val h = 129f
        val w = if (shape == Shape.CIRCLE) h else maxOf(paint.measureText(text) + 36f, h)
        val radius = when (shape) {
            Shape.ROUNDED -> 30f
            Shape.PILL, Shape.CIRCLE -> h / 2f
            Shape.SQUARE -> 10f
        }
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val left = (size - w) / 2f
        val top = (size - h) / 2f
        canvas.drawRoundRect(RectF(left, top, left + w, top + h), radius, radius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color })
        val baseline = top + h / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(text, left + (w - paint.measureText(text)) / 2f, baseline, paint)
        return bitmap
    }
}