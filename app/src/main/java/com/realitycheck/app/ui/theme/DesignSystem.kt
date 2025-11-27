package com.realitycheck.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modern 2025 Design System
 * Consistent spacing, elevation, and animation tokens
 */

// Spacing Scale (4/8/12px base)
object Spacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val huge: Dp = 48.dp
}

// Elevation System (Material 3 inspired)
object Elevation {
    val none: Dp = 0.dp
    val sm: Dp = 1.dp
    val md: Dp = 2.dp
    val lg: Dp = 4.dp
    val xl: Dp = 8.dp
    val xxl: Dp = 12.dp
}

// Corner Radius Scale
object Radius {
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val round: Dp = 999.dp
}

// Animation Durations
object Animation {
    val fast: Int = 150
    val normal: Int = 300
    val slow: Int = 500
    
    // Easing curves
    val standardEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val decelerateEasing: Easing = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val accelerateEasing: Easing = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
}

/**
 * Press animation modifier for interactive elements
 * Note: This modifier is for press-only animations. For clickable elements,
 * use Material's default indication which includes focus indicators.
 */
fun Modifier.pressAnimation(): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale"
    )
    
    this.scale(scale)
}

/**
 * Fade in animation for content
 */
@Composable
fun fadeInAnimationSpec(): AnimationSpec<Float> = tween(
    durationMillis = Animation.normal,
    easing = Animation.standardEasing
)

/**
 * Slide in animation for content
 */
@Composable
fun slideInAnimationSpec(): AnimationSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

/**
 * Accessibility: Focus indicator colors
 * Material 3 provides default focus indicators, but these can be customized
 * for enhanced visibility (WCAG 2.2 requires 3:1 contrast for UI components)
 * 
 * Note: Material 3 automatically handles focus indicators with proper contrast.
 * These values are provided for custom implementations if needed.
 */
object FocusColors {
    // Focus ring color - uses theme primary for proper contrast
    @Composable
    fun getFocusRingColor(): Color {
        return MaterialTheme.colorScheme.primary
    }
    
    // Focus border width (2-3px recommended for visibility)
    val FocusBorderWidth = 2.dp
    
    // Focus outline width for enhanced visibility
    val FocusOutlineWidth = 3.dp
}

/**
 * Enhanced clickable modifier with focus support
 * Use this for clickable cards and custom elements that need focus indicators
 * 
 * Material 3's default indication (ripple) includes focus indicators when elements are focusable.
 * This modifier ensures elements are focusable and use Material's default indication.
 * 
 * Note: Material 3 automatically handles focus indicators with proper contrast when:
 * 1. Elements are made focusable with .focusable()
 * 2. Elements use Material's default indication (not indication = null)
 * 3. Elements are navigable via keyboard or accessibility services
 */
fun Modifier.enhancedClickable(
    onClick: () -> Unit
): Modifier = composed {
    // Use Material's default indication which includes focus indicators
    // Material 3 automatically handles focus indicators with proper contrast
    val source = remember { MutableInteractionSource() }
    this
        .focusable(interactionSource = source)
        .clickable(onClick = onClick)
        // Note: Material 3's default indication (ripple) includes focus indicators
        // We don't set indication = null, so focus indicators are automatically shown
        // Focus indicators appear when the element is focused via keyboard navigation
}

