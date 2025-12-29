package ir.zeusdns.zeusdnschanger

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

object PersianFonts {
    val vazirmatn = FontFamily(
        Font(R.font.vazirmatn_light, FontWeight.Light),
        Font(R.font.vazirmatn_regular, FontWeight.Normal),
        Font(R.font.vazirmatn_medium, FontWeight.Medium),
        Font(R.font.vazirmatn_bold, FontWeight.Bold),
    )

    val default = vazirmatn
}