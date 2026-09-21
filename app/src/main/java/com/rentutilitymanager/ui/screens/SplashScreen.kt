package com.rentutilitymanager.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rentutilitymanager.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val currentOnFinished by rememberUpdatedState(onFinished)

    // ============================================================
    // COLOR SYSTEM
    // ============================================================

    val black = Color(0xFF030610)
    val navy = Color(0xFF07122A)
    val blue = Color(0xFF102B63)

    val electricBlue = Color(0xFF4DA3FF)
    val cyan = Color(0xFF48E5FF)
    val violet = Color(0xFF8B7CFF)
    val pink = Color(0xFFEA7CFF)

    val white = Color.White
    val softWhite = white.copy(alpha = 0.75f)
    val mutedWhite = white.copy(alpha = 0.42f)

    // ============================================================
    // EASING
    // ============================================================

    val enterEase = CubicBezierEasing(
        0.22f,
        1f,
        0.36f,
        1f
    )

    // ============================================================
    // ENTRANCE ANIMATIONS
    // ============================================================

    val buildingScale = remember {
        Animatable(0.55f)
    }

    val buildingAlpha = remember {
        Animatable(0f)
    }

    val titleAlpha = remember {
        Animatable(0f)
    }

    val subtitleAlpha = remember {
        Animatable(0f)
    }

    val hudAlpha = remember {
        Animatable(0f)
    }

    val footerAlpha = remember {
        Animatable(0f)
    }

    val progress = remember {
        Animatable(0f)
    }

    // ============================================================
    // INFINITE ANIMATIONS
    // ============================================================

    val infinite = rememberInfiniteTransition(
        label = "creativeSplash"
    )

    // Main building breathing
    val buildingPulse by infinite.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                1900,
                easing = enterEase
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buildingPulse"
    )

    // Ambient glow
    val glowPulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                2500,
                easing = enterEase
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    // Orbit
    val orbitRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                7000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitRotation"
    )

    // Radar
    val radarRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                3600,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRotation"
    )

    // Background grid movement
    val gridOffset by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                2600,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridOffset"
    )

    // Floating particle movement
    val particleRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                50000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleRotation"
    )

    // Shimmer
    val shimmer by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                2200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    // ============================================================
    // STATUS
    // ============================================================

    var status by remember {
        mutableStateOf("INITIALIZING SYSTEM")
    }

    var buildingProgress by remember {
        mutableFloatStateOf(0f)
    }

    // ============================================================
    // ENTRY SEQUENCE
    // ============================================================

    LaunchedEffect(Unit) {

        launch {
            buildingScale.animateTo(
                1f,
                tween(
                    850,
                    easing = enterEase
                )
            )
        }

        launch {
            buildingAlpha.animateTo(
                1f,
                tween(550)
            )
        }

        launch {
            delay(350.milliseconds)

            titleAlpha.animateTo(
                1f,
                tween(
                    450,
                    easing = enterEase
                )
            )
        }

        launch {
            delay(500.milliseconds)

            subtitleAlpha.animateTo(
                1f,
                tween(
                    450,
                    easing = enterEase
                )
            )
        }

        launch {
            delay(650.milliseconds)

            hudAlpha.animateTo(
                1f,
                tween(
                    500,
                    easing = enterEase
                )
            )
        }

        launch {
            delay(700.milliseconds)

            footerAlpha.animateTo(
                1f,
                tween(
                    500,
                    easing = enterEase
                )
            )
        }
    }

    // ============================================================
    // LOADING
    // ============================================================

    LaunchedEffect(Unit) {

        delay(600.milliseconds)

        status = "CONNECTING PROPERTY CORE"

        progress.animateTo(
            0.18f,
            tween(
                350,
                easing = enterEase
            )
        )

        buildingProgress = 0.22f

        status = "LOADING ROOM DATABASE"

        progress.animateTo(
            0.38f,
            tween(
                400,
                easing = enterEase
            )
        )

        buildingProgress = 0.40f

        status = "INITIALIZING RENT ENGINE"

        progress.animateTo(
            0.55f,
            tween(
                400,
                easing = enterEase
            )
        )

        buildingProgress = 0.56f

        status = "SYNCING UTILITY METERS"

        progress.animateTo(
            0.73f,
            tween(
                400,
                easing = enterEase
            )
        )

        buildingProgress = 0.75f

        status = "PREPARING DASHBOARD"

        progress.animateTo(
            0.90f,
            tween(
                380,
                easing = enterEase
            )
        )

        buildingProgress = 0.92f

        status = "SYSTEM READY"

        progress.animateTo(
            1f,
            tween(
                450,
                easing = enterEase
            )
        )

        buildingProgress = 1f
    }

    // ============================================================
    // FINISH
    // ============================================================

    LaunchedEffect(Unit) {
        delay(3100.milliseconds)
        currentOnFinished()
    }

    // ============================================================
    // ROOT
    // ============================================================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        black,
                        navy,
                        blue,
                        black
                    )
                )
            )
    ) {

        // ========================================================
        // BACKGROUND GLOW
        // ========================================================

        Box(
            modifier = Modifier
                .size(450.dp)
                .scale(glowPulse)
                .offset(
                    x = (-150).dp,
                    y = (-130).dp
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            electricBlue.copy(
                                alpha = 0.20f
                            ),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(460.dp)
                .scale(glowPulse)
                .align(Alignment.BottomEnd)
                .offset(
                    x = 130.dp,
                    y = 140.dp
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            violet.copy(
                                alpha = 0.18f
                            ),
                            Color.Transparent
                        )
                    )
                )
        )

        // ========================================================
        // FUTURISTIC GRID
        // ========================================================

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.13f)
        ) {

            val spacing = 42.dp.toPx()

            var x = -spacing + gridOffset

            while (x < size.width + spacing) {

                drawLine(
                    color = cyan.copy(alpha = 0.16f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )

                x += spacing
            }

            var y = -spacing + gridOffset

            while (y < size.height + spacing) {

                drawLine(
                    color = cyan.copy(alpha = 0.12f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )

                y += spacing
            }
        }

        // ========================================================
        // FLOATING PARTICLES
        // ========================================================

        CreativeParticleField(
            rotation = particleRotation,
            color = white
        )

        // ========================================================
        // MAIN CONTENT
        // ========================================================

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 22.dp,
                    end = 22.dp,
                    top = 48.dp,
                    bottom = 110.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ====================================================
            // MINI SYSTEM LABEL
            // ====================================================

            Row(
                modifier = Modifier
                    .alpha(hudAlpha.value)
                    .clip(
                        RoundedCornerShape(50)
                    )
                    .background(
                        Color.White.copy(
                            alpha = 0.045f
                        )
                    )
                    .border(
                        1.dp,
                        cyan.copy(alpha = 0.20f),
                        RoundedCornerShape(50)
                    )
                    .padding(
                        horizontal = 11.dp,
                        vertical = 5.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(cyan)
                )

                Text(
                    text = "PROPERTY CONTROL SYSTEM",
                    color = softWhite,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.3.sp
                )
            }

            Spacer(
                Modifier.height(18.dp)
            )

            // ====================================================
            // BUILDING CORE
            // ====================================================

            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(buildingScale.value),
                contentAlignment = Alignment.Center
            ) {

                // Radar circles
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = radarRotation
                        }
                        .alpha(buildingAlpha.value)
                ) {

                    val center = Offset(
                        size.width / 2f,
                        size.height / 2f
                    )

                    listOf(
                        60f,
                        82f,
                        105f
                    ).forEachIndexed { index, radius ->

                        drawCircle(
                            color =
                                when (index) {
                                    0 -> cyan.copy(
                                        alpha = 0.14f
                                    )

                                    1 -> electricBlue.copy(
                                        alpha = 0.09f
                                    )

                                    else -> violet.copy(
                                        alpha = 0.06f
                                    )
                                },
                            radius = radius.dp.toPx(),
                            center = center,
                            style = Stroke(
                                width = 1.dp.toPx()
                            )
                        )
                    }

                    // Radar sweep
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                cyan.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 85f,
                        useCenter = true,
                        style = Stroke(
                            width = 1.dp.toPx()
                        )
                    )
                }

                // Outer glow
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(glowPulse)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    cyan.copy(
                                        alpha = 0.14f
                                    ),
                                    electricBlue.copy(
                                        alpha = 0.07f
                                    ),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Glass building card
                Box(
                    modifier = Modifier
                        .size(138.dp)
                        .scale(buildingPulse)
                        .clip(
                            RoundedCornerShape(40.dp)
                        )
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(
                                        alpha = 0.16f
                                    ),
                                    Color.White.copy(
                                        alpha = 0.035f
                                    )
                                )
                            )
                        )
                        .border(
                            width = 1.4.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    cyan.copy(
                                        alpha = 0.65f
                                    ),
                                    Color.White.copy(
                                        alpha = 0.08f
                                    ),
                                    violet.copy(
                                        alpha = 0.35f
                                    )
                                )
                            ),
                            shape =
                                RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    // Shimmer layer
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX =
                                    shimmer * 130f
                            }
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(
                                            alpha = 0.10f
                                        ),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Apartment
                    FuturisticApartmentLogo(
                        modifier = Modifier.size(92.dp),
                        progress = buildingProgress,
                        white = white,
                        cyan = cyan,
                        darkWindow = Color(
                            0xFF08152D
                        )
                    )
                }

                // =================================================
                // ORBIT NODES
                // =================================================

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ =
                                orbitRotation
                        }
                ) {

                    val center =
                        Offset(
                            size.width / 2f,
                            size.height / 2f
                        )

                    val radius =
                        101.dp.toPx()

                    val points =
                        listOf(
                            0.0,
                            2.1,
                            4.2
                        )

                    points.forEachIndexed { index, value ->

                        val point = Offset(
                            center.x +
                                    radius *
                                    cos(value).toFloat(),

                            center.y +
                                    radius *
                                    sin(value).toFloat()
                        )

                        val color =
                            when (index) {
                                0 -> cyan
                                1 -> electricBlue
                                else -> pink
                            }

                        drawCircle(
                            color = color.copy(
                                alpha = 0.25f
                            ),
                            radius =
                                8.dp.toPx(),
                            center = point
                        )

                        drawCircle(
                            color = color,
                            radius =
                                2.8.dp.toPx(),
                            center = point
                        )
                    }

                    // Orbit path
                    drawCircle(
                        color = white.copy(
                            alpha = 0.10f
                        ),
                        radius = radius,
                        center = center,
                        style = Stroke(
                            width =
                                1.dp.toPx()
                        )
                    )
                }
            }

            Spacer(
                Modifier.height(15.dp)
            )

            // ====================================================
            // APP NAME
            // ====================================================

            Text(
                text = "Room Rent & Utility",
                color = white,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.6).sp,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    shadow = Shadow(
                        color = cyan.copy(alpha = 0.12f),
                        offset = Offset(0f, 3f),
                        blurRadius = 18f
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(titleAlpha.value)
            )

            Spacer(
                Modifier.height(6.dp)
            )

            Text(
                text = "Your home. Your meters. Your records.",
                color = softWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.7.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(
                        subtitleAlpha.value
                    )
            )

            Spacer(
                Modifier.height(25.dp)
            )

            // ====================================================
            // UTILITY CONTROL CARDS
            // ====================================================

            Row(
                modifier = Modifier
                    .alpha(hudAlpha.value),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                UtilityCard(
                    title = "RENT",
                    symbol = "₹",
                    value = progress.value,
                    color = electricBlue
                )

                UtilityCard(
                    title = "WATER",
                    symbol = "W",
                    value = progress.value * 0.92f,
                    color = cyan
                )

                UtilityCard(
                    title = "POWER",
                    symbol = "⚡",
                    value = progress.value * 0.84f,
                    color = violet
                )
            }

            Spacer(
                Modifier.height(20.dp)
            )

            // ====================================================
            // SYSTEM STATUS
            // ====================================================

            Row(
                modifier = Modifier
                    .alpha(hudAlpha.value)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = status,
                    color = mutedWhite,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "${(progress.value * 100).toInt()}%",
                    color = cyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                Modifier.height(7.dp)
            )

            // ====================================================
            // DIGITAL PROGRESS BAR
            // ====================================================

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(
                            alpha = 0.08f
                        )
                    )
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            progress.value
                        )
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    cyan,
                                    electricBlue,
                                    violet
                                )
                            )
                        )
                )
            }
        }

        // ========================================================
        // FOOTER
        // ========================================================

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    bottom = 16.dp
                )
                .alpha(
                    footerAlpha.value
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(7.dp)
            ) {

                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(1.dp)
                        .background(
                            cyan.copy(
                                alpha = 0.28f
                            )
                        )
                )

                Image(
                    painter = painterResource(
                        R.drawable.nepal_flag
                    ),
                    contentDescription =
                        "Nepal flag",
                    modifier = Modifier
                        .size(
                            width = 32.dp,
                            height = 40.dp
                        )
                )

                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(1.dp)
                        .background(
                            cyan.copy(
                                alpha = 0.28f
                            )
                        )
                )
            }

            Spacer(
                Modifier.height(5.dp)
            )

            Text(
                text = "MADE IN NEPAL",
                color = white,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.8.sp
            )

            Text(
                text = stringResource(R.string.biplap_neupane_v1_0),
                color = mutedWhite,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.7.sp
            )
        }
    }
}

