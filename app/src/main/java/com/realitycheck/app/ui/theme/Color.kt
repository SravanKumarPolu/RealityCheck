package com.realitycheck.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Colors (Legacy - kept for compatibility)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// RealityCheck Brand Colors - Modern 2025 Palette
val BrandPrimary = Color(0xFF6C5CE7)
val BrandPrimaryDark = Color(0xFF5A4FCF)
val BrandPrimaryLight = Color(0xFF8B7FED)
val BrandSecondary = Color(0xFFA29BFE)
val BrandAccent = Color(0xFFFD79A8)
val BrandSuccess = Color(0xFF00B894)
val BrandSuccessLight = Color(0xFF00D4A3)
val BrandWarning = Color(0xFFFDCB6E)
val BrandError = Color(0xFFE17055)
val BrandErrorLight = Color(0xFFE8846A)
val BrandErrorDark = Color(0xFFC85A3F)

// Surface Colors - Modern Light Theme
val BackgroundLight = Color(0xFFFAFBFC)
val SurfaceElevated = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF5F6F8)

// Surface Colors - Modern Dark Theme
val BackgroundDark = Color(0xFF121212)
val SurfaceDark = Color(0xFF1E1E1E)
val SurfaceElevatedDark = Color(0xFF2C2C2E)
val SurfaceVariantDark = Color(0xFF252528)

// Semantic Colors with Improved Contrast
object SemanticColors {
    // Success states
    val Success = BrandSuccess
    val SuccessContainer = BrandSuccess.copy(alpha = 0.12f)
    val OnSuccess = Color.White
    
    // Warning states
    val Warning = BrandWarning
    val WarningContainer = BrandWarning.copy(alpha = 0.12f)
    val OnWarning = Color(0xFF1A1A1A)
    
    // Error states
    val Error = BrandError
    val ErrorContainer = BrandError.copy(alpha = 0.12f)
    val OnError = Color.White
    
    // Info states
    val Info = BrandPrimary
    val InfoContainer = BrandPrimary.copy(alpha = 0.12f)
    val OnInfo = Color.White
}

// Glassmorphism support
object GlassColors {
    val GlassLight = Color.White.copy(alpha = 0.7f)
    val GlassDark = Color(0xFF1E1E1E).copy(alpha = 0.8f)
    val GlassBorder = Color.White.copy(alpha = 0.18f)
}

