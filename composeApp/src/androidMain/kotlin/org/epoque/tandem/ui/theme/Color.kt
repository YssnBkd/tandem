package org.epoque.tandem.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Colors - Warm Terracotta palette (from Goals mockup)
val TandemPrimary = Color(0xFFD97757)             // Warm terracotta
val TandemOnPrimary = Color(0xFFFFFFFF)
val TandemPrimaryContainer = Color(0xFFFDF2EF)   // Soft coral background
val TandemOnPrimaryContainer = Color(0xFF4A4238) // Warm charcoal

val TandemSecondary = Color(0xFF9C9488)          // Warm muted
val TandemOnSecondary = Color(0xFFFFFFFF)
val TandemSecondaryContainer = Color(0xFFF0EBE6) // Light warm gray
val TandemOnSecondaryContainer = Color(0xFF4A4238)

val TandemTertiary = Color(0xFFE07A5F)           // Coral accent
val TandemOnTertiary = Color(0xFFFFFFFF)
val TandemTertiaryContainer = Color(0xFFFFE5DE)  // Light coral
val TandemOnTertiaryContainer = Color(0xFF4A4238)

val TandemError = Color(0xFFD1453B)              // Consistent with OverdueRed
val TandemOnError = Color(0xFFFFFFFF)
val TandemErrorContainer = Color(0xFFF9DEDC)
val TandemOnErrorContainer = Color(0xFF410E0B)

// Light Theme Colors - Warm palette
val TandemBackgroundLight = Color(0xFFFFFBF7)    // Soft eggshell
val TandemOnBackgroundLight = Color(0xFF4A4238)  // Warm charcoal
val TandemSurfaceLight = Color(0xFFFFFFFF)       // Pure white for cards
val TandemOnSurfaceLight = Color(0xFF4A4238)     // Warm charcoal
val TandemSurfaceVariantLight = Color(0xFFF0EBE6) // Light warm gray
val TandemOnSurfaceVariantLight = Color(0xFF9C9488) // Warm muted
val TandemOutlineLight = Color(0xFFE0DCD6)       // Warm outline
val TandemOutlineVariantLight = Color(0xFFF0EBE6) // Light warm gray

// Dark Theme Colors - Warm palette
val TandemPrimaryDark = Color(0xFFE9A68E)         // Lighter terracotta
val TandemOnPrimaryDark = Color(0xFF3D2016)
val TandemPrimaryContainerDark = Color(0xFF5C3A2A)
val TandemOnPrimaryContainerDark = Color(0xFFFDF2EF)

val TandemSecondaryDark = Color(0xFFD4CEC7)       // Light warm muted
val TandemOnSecondaryDark = Color(0xFF3A3630)
val TandemSecondaryContainerDark = Color(0xFF514B44)
val TandemOnSecondaryContainerDark = Color(0xFFF0EBE6)

val TandemTertiaryDark = Color(0xFFFFB4A1)        // Light coral
val TandemOnTertiaryDark = Color(0xFF5C2418)
val TandemTertiaryContainerDark = Color(0xFF7A392A)
val TandemOnTertiaryContainerDark = Color(0xFFFFE5DE)

val TandemErrorDark = Color(0xFFF2B8B5)
val TandemOnErrorDark = Color(0xFF601410)
val TandemErrorContainerDark = Color(0xFF8C1D18)
val TandemOnErrorContainerDark = Color(0xFFF9DEDC)

val TandemBackgroundDark = Color(0xFF1A1816)      // Warm dark
val TandemOnBackgroundDark = Color(0xFFE8E2DC)    // Warm light
val TandemSurfaceDark = Color(0xFF1A1816)
val TandemOnSurfaceDark = Color(0xFFE8E2DC)
val TandemSurfaceVariantDark = Color(0xFF4A4238)  // Warm charcoal
val TandemOnSurfaceVariantDark = Color(0xFFD4CEC7)
val TandemOutlineDark = Color(0xFF9C9488)         // Warm muted
val TandemOutlineVariantDark = Color(0xFF4A4238)

// ═══════════════════════════════════════════════════════════════════════════
// UI REDESIGN COLORS (Feature 009)
// ═══════════════════════════════════════════════════════════════════════════

// Priority Colors (Todoist-style)
val PriorityP1 = Color(0xFFD1453B)       // Red - Highest priority
val PriorityP2 = Color(0xFFEB8909)       // Orange - High priority
val PriorityP3 = Color(0xFF246FE0)       // Blue - Medium priority
val PriorityP4 = Color(0xFF79747E)       // Gray - Low/No priority (default)

// Priority Light Backgrounds (10% opacity)
val PriorityP1Light = Color(0x1AD1453B)  // 10% red
val PriorityP2Light = Color(0x1AEB8909)  // 10% orange
val PriorityP3Light = Color(0x1A246FE0)  // 10% blue

// Coral accent colors (FAB, AI button, borders)
val CoralPrimary = Color(0xFFE07A5F)     // Main coral
val CoralDark = Color(0xFFDC4C3E)        // Darker coral
val CoralLight = Color(0xFFFFE5DE)       // Light coral background

// Schedule green (Todoist style)
val ScheduleGreen = Color(0xFF058527)    // Green for schedule indicators

// Streak/progress gradient colors
val StreakStart = Color(0xFFE07A5F)      // Gradient start
val StreakEnd = Color(0xFFF4A261)        // Gradient end

// Section colors
val OverdueRed = Color(0xFFD1453B)       // Overdue section
val TodayBlue = Color(0xFF246FE0)        // Today indicator

// Owner type colors
val OwnerSelfColor = Color(0xFF6750A4)   // Purple for "Me"
val OwnerPartnerColor = Color(0xFFE07A5F) // Coral for Partner
val OwnerTogetherColor = Color(0xFF246FE0) // Blue for Together

// Goals screen colors
val GoalPrimary = Color(0xFFD97757)            // Warm terracotta (from mockup)
val GoalIconBackground = Color(0xFFFDF2EF)    // Soft coral for goal icon backgrounds
val GoalProgressBackground = Color(0xFFF0EBE6) // Light warm gray for progress bar track
val GoalTextMuted = Color(0xFF9C9488)          // Warm muted text
val GoalTextMain = Color(0xFF4A4238)           // Warm charcoal for main text
val GoalDashedBorder = Color(0xFFE0DCD6)       // Dashed border for add button
val GoalCardShadow = Color(0x0D4A4238)         // 5% warm shadow
val GoalCardBorder = Color(0x05000000)         // 2% black border