/* ================================================================
   UTILITY CARD
   ================================================================ */

@Composable
private fun UtilityCard(
    title: String,
    symbol: String,
    value: Float,
    color: Color
) {
    Box(
        modifier = Modifier
            .width(88.dp)
            .height(54.dp)
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(
                Color.White.copy(
                    alpha = 0.045f
                )
            )
            .border(
                width = 1.dp,
                color = color.copy(
                    alpha = 0.20f
                ),
                shape =
                    RoundedCornerShape(16.dp)
            )
            .padding(
                horizontal = 8.dp,
                vertical = 7.dp
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            color.copy(
                                alpha = 0.13f
                            )
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = symbol,
                        color = color,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    Modifier.width(5.dp)
                )

                Text(
                    text = title,
                    color = Color.White.copy(
                        alpha = 0.65f
                    ),
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.7.sp
                )
            }

            Spacer(
                Modifier.height(7.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(
                            alpha = 0.08f
                        )
                    )
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            value.coerceIn(
                                0f,
                                1f
                            )
                        )
                        .fillMaxHeight()
                        .background(
                            color
                        )
                )
            }
        }
    }
}

/* ================================================================
   FUTURISTIC APARTMENT LOGO
   ================================================================ */

@Composable
private fun FuturisticApartmentLogo(
    modifier: Modifier,
    progress: Float,
    white: Color,
    cyan: Color,
    darkWindow: Color
) {

    Canvas(
        modifier = modifier
    ) {

        val w = size.width
        val h = size.height

        val buildingColor =
            Brush.linearGradient(
                colors = listOf(
                    white,
                    cyan.copy(
                        alpha = 0.86f
                    )
                )
            )

        // --------------------------------------------------------
        // Main tower
        // --------------------------------------------------------

        val mainLeft = w * 0.30f
        val mainRight = w * 0.72f
        val mainTop = h * 0.14f
        val mainBottom = h * 0.90f

        drawRoundRect(
            brush = buildingColor,
            topLeft = Offset(
                mainLeft,
                mainTop
            ),
            size = Size(
                mainRight - mainLeft,
                mainBottom - mainTop
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                w * 0.06f
            )
        )

        // --------------------------------------------------------
        // Left wing
        // --------------------------------------------------------

        drawRoundRect(
            color = white.copy(
                alpha = 0.72f
            ),
            topLeft = Offset(
                w * 0.08f,
                h * 0.40f
            ),
            size = Size(
                w * 0.22f,
                h * 0.50f
            ),
            cornerRadius =
                androidx.compose.ui.geometry.CornerRadius(
                    w * 0.06f
                )
        )

        // --------------------------------------------------------
        // Right wing
        // --------------------------------------------------------

        drawRoundRect(
            color = white.copy(
                alpha = 0.72f
            ),
            topLeft = Offset(
                w * 0.72f,
                h * 0.31f
            ),
            size = Size(
                w * 0.22f,
                h * 0.59f
            ),
            cornerRadius =
                androidx.compose.ui.geometry.CornerRadius(
                    w * 0.06f
                )
        )

        // --------------------------------------------------------
        // Windows
        // progressively illuminated
        // --------------------------------------------------------

        val rows = 4
        val columns = 2

        val windowWidth =
            (mainRight - mainLeft) * 0.20f

        val windowHeight =
            (mainBottom - mainTop) * 0.105f

        val gapX =
            (mainRight - mainLeft) * 0.14f

        val gapY =
            (mainBottom - mainTop) * 0.105f

        for (row in 0 until rows) {

            for (column in 0 until columns) {

                val index =
                    row * columns + column

                val threshold =
                    (index + 1) /
                            (rows * columns)
                                .toFloat()

                val lit =
                    progress >= threshold

                drawRoundRect(
                    color =
                        if (lit) {
                            when {
                                index % 3 == 0 ->
                                    cyan.copy(
                                        alpha = 0.95f
                                    )

                                index % 3 == 1 ->
                                    Color.White.copy(
                                        alpha = 0.92f
                                    )

                                else ->
                                    Color(
                                        0xFFB49CFF
                                    ).copy(
                                        alpha = 0.95f
                                    )
                            }
                        } else {
                            darkWindow
                        },
                    topLeft = Offset(
                        mainLeft +
                                gapX +
                                column *
                                (windowWidth + gapX),

                        mainTop +
                                gapY +
                                row *
                                (windowHeight + gapY)
                    ),
                    size = Size(
                        windowWidth,
                        windowHeight
                    ),
                    cornerRadius =
                        androidx.compose.ui.geometry.CornerRadius(
                            w * 0.012f
                        )
                )
            }
        }

        // --------------------------------------------------------
        // Antenna
        // --------------------------------------------------------

        drawLine(
            color = cyan,
            start = Offset(
                w * 0.51f,
                mainTop
            ),
            end = Offset(
                w * 0.51f,
                h * 0.03f
            ),
            strokeWidth =
                w * 0.025f,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = cyan,
            radius = w * 0.03f,
            center = Offset(
                w * 0.51f,
                h * 0.03f
            )
        )

        // --------------------------------------------------------
        // Foundation
        // --------------------------------------------------------

        drawRoundRect(
            color = white,
            topLeft = Offset(
                w * 0.04f,
                h * 0.88f
            ),
            size = Size(
                w * 0.92f,
                h * 0.07f
            ),
            cornerRadius =
                androidx.compose.ui.geometry.CornerRadius(
                    w * 0.035f
                )
        )
    }
}

