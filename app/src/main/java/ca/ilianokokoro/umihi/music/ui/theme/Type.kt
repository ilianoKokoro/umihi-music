package ca.ilianokokoro.umihi.music.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import ca.ilianokokoro.umihi.music.R

val nunitoFontFamily = FontFamily(
    Font(R.font.nunito_extralight, FontWeight.ExtraLight, FontStyle.Normal),
    Font(R.font.nunito_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.nunito, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.nunito_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.nunito_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.nunito_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(R.font.nunito_black, FontWeight.Black, FontStyle.Normal),
)
private val defaultTypography = Typography()
val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),
    displayMedium = defaultTypography.displayMedium.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),
    displaySmall = defaultTypography.displaySmall.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),

    headlineLarge = defaultTypography.headlineLarge.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = defaultTypography.headlineMedium.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = defaultTypography.headlineSmall.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),

    titleLarge = defaultTypography.titleLarge.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),
    titleMedium = defaultTypography.titleMedium.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),
    titleSmall = defaultTypography.titleSmall.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),

    bodyLarge = defaultTypography.bodyLarge.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),
    bodyMedium = defaultTypography.bodyMedium.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),
    bodySmall = defaultTypography.bodySmall.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),

    labelLarge = defaultTypography.labelLarge.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),
    labelMedium = defaultTypography.labelMedium.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    ),
    labelSmall = defaultTypography.labelSmall.copy(
        fontFamily = nunitoFontFamily,
        fontWeight = FontWeight.Bold
    )
)