package com.zerosignal.cognitedge.ui.theme

import androidx.compose.ui.graphics.Color

// ── Obsidian Clarity Palette ────────────────────────────────
// Inspired by Linear, Arc, Vercel — refined monochrome dark.
// One restrained accent. Surgical precision.

// Surfaces — barely-there depth hierarchy
val Obsidian        = Color(0xFF09090B)   // OLED-true black base
val Surface1        = Color(0xFF111113)   // Primary surface
val Surface2        = Color(0xFF18181B)   // Cards, containers
val Surface3        = Color(0xFF1E1E23)   // Elevated chips, badges
val Surface4        = Color(0xFF27272A)   // Pressed states, dividers

// Borders — ghost-thin, barely visible
val BorderSubtle    = Color(0xFF27272A)   // Default borders
val BorderMuted     = Color(0xFF1F1F23)   // Even quieter borders

// Text — warm whites with deliberate hierarchy
val TextPrimary     = Color(0xFFF4F4F5)   // Headlines, primary content
val TextSecondary   = Color(0xFFA1A1AA)   // Body, descriptions
val TextTertiary    = Color(0xFF71717A)   // Hints, placeholders
val TextQuaternary  = Color(0xFF52525B)   // Disabled, watermarks

// Accent — restrained indigo, used sparingly
val Accent          = Color(0xFF818CF8)   // Primary interactive (indigo-400)
val AccentMuted     = Color(0xFF6366F1)   // Hover/focus variant (indigo-500)
val AccentSubtle    = Color(0xFF312E81)   // Background tint (indigo-900)

// Semantic — softer, not alarming
val Success         = Color(0xFF34D399)   // Emerald-400
val SuccessSubtle   = Color(0xFF064E3B)   // Emerald-900 for bg
val Warning         = Color(0xFFFBBF24)   // Amber-400
val WarningSubtle   = Color(0xFF78350F)   // Amber-900 for bg
val Error           = Color(0xFFF87171)   // Red-400
val ErrorSubtle     = Color(0xFF7F1D1D)   // Red-900 for bg

// Chat — subtle differentiation
val UserBubble      = Color(0xFF1A1A2E)   // Slightly warm dark
val AssistantBubble = Color(0xFF111113)   // Matches surface

// Legacy compatibility aliases (used across codebase)
val NeonCyan            = Accent
val ElectricPurple      = AccentMuted
val DeepPurple          = AccentSubtle
val DarkPurple          = AccentSubtle
val SurfaceDark         = Obsidian
val SurfaceVariantDark  = Surface1
val SurfaceCard         = Surface2
val SurfaceElevated     = Surface3
val OnSurfaceDark       = TextPrimary
val OnSurfaceSecondary  = TextSecondary
val AccentPurple        = Accent
val Purple80            = Accent
val Purple40            = AccentMuted
val PurpleDark          = AccentSubtle
val GradientStart       = Accent
val GradientEnd         = Accent
val DownloadingAmber    = Warning
val DownloadedGreen     = Success
val ErrorRed            = Error
val NavBarSurface       = Color(0xFF0C0C0E)
val NavBarIndicator     = Color(0xFF18181B)
