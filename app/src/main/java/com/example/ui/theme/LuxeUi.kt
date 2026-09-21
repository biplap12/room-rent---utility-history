package com.example.ui.theme
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardActions

/* ═══════════════════════════════════════════════════════════════════
   1. TOKENS
   ═══════════════════════════════════════════════════════════════════ */

object LuxeTokens {
    val CardRadius: Dp = 18.dp
    val DialogRadius: Dp = 24.dp
    val InnerRadius: Dp = 12.dp
    val FieldRadius: Dp = 14.dp
    val CardElevation: Dp = 3.dp
    val HeroElevation: Dp = 8.dp
}

/* ═══════════════════════════════════════════════════════════════════
   SOLID COLOR HELPERS
   -------------------------------------------------------------------
   Because we refuse transparency, every accent tint is blended
   against a solid surface color and returned as an opaque color.
   ═══════════════════════════════════════════════════════════════════ */

/** Blend [accent] with [surface] by [amount] (0..1) → solid result. */
private fun tintOn(accent: Color, surface: Color, amount: Float): Color =
    lerp(surface, accent, amount.coerceIn(0f, 1f))

/** The app's current solid surface color. */
@Composable
private fun luxeSurface(): Color = MaterialTheme.colorScheme.surface

/** The app's current solid outline color (already opaque). */
@Composable
private fun luxeOutline(): Color = MaterialTheme.colorScheme.outline

/* ═══════════════════════════════════════════════════════════════════
   2. CARDS
   ═══════════════════════════════════════════════════════════════════ */

/**
 * Gradient-edged card with a soft SOLID accent wash and an ambient
 * accent-tinted shadow. No transparency anywhere.
 */
@Composable
fun BeautifulCard(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    shape: RoundedCornerShape = RoundedCornerShape(LuxeTokens.CardRadius),
    elevation: Dp = LuxeTokens.CardElevation,
    content: @Composable () -> Unit
) {
    val a = accent ?: MaterialTheme.colorScheme.primary
    val surface = luxeSurface()
    val outline = luxeOutline()

    // Solid gradient stops blended against the surface.
    val topSolid    = tintOn(a, surface, 0.05f)
    val middleSolid = tintOn(a, surface, 0.02f)
    val bottomSolid = surface

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = tintOn(a, surface, 0.15f),
                spotColor    = tintOn(a, surface, 0.15f)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(topSolid, middleSolid, bottomSolid)
                )
            )
            .border(
                width = 1.dp,
                color = tintOn(outline, surface, 0.35f),
                shape = shape
            )
    ) {
        content()
    }
}

/**
 * Empty-state placeholder card — icon-in-circle + message.
 */
@Composable
fun EmptyStateCard(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    val surface = luxeSurface()
    val outline = luxeOutline()

    BeautifulCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(tintOn(outline, surface, 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    tint = outline,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════
   3. ICON PLATES
   ═══════════════════════════════════════════════════════════════════ */

/**
 * Rounded icon plate — SOLID gradient, hairline ring.
 */
@Composable
fun GradientIconBadge(
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 18.dp,
    corner: Dp = 11.dp,
    topAmount: Float = 0.22f,
    bottomAmount: Float = 0.06f,
    ringAmount: Float = 0.20f
) {
    val surface = luxeSurface()
    val shape = RoundedCornerShape(corner)

    val topSolid    = tintOn(accent, surface, topAmount)
    val bottomSolid = tintOn(accent, surface, bottomAmount)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(topSolid, bottomSolid)
                )
            )
            .border(1.dp, tintOn(accent, surface, ringAmount), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(iconSize))
    }
}

/**
 * Larger, animated icon tile — the "hero" icon used on room cards
 * and pickers. Solid fills only.
 */
@Composable
fun LeadingIconTile(
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    size: Dp = 46.dp,
    iconSize: Dp = 22.dp,
    corner: Dp = 14.dp
) {
    val isDark = isSystemInDarkTheme()
    val surface = luxeSurface()
    val themedAccent = if (isDark) lerp(accent, Color.White, 0.18f) else accent

    val topAmount by animateFloatAsState(
        targetValue = when {
            isActive -> 0.34f
            isDark   -> 0.24f
            else     -> 0.20f
        },
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "tileTop"
    )
    val bottomAmount by animateFloatAsState(
        targetValue = when {
            isActive -> 0.14f
            isDark   -> 0.09f
            else     -> 0.06f
        },
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "tileBottom"
    )
    val ringAmount by animateFloatAsState(
        targetValue = when {
            isActive -> 0.45f
            isDark   -> 0.22f
            else     -> 0.16f
        },
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "tileRing"
    )
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.06f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tileScale"
    )

    val shape = RoundedCornerShape(corner)
    // In dark mode we lift the plate slightly using a solid color blend,
    // not a translucent white.
    val plateColor = if (isDark) tintOn(Color.White, surface, 0.05f) else surface

    val topSolid    = tintOn(themedAccent, plateColor, topAmount)
    val bottomSolid = tintOn(themedAccent, plateColor, bottomAmount)

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = if (isActive) 4.dp else 0.dp,
                shape = shape,
                ambientColor = tintOn(themedAccent, surface, 0.35f),
                spotColor    = tintOn(themedAccent, surface, 0.35f)
            )
            .clip(shape)
            .background(plateColor)
            .background(
                Brush.linearGradient(
                    colors = listOf(topSolid, bottomSolid)
                )
            )
            .border(1.dp, tintOn(themedAccent, plateColor, ringAmount), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = themedAccent,
            modifier = Modifier.size(iconSize)
        )
    }
}

