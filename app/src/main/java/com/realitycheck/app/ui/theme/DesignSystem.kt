package com.realitycheck.app.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
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
    
    this
        .clickable(interactionSource = interactionSource, indication = null) {}
        .scale(scale)
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

