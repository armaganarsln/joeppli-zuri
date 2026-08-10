package gl.joeppli.zueri.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Layout scale for the whole app.
 *
 * Screens used to pick spacing ad hoc (8/12/16/20/24 with no pattern), which is
 * what made the app feel unaligned from screen to screen. Everything vertical
 * now comes from here.
 */
object Dimens {
    /** Tight pairing — a label and the value it belongs to. */
    val gapXs = 4.dp
    /** Inside a group — rows of a card, chips in a row. */
    val gapSm = 8.dp
    val gapMd = 12.dp
    /** Card padding and the default gap between related blocks. */
    val gapLg = 16.dp
    /** Between major sections of a screen. */
    val section = 24.dp

    /** Horizontal inset for screen content. */
    val screenH = 16.dp
    /** Above the first element on a tab screen. */
    val screenTop = 12.dp
    /**
     * Below the last element. The Scaffold already reserves the bottom bar, so
     * this is breathing room only — never the bar height.
     */
    val screenBottom = 24.dp
    /** Onboarding screens render outside the Scaffold and centre their content. */
    val screenEdgeWide = 24.dp

    /** Hero/feature cards: the primary CTA on a screen. */
    val cardHero = RoundedCornerShape(24.dp)
    /** Standard content cards and list rows. */
    val card = RoundedCornerShape(16.dp)
    /** Inline chips, badges, small containers. */
    val chip = RoundedCornerShape(12.dp)

    /** Filled CTA height — matches the 28dp corner radius convention. */
    val ctaHeight = 56.dp
    val ctaShape = RoundedCornerShape(28.dp)
}