/* ================================================================
   PARTICLES
   ================================================================ */

private data class CreativeParticle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float
)

@Composable
private fun CreativeParticleField(
    rotation: Float,
    color: Color
) {

    val particles = remember {

        val list =
            mutableListOf<CreativeParticle>()

        val rings =
            listOf(
                160f,
                220f,
                290f,
                360f
            )

        val counts =
            listOf(
                7,
                9,
                12,
                15
            )

        rings.forEachIndexed { ring, radius ->

            repeat(
                counts[ring]
            ) { index ->

                val angle =
                    index *
                            (360f /
                                    counts[ring]) *
                            Math.PI /
                            180.0

                list.add(
                    CreativeParticle(
                        x =
                            (cos(angle) *
                                    radius)
                                .toFloat(),

                        y =
                            (sin(angle) *
                                    radius)
                                .toFloat(),

                        radius =
                            when (ring) {
                                0 -> 2.8f
                                1 -> 2.2f
                                2 -> 1.8f
                                else -> 1.4f
                            },

                        alpha =
                            when (ring) {
                                0 -> 0.26f
                                1 -> 0.19f
                                2 -> 0.12f
                                else -> 0.08f
                            }
                    )
                )
            }
        }

        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                rotationZ = rotation
            },
        contentAlignment =
            Alignment.Center
    ) {

        particles.forEach { particle ->

            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {

                val center =
                    Offset(
                        size.width / 2f +
                                particle.x.dp.toPx(),

                        size.height / 2f +
                                particle.y.dp.toPx()
                    )

                drawCircle(
                    color = color.copy(
                        alpha =
                            particle.alpha
                    ),
                    radius =
                        particle.radius.dp.toPx(),
                    center = center
                )
            }
        }
    }
}