/* ═══════════════════════════════════════════════════════════════════
   4. PROGRESS & LEGENDS
   ═══════════════════════════════════════════════════════════════════ */

/**
 * Themed horizontal progress bar with a SOLID gradient fill.
 */
@Composable
fun AccentProgressBar(
    fraction: Float,
    accent: Color,
    modifier: Modifier = Modifier,
    height: Dp = 5.dp,
    trackColor: Color? = null
) {
    val surface = luxeSurface()
    val outline = luxeOutline()
    val track = trackColor ?: tintOn(outline, surface, 0.15f)

    val shape = RoundedCornerShape(height / 2)
    val fillStart = tintOn(accent, surface, 0.75f)
    val fillEnd   = accent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .clip(shape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(fillStart, fillEnd)
                    )
                )
        )
    }
}

/**
 * Small swatch + label used in chart legends.
 */
@Composable
fun LegendChip(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Vertical accent bar + label + value.
 */
@Composable
fun LegendBar(label: String, value: String, color: Color) {
    val surface = luxeSurface()
    val topSolid    = tintOn(color, surface, 0.7f)
    val bottomSolid = color

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(topSolid, bottomSolid)
                        )
                    )
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/* ═══════════════════════════════════════════════════════════════════
   5. TILES & METRICS
   ═══════════════════════════════════════════════════════════════════ */

