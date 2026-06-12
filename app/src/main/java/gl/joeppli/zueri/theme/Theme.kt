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

// Green Theme Schemes
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

private val GreenDarkColorScheme = darkColorScheme(
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

// Blue Theme Schemes
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

private val BlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF7CA6F9), // Lightened blue for dark mode primary
    onPrimary = Color(0xFF002B66),
    primaryContainer = BluePrimaryContainerDark,
    onPrimaryContainer = BlueOnPrimaryContainerDark,
    secondary = BrandGreen,
    onSecondary = PureWhite,
    secondaryContainer = GreenPrimaryContainerDark,
    onSecondaryContainer = GreenOnPrimaryContainerDark,
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

// Yellow Theme Schemes
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

private val YellowDarkColorScheme = darkColorScheme(
    primary = BrandYellow,
    onPrimary = DarkSlate,
    primaryContainer = YellowPrimaryContainerDark,
    onPrimaryContainer = YellowOnPrimaryContainerDark,
    secondary = BrandBlue,
    onSecondary = PureWhite,
    secondaryContainer = BluePrimaryContainerDark,
    onSecondaryContainer = BlueOnPrimaryContainerDark,
    tertiary = BrandGreen,
    onTertiary = PureWhite,
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

// Red Theme Schemes
private val RedLightColorScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = PureWhite,
    primaryContainer = RedPrimaryContainerLight,
    onPrimaryContainer = RedOnPrimaryContainerLight,
    secondary = BrandBlue,
    onSecondary = PureWhite,
    secondaryContainer = BluePrimaryContainerLight,
    onSecondaryContainer = BlueOnPrimaryContainerLight,
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

private val RedDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB4AB), // Lightened red for dark mode
    onPrimary = Color(0xFF690005),
    primaryContainer = RedPrimaryContainerDark,
    onPrimaryContainer = RedOnPrimaryContainerDark,
    secondary = BrandBlue,
    onSecondary = PureWhite,
    secondaryContainer = BluePrimaryContainerDark,
    onSecondaryContainer = BlueOnPrimaryContainerDark,
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
        "blue" -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
        "yellow" -> if (darkTheme) YellowDarkColorScheme else YellowLightColorScheme
        "red" -> if (darkTheme) RedDarkColorScheme else RedLightColorScheme
        else -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
