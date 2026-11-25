package dev.marco.smartspacer.ubereats

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.drawable.toBitmap

/**
 * The app icon of Uber Eats or the Uber app (whichever the order is in), loaded from the
 * installed app at runtime (nothing of Uber's is bundled in this plugin). Null if the app
 * isn't installed.
 */
object EatsIcon {
    private val cached = java.util.concurrent.ConcurrentHashMap<String, Bitmap>()
    private val cachedGlyph = java.util.concurrent.ConcurrentHashMap<String, Bitmap>()

    /** Uber Eats if it's installed, else the Uber app: for when there's no order to go by. */
    fun defaultPackage(context: Context): String = EatsListener.EATS_PACKAGE.takeIf {
        runCatching { context.packageManager.getApplicationInfo(it, 0) }.isSuccess
    } ?: EatsListener.UBER_PACKAGE

    /** The app's name, for accessibility */
    fun name(pkg: String?): String = if (pkg == EatsListener.UBER_PACKAGE) "Uber" else SOURCE_NAME

    /**
     * Text after the logo on the card. The Uber app's logo is the word "Uber", so "Eats" after it
     * reads "Uber Eats"; Uber Eats' own logo already says it all, so nothing follows it.
     */
    fun afterLogo(pkg: String?): String = if (pkg == EatsListener.UBER_PACKAGE) "Eats" else ""

    /** The full icon, background included. */
    fun get(context: Context, pkg: String = defaultPackage(context), size: Int = 192): Bitmap? {
        cached[pkg]?.let { return it }
        return try {
            context.packageManager.getApplicationIcon(pkg)
                .toBitmap(size, size)
                .copy(Bitmap.Config.ARGB_8888, false)
                .also { cached[pkg] = it }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Just the logo, without the background: the adaptive icon's monochrome layer (themed icons,
     * Android 13+) or else its foreground layer, trimmed to the logo itself and centered on a
     * square, so it's as big as the spot allows. Meant to be tinted.
     */
    fun glyph(context: Context, pkg: String = defaultPackage(context), size: Int = 192): Bitmap? {
        cachedGlyph[pkg]?.let { return it }
        return try {
            val icon = context.packageManager.getApplicationIcon(pkg)
            val layer: Drawable = (icon as? AdaptiveIconDrawable)?.let {
                (if (Build.VERSION.SDK_INT >= 33) it.monochrome else null) ?: it.foreground
            } ?: return null
            val full = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            layer.setBounds(0, 0, size, size)
            layer.draw(Canvas(full))
            trimToSquare(full)?.also { cachedGlyph[pkg] = it }
        } catch (e: Exception) {
            null
        }
    }

    /** Crops away the transparent space around the logo, then centers it on a square. */
    private fun trimToSquare(b: Bitmap): Bitmap? {
        val w = b.width; val h = b.height
        val px = IntArray(w * h).also { b.getPixels(it, 0, w, 0, 0, w, h) }
        var left = w; var top = h; var right = -1; var bottom = -1
        for (y in 0 until h) for (x in 0 until w) {
            if ((px[y * w + x] ushr 24) > 16) {
                if (x < left) left = x; if (x > right) right = x
                if (y < top) top = y; if (y > bottom) bottom = y
            }
        }
        if (right < 0) return null
        val cw = right - left + 1; val ch = bottom - top + 1
        val side = maxOf(cw, ch)
        val out = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        Canvas(out).drawBitmap(
            Bitmap.createBitmap(b, left, top, cw, ch),
            (side - cw) / 2f, (side - ch) / 2f, null
        )
        return out
    }
}