@Composable
fun StatTile(
    label: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val surface = luxeSurface()
    val outline = luxeOutline()

    val height = if (compact) 108.dp else 116.dp
    val iconPlateSize = if (compact) 34.dp else 38.dp
    val valueStyle = if (compact)
        MaterialTheme.typography.titleMedium
    else
        MaterialTheme.typography.headlineSmall

    val borderColor = if (selected) tintOn(accent, surface, 0.7f)
    else tintOn(outline, surface, 0.35f)

    val bgAmount = if (selected) 0.14f else 0.04f
    val bgSolid  = tintOn(accent, surface, bgAmount)
    val bgSolid2 = surface

    val elevation by animateFloatAsState(
        targetValue = if (selected) 8f else 0f,
        animationSpec = tween(250),
        label = "statTileElev"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "statTileScale"
    )

    val clickableMod = if (onClick != null) Modifier.clickable { onClick() } else Modifier

    // Radial plate — solid start / end, no alpha.
    val plateInner = tintOn(accent, surface, 0.30f)
    val plateOuter = tintOn(accent, surface, 0.08f)

    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(LuxeTokens.CardRadius),
                ambientColor = tintOn(accent, surface, 0.30f),
                spotColor    = tintOn(accent, surface, 0.30f)
            )
            .clip(RoundedCornerShape(LuxeTokens.CardRadius))
            .background(surface)
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgSolid, bgSolid2)
                )
            )
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(LuxeTokens.CardRadius)
            )
            .then(clickableMod)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(iconPlateSize)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(plateInner, plateOuter)
                        )
                    )
                    .border(1.dp, tintOn(accent, surface, 0.20f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                style = valueStyle,
                fontWeight = FontWeight.Bold,
                color = if (selected) accent else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.9.sp,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MetricColumn(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            fontSize = 9.sp,
            letterSpacing = 0.8.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
fun StatusPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val surface = luxeSurface()
    val bg = tintOn(color, surface, 0.14f)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(5.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════
   6. FORM PRIMITIVES
   ═══════════════════════════════════════════════════════════════════ */

@Composable
fun LuxeSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    caption: String? = null
) {
    val surface = luxeSurface()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(accent, tintOn(accent, surface, 0.55f))
                        )
                    )
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accent,
                letterSpacing = 1.2.sp,
                fontSize = 11.sp
            )
        }
        if (caption != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LuxeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val surface = luxeSurface()
    val primary = MaterialTheme.colorScheme.primary
    val outline = luxeOutline()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let {
            { Icon(it, null, tint = primary, modifier = Modifier.size(20.dp)) }
        },
        trailingIcon = trailingIcon,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(LuxeTokens.FieldRadius),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor     = primary,
            unfocusedBorderColor   = tintOn(outline, surface, 0.5f),
            focusedContainerColor  = tintOn(primary, surface, 0.04f),
            unfocusedContainerColor = surface,
            focusedLabelColor      = primary,
            cursorColor            = primary
        ),
        visualTransformation = visualTransformation,
        keyboardActions = keyboardActions,
        modifier = modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> LuxeDropdown(
    label: String,
    selected: T?,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null
) {
    var expanded by remember { mutableStateOf(false) }

    val surface = luxeSurface()
    val primary = MaterialTheme.colorScheme.primary
    val outline = luxeOutline()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected?.let(optionLabel).orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let {
                { Icon(it, null, tint = primary, modifier = Modifier.size(20.dp)) }
            },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, null, tint = primary)
            },
            shape = RoundedCornerShape(LuxeTokens.FieldRadius),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = primary,
                unfocusedBorderColor    = tintOn(outline, surface, 0.5f),
                focusedContainerColor   = tintOn(primary, surface, 0.04f),
                unfocusedContainerColor = surface
            ),
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(LuxeTokens.FieldRadius),
            containerColor = surface
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = { onSelect(option); expanded = false },
                    trailingIcon = if (option == selected) {
                        { Icon(Icons.Default.Check, null, tint = primary) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun LuxePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    val surface = luxeSurface()
    val shape = RoundedCornerShape(LuxeTokens.FieldRadius)

    val fillStart = accent
    val fillEnd   = tintOn(accent, surface, 0.82f)
    val disabledStart = tintOn(MaterialTheme.colorScheme.outline, surface, 0.40f)
    val disabledEnd   = disabledStart

    Box(
        modifier = modifier
            .height(52.dp)
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = shape,
                ambientColor = tintOn(accent, surface, 0.35f),
                spotColor    = tintOn(accent, surface, 0.40f)
            )
            .clip(shape)
            .background(
                if (enabled)
                    Brush.horizontalGradient(colors = listOf(fillStart, fillEnd))
                else
                    Brush.horizontalGradient(colors = listOf(disabledStart, disabledEnd))
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LuxeSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = MaterialTheme.colorScheme.primary
) {
    val surface = luxeSurface()
    val bg = tintOn(accent, surface, 0.08f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(LuxeTokens.FieldRadius),
        color = bg,
        border = BorderStroke(1.dp, tintOn(accent, surface, 0.35f)),
        modifier = modifier.height(52.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text,
                color = accent,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════════
   7. LIST PRIMITIVES
   ═══════════════════════════════════════════════════════════════════ */

@Composable
fun LuxeListRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val clickMod = if (onClick != null)
        Modifier
            .clip(RoundedCornerShape(LuxeTokens.InnerRadius))
            .clickable { onClick() }
    else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickMod)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GradientIconBadge(
            icon = icon,
            accent = accent,
            size = 38.dp,
            iconSize = 18.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            trailing()
        }
        if (showChevron) {
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Default.ChevronRight, null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun LuxeListGroup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BeautifulCard(modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp)) { content() }
    }
}

@Composable
fun LuxeDivider(modifier: Modifier = Modifier) {
    val surface = luxeSurface()
    val outline = luxeOutline()

    HorizontalDivider(
        modifier = modifier,
        color = tintOn(outline, surface, 0.15f)
    )
}

@Composable
fun LuxeToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LuxeTokens.InnerRadius))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GradientIconBadge(icon, accent, size = 38.dp, iconSize = 18.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accent
            )
        )
    }
}

/* ═══════════════════════════════════════════════════════════════════
   8. SCREEN CHROME
   ═══════════════════════════════════════════════════════════════════ */

@Composable
fun LuxeScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val surface = luxeSurface()
    val primary = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = tintOn(primary, surface, 0.10f),
                border = BorderStroke(1.dp, tintOn(primary, surface, 0.25f)),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.ArrowBack, null,
                        tint = primary,
                        modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing?.invoke()
    }
}

/* ═══════════════════════════════════════════════════════════════════
   9. CHOICE ROWS
   ═══════════════════════════════════════════════════════════════════ */

@Composable
fun ExportOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    baseColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surface = luxeSurface()

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = tintOn(baseColor, surface, 0.08f),
        border = BorderStroke(1.dp, tintOn(baseColor, surface, 0.25f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(baseColor, tintOn(baseColor, surface, 0.75f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = baseColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight, null,
                tint = baseColor
            )
        }
    }
}