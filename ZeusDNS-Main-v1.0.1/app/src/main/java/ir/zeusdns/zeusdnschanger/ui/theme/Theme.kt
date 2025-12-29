package ir.zeusdns.zeusdnschanger.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import ir.zeusdns.zeusdnschanger.PersianFonts
import ir.zeusdns.zeusdnschanger.viewmodel.MainViewModel
import java.util.Locale

@Composable
fun GoldTheme(
    viewModel: MainViewModel,
    content: @Composable () -> Unit
) {
    val isDarkTheme = viewModel.isDarkThemeEnabled()
    val context = LocalContext.current

    LaunchedEffect(isDarkTheme) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isDarkTheme
            isAppearanceLightNavigationBars = !isDarkTheme
        }
    }
    val language = viewModel.appSettings.language
    val isPersian = language == "fa" ||
            (language == "system" && Locale.getDefault().language == "fa")

    val layoutDirection = when {
        isPersian -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    val typography = Typography(
        displayLarge = MaterialTheme.typography.displayLarge.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        displayMedium = MaterialTheme.typography.displayMedium.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        displaySmall = MaterialTheme.typography.displaySmall.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        headlineLarge = MaterialTheme.typography.headlineLarge.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        headlineMedium = MaterialTheme.typography.headlineMedium.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        headlineSmall = MaterialTheme.typography.headlineSmall.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        titleLarge = MaterialTheme.typography.titleLarge.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        titleMedium = MaterialTheme.typography.titleMedium.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        titleSmall = MaterialTheme.typography.titleSmall.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        bodyLarge = MaterialTheme.typography.bodyLarge.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        bodySmall = MaterialTheme.typography.bodySmall.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        labelLarge = MaterialTheme.typography.labelLarge.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        labelMedium = MaterialTheme.typography.labelMedium.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        ),
        labelSmall = MaterialTheme.typography.labelSmall.copy(
            fontFamily = if (isPersian) PersianFonts.default else FontFamily.Default,
            textAlign = if (isPersian) TextAlign.End else TextAlign.Start,
            textDirection = if (isPersian) TextDirection.Rtl else TextDirection.Ltr
        )
    )

    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = GoldColor,
            onPrimary = Color.Black,
            background = DarkBackground,
            surface = CardBackground,
            onSurface = Color.White,
            surfaceVariant = CardBackgroundVariant
        )
    } else {
        lightColorScheme(
            primary = GoldColor,
            onPrimary = Color.White,
            background = LightBackground,
            surface = LightCardBackground,
            onSurface = LightTextColor,
            surfaceVariant = LightCardBackgroundVariant
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection
        ) {
            content()
        }
    }
}