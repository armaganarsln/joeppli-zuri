package gl.joeppli.zueri.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import gl.joeppli.zueri.data.RecyclingRepository

// 1. Blue Theme Scheme (Light) - Default Theme
private val BlueLightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = PureWhite,
    primaryContainer = BluePrimaryContainerLight,
    onPrimaryContainer = BlueOnPrimaryContainerLight,
    secondary = BrandGreen,
    onSecondary = PureWhite,
    secondaryContainer = GreenPrimaryContainerLight,
    onSecondaryContainer = GreenOnPrimaryContainerLight,
    tertiary = BrandYellow,
    onTertiary = Color(0xFF3A2E00),
    background = SoftGrey,
    surface = PureWhite,
    onBackground = DarkSlate,
    onSurface = DarkSlate,
    surfaceVariant = SoftGrey,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = PureWhite,
    surfaceContainer = Color(0xFFF1F2EF),
    surfaceContainerHigh = Color(0xFFEAEBE8),
    surfaceContainerHighest = Color(0xFFE4E5E2)
)

// 2. Green Theme Scheme (Light)
private val GreenLightColorScheme = lightColorScheme(
    primary = BrandGreen,
    onPrimary = PureWhite,
    primaryContainer = GreenPrimaryContainerLight,
    onPrimaryContainer = GreenOnPrimaryContainerLight,
    secondary = BrandBlue,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFFDDE1FF),
    onSecondaryContainer = Color(0xFF001454),
    tertiary = BrandYellow,
    onTertiary = Color(0xFF3A2E00),
    background = SoftGrey,
    surface = PureWhite,
    onBackground = DarkSlate,
    onSurface = DarkSlate,
    surfaceVariant = SoftGrey,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = PureWhite,
    surfaceContainer = Color(0xFFF1F2EF),
    surfaceContainerHigh = Color(0xFFEAEBE8),
    surfaceContainerHighest = Color(0xFFE4E5E2)
)

// 3. Yellow Theme Scheme (Light)
private val YellowLightColorScheme = lightColorScheme(
    primary = BrandYellow,
    onPrimary = DarkSlate,
    primaryContainer = YellowPrimaryContainerLight,
    onPrimaryContainer = YellowOnPrimaryContainerLight,
    secondary = BrandBlue,
    onSecondary = PureWhite,
    secondaryContainer = BluePrimaryContainerLight,
    onSecondaryContainer = BlueOnPrimaryContainerLight,
    tertiary = BrandGreen,
    onTertiary = PureWhite,
    background = SoftGrey,
    surface = PureWhite,
    onBackground = DarkSlate,
    onSurface = DarkSlate,
    surfaceVariant = SoftGrey,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceContainerLowest = PureWhite,
    surfaceContainerLow = PureWhite,
    surfaceContainer = Color(0xFFF1F2EF),
    surfaceContainerHigh = Color(0xFFEAEBE8),
    surfaceContainerHighest = Color(0xFFE4E5E2)
)

// 4. Dark Theme Scheme (Dark) - The single dark mode option
private val DarkColorScheme = darkColorScheme(
    primary = BrandGreen,
    onPrimary = PureWhite,
    primaryContainer = GreenPrimaryContainerDark,
    onPrimaryContainer = GreenOnPrimaryContainerDark,
    secondary = BrandBlue,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF2A3BA0),
    onSecondaryContainer = Color(0xFFDDE1FF),
    tertiary = BrandYellow,
    onTertiary = Color(0xFF3A2E00),
    background = NeutralDark,
    surface = SurfaceDarkCard,
    onBackground = SoftGrey,
    onSurface = SoftGrey,
    surfaceVariant = Color(0xFF3E4152),
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceContainerLowest = NeutralDark,
    surfaceContainerLow = NeutralDark,
    surfaceContainer = SurfaceDarkCard,
    surfaceContainerHigh = Color(0xFF45495B),
    surfaceContainerHighest = Color(0xFF505469)
)

@Composable
fun ZueriJoeppliTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val themeState by RecyclingRepository.theme.collectAsState()
    val colorScheme = when (themeState) {
        "green" -> GreenLightColorScheme
        "yellow" -> YellowLightColorScheme
        "dark" -> DarkColorScheme
        else -> BlueLightColorScheme // default
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
