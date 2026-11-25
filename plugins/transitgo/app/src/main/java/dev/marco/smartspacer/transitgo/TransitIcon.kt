package dev.marco.smartspacer.transitgo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.drawable.toBitmap

/**
 * Transit's own app icon, loaded from the installed Transit app at runtime
 * (nothing of Transit's is bundled in this plugin). Null if Transit isn't installed.
 */
object TransitIcon {
    @Volatile private var cached: Bitmap? = null
    @Volatile private var cachedGlyph: Bitmap? = null

    /** The full icon, green background included. */
    fun get(context: Context, size: Int = 192): Bitmap? {
        cached?.let { return it }
        return try {
            context.packageManager.getApplicationIcon(TransitNotificationContract.PACKAGE_NAME)
                .toBitmap(size, size)
                .copy(Bitmap.Config.ARGB_8888, false)
                .also { cached = it }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Just the logo, without the background: the adaptive icon's monochrome layer
     * (themed icons, Android 13+) or else its foreground layer. Meant to be tinted.
     */
    fun glyph(context: Context, size: Int = 192): Bitmap? {
        cachedGlyph?.let { return it }
        return try {
            val icon = context.packageManager.getApplicationIcon(TransitNotificationContract.PACKAGE_NAME)
            val layer: Drawable = (icon as? AdaptiveIconDrawable)?.let {
                (if (Build.VERSION.SDK_INT >= 33) it.monochrome else null) ?: it.foreground
            } ?: return null
            // Adaptive layers are 108dp with the visible logo in the middle 72dp:
            // draw 1.5x larger and centered so the logo fills the bitmap
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val inset = size / 4
            layer.setBounds(-inset, -inset, size + inset, size + inset)
            layer.draw(Canvas(bitmap))
            bitmap.also { cachedGlyph = it }
        } catch (e: Exception) {
            null
        }
    }
}
