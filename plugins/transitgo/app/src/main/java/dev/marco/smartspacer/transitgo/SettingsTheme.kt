package dev.marco.smartspacer.transitgo

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

import dev.marco.smartspacer.transitgo.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SettingsTheme(content: @Composable () -> Unit) {
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

