package com.watnapp.etipitaka.plus.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Immutable
data class ETipitakaColors(
    val tabStripLine: Color = Color(0xFF595959),
    val read: Color = Color(0xFF00BFE5),
    val skimmed: Color = Color(0xFF7FCAE6),
    val translucentControl: Color = Color(0xDFE7E7E7),
    val subtitleBackground: Color = Color(0xFFBBBBBB),
    val subtitleText: Color = Color(0xFFEEEEEE),
    val readerLightText: Color = Color(0xFF010101),
    val readerLightBackground: Color = Color(0xFFFEFEFE),
    val readerDarkText: Color = Color(0xFFFEFEFE),
    val readerDarkBackground: Color = Color(0xFF010101),
    val readerSepiaText: Color = Color(0xFF5E4933),
    val readerSepiaBackground: Color = Color(0xFFF9EFD8),
    val itemMarker: Color = Color(0xFFEE00EE),
    val subItemMarker: Color = Color(0xFF89C200),
    val keywordText: Color = Color(0xFF0000FF),
    val keywordBackground: Color = Color(0xFFFFFF00),
)

@Immutable
data class ETipitakaSpacing(
    val slidingMenuOffset: Dp = 60.dp,
    val listPadding: Dp = 10.dp,
    val shadowWidth: Dp = 15.dp,
    val readerBottomControlsSpace: Dp = 55.dp,
)

@Immutable
data class ReaderColorPreset(
    val text: Color,
    val background: Color,
)

private val LocalETipitakaColors = staticCompositionLocalOf { ETipitakaColors() }
private val LocalETipitakaSpacing = staticCompositionLocalOf { ETipitakaSpacing() }

object ETipitakaThemeTokens {
    val colors: ETipitakaColors
        @Composable
        @ReadOnlyComposable
        get() = LocalETipitakaColors.current

    val spacing: ETipitakaSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalETipitakaSpacing.current

    val readerLight: ReaderColorPreset
        @Composable
        @ReadOnlyComposable
        get() = ReaderColorPreset(colors.readerLightText, colors.readerLightBackground)

    val readerDark: ReaderColorPreset
        @Composable
        @ReadOnlyComposable
        get() = ReaderColorPreset(colors.readerDarkText, colors.readerDarkBackground)

    val readerSepia: ReaderColorPreset
        @Composable
        @ReadOnlyComposable
        get() = ReaderColorPreset(colors.readerSepiaText, colors.readerSepiaBackground)
}

private val ETipitakaColorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() {
        val colors = ETipitakaThemeTokens.colors
        return lightColorScheme(
            primary = colors.tabStripLine,
            secondary = colors.read,
            tertiary = colors.skimmed,
            background = colors.readerLightBackground,
            surface = colors.readerLightBackground,
            onPrimary = Color.White,
            onSecondary = colors.readerLightText,
            onTertiary = colors.readerLightText,
            onBackground = colors.readerLightText,
            onSurface = colors.readerLightText,
        )
    }

private val ETipitakaTypography = Typography(
    bodyLarge = Typography().bodyLarge.copy(fontSize = 18.sp),
    titleMedium = Typography().titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
)

@Composable
fun ETipitakaTheme(
    colors: ETipitakaColors = ETipitakaColors(),
    spacing: ETipitakaSpacing = ETipitakaSpacing(),
    content: @Composable () -> Unit,
) {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalETipitakaColors provides colors,
        LocalETipitakaSpacing provides spacing,
    ) {
        MaterialTheme(
            colorScheme = ETipitakaColorScheme,
            typography = ETipitakaTypography,
            content = content,
        )
    }
}

@Composable
fun ComposeFoundationSmokeText(text: String) {
    ETipitakaTheme {
        Surface {
            Text(
                text = text,
                modifier = Modifier.padding(PaddingValues(ETipitakaThemeTokens.spacing.listPadding)),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ComposeFoundationSmokeTextPreview() {
    ComposeFoundationSmokeText(text = "E-Tipitaka")
}

@Preview(showBackground = true)
@Composable
private fun ETipitakaThemeBridgePreview() {
    ETipitakaTheme {
        val colors = ETipitakaThemeTokens.colors
        Column {
            Text(
                text = "Subtitle",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.subtitleBackground)
                    .padding(ETipitakaThemeTokens.spacing.listPadding),
                color = colors.subtitleText,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(ETipitakaThemeTokens.spacing.listPadding))
            Text(
                text = "Reader text",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.readerLightBackground)
                    .padding(ETipitakaThemeTokens.spacing.listPadding),
                color = colors.readerLightText,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
