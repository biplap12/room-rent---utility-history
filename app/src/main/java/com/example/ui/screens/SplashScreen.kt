////package com.example.ui.screens
////
////import androidx.compose.animation.core.Animatable
////import androidx.compose.animation.core.CubicBezierEasing
////import androidx.compose.animation.core.LinearEasing
////import androidx.compose.animation.core.RepeatMode
////import androidx.compose.animation.core.animateFloat
////import androidx.compose.animation.core.infiniteRepeatable
////import androidx.compose.animation.core.rememberInfiniteTransition
////import androidx.compose.animation.core.tween
////import androidx.compose.foundation.Image
////import androidx.compose.foundation.background
////import androidx.compose.foundation.layout.*
////import androidx.compose.foundation.shape.CircleShape
////import androidx.compose.foundation.shape.RoundedCornerShape
////import androidx.compose.material.icons.Icons
////import androidx.compose.material.icons.filled.Apartment
////import androidx.compose.material3.Icon
////import androidx.compose.material3.Text
////import androidx.compose.runtime.Composable
////import androidx.compose.runtime.LaunchedEffect
////import androidx.compose.runtime.getValue
////import androidx.compose.runtime.remember
////import androidx.compose.runtime.rememberUpdatedState
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.draw.alpha
////import androidx.compose.ui.draw.blur
////import androidx.compose.ui.draw.clip
////import androidx.compose.ui.draw.rotate
////import androidx.compose.ui.draw.scale
////import androidx.compose.ui.geometry.Offset
////import androidx.compose.ui.graphics.Brush
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.graphics.Shadow
////import androidx.compose.ui.res.painterResource
////import androidx.compose.ui.text.TextStyle
////import androidx.compose.ui.text.font.FontWeight
////import androidx.compose.ui.text.style.TextAlign
////import androidx.compose.ui.unit.dp
////import androidx.compose.ui.unit.sp
////import com.example.R
////import kotlinx.coroutines.delay
////import kotlinx.coroutines.launch
////import kotlin.math.cos
////import kotlin.math.sin
////
////@Composable
////fun SplashScreen(onFinished: () -> Unit) {
////
////    // ✅ FIX 1: keep the latest callback so stale lambdas can't block us
////    val currentOnFinished by rememberUpdatedState(onFinished)
////
////    val smoothEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
////    val gentleEase = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
////    val softEase   = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
////
////    val glowAlpha  = remember { Animatable(0f) }
////    val logoScale  = remember { Animatable(0.55f) }
////    val logoRotate = remember { Animatable(-8f) }
////    val logoAlpha  = remember { Animatable(0f) }
////    val textAlpha  = remember { Animatable(0f) }
////    val textSlide  = remember { Animatable(20f) }
////    val tagAlpha   = remember { Animatable(0f) }
////    val barAlpha   = remember { Animatable(0f) }
////    val progress   = remember { Animatable(0f) }
////    val flagAlpha  = remember { Animatable(0f) }
////    val flagSlide  = remember { Animatable(12f) }
////
////    val pulse = rememberInfiniteTransition(label = "pulse")
////    val pulseScale by pulse.animateFloat(
////        initialValue = 0.94f,
////        targetValue = 1.08f,
////        animationSpec = infiniteRepeatable(
////            animation = tween(2600, easing = softEase),
////            repeatMode = RepeatMode.Reverse
////        ),
////        label = "pulseScale"
////    )
////    val pulseAlpha by pulse.animateFloat(
////        initialValue = 0.40f,
////        targetValue = 0.70f,
////        animationSpec = infiniteRepeatable(
////            animation = tween(2600, easing = softEase),
////            repeatMode = RepeatMode.Reverse
////        ),
////        label = "pulseAlpha"
////    )
////
////    val drift = rememberInfiniteTransition(label = "drift")
////    val particleRot by drift.animateFloat(
////        initialValue = 0f,
////        targetValue = 360f,
////        animationSpec = infiniteRepeatable(
////            animation = tween(32000, easing = LinearEasing),
////            repeatMode = RepeatMode.Restart
////        ),
////        label = "particleRot"
////    )
////
////    // ✅ FIX 2: animations are their own effect. If they get cancelled,
////    // the exit timer below is UNTOUCHED and will still fire.
////    LaunchedEffect(Unit) {
////        launch { glowAlpha.animateTo(1f, tween(900, easing = smoothEase)) }
////        launch {
////            logoScale.animateTo(1f, tween(1000, easing = smoothEase))
////            logoRotate.animateTo(0f, tween(1000, easing = smoothEase))
////            logoAlpha.animateTo(1f, tween(700, easing = gentleEase))
////        }
////        launch {
////            delay(400)
////            textAlpha.animateTo(1f, tween(700, easing = gentleEase))
////            textSlide.animateTo(0f, tween(700, easing = smoothEase))
////        }
////        launch {
////            delay(700)
////            tagAlpha.animateTo(1f, tween(600, easing = gentleEase))
////        }
////        launch {
////            delay(900)
////            barAlpha.animateTo(1f, tween(500, easing = gentleEase))
////            progress.animateTo(1f, tween(1600, easing = softEase))
////        }
////        launch {
////            delay(200)
////            flagAlpha.animateTo(1f, tween(700, easing = gentleEase))
////            flagSlide.animateTo(0f, tween(700, easing = smoothEase))
////        }
////    }
////
////    // ✅ FIX 3: separate effect just for the exit timer — guaranteed to run
////    LaunchedEffect(Unit) {
////        delay(3000)
////        currentOnFinished()
////    }
////
////    // ── palette ─────────────────────────────────────────────────
////    val topColor    = Color(0xFF1E1B4B)
////    val midColor    = Color(0xFF4F46E5)
////    val bottomColor = Color(0xFF0EA5E9)
////    val accent      = Color(0xFFC7D2FE)
////    val white       = Color.White
////    val whiteSoft   = Color.White.copy(alpha = 0.78f)
////    val whiteMuted  = Color.White.copy(alpha = 0.55f)
////
////    Box(
////        modifier = Modifier
////            .fillMaxSize()
////            .background(
////                Brush.verticalGradient(
////                    colors = listOf(topColor, midColor, bottomColor)
////                )
////            )
////    ) {
////        ParticleField(rotation = particleRot, color = white)
////
////        Box(
////            modifier = Modifier
////                .align(Alignment.Center)
////                .offset(y = (-52).dp)
////                .size(340.dp)
////                .scale(pulseScale)
////                .alpha(glowAlpha.value * pulseAlpha)
////                .blur(70.dp)
////                .background(
////                    Brush.radialGradient(
////                        colors = listOf(
////                            accent.copy(alpha = 0.85f),
////                            Color.Transparent
////                        )
////                    ),
////                    shape = CircleShape
////                )
////        )
////
////        Column(
////            modifier = Modifier
////                .fillMaxSize()
////                .padding(horizontal = 32.dp),
////            horizontalAlignment = Alignment.CenterHorizontally,
////            verticalArrangement = Arrangement.Center
////        ) {
////            Box(
////                modifier = Modifier
////                    .size(130.dp)
////                    .scale(logoScale.value)
////                    .rotate(logoRotate.value)
////                    .alpha(logoAlpha.value)
////                    .clip(RoundedCornerShape(32.dp))
////                    .background(
////                        Brush.linearGradient(
////                            colors = listOf(
////                                white.copy(alpha = 0.28f),
////                                white.copy(alpha = 0.10f)
////                            )
////                        )
////                    ),
////                contentAlignment = Alignment.Center
////            ) {
////                Box(
////                    modifier = Modifier
////                        .size(120.dp)
////                        .clip(RoundedCornerShape(28.dp))
////                        .background(Color.White.copy(alpha = 0.06f))
////                )
////                Icon(
////                    imageVector = Icons.Default.Apartment,
////                    contentDescription = null,
////                    tint = white,
////                    modifier = Modifier.size(66.dp)
////                )
////            }
////
////            Spacer(Modifier.height(36.dp))
////
////            Text(
////                text = "Room Rent & Utility",
////                color = white,
////                fontSize = 26.sp,
////                fontWeight = FontWeight.Bold,
////                textAlign = TextAlign.Center,
////                style = TextStyle(
////                    shadow = Shadow(
////                        color = Color.Black.copy(alpha = 0.22f),
////                        offset = Offset(0f, 2f),
////                        blurRadius = 14f
////                    )
////                ),
////                modifier = Modifier
////                    .alpha(textAlpha.value)
////                    .offset(y = textSlide.value.dp)
////            )
////
////            Spacer(Modifier.height(10.dp))
////
////            Text(
////                text = "Offline Meter & Rent Notebook",
////                color = whiteSoft,
////                fontSize = 13.sp,
////                letterSpacing = 0.6.sp,
////                textAlign = TextAlign.Center,
////                modifier = Modifier.alpha(tagAlpha.value)
////            )
////
////            Spacer(Modifier.height(56.dp))
////
////            Box(
////                modifier = Modifier
////                    .fillMaxWidth(0.55f)
////                    .height(3.dp)
////                    .alpha(barAlpha.value)
////                    .clip(RoundedCornerShape(2.dp))
////                    .background(white.copy(alpha = 0.18f))
////            ) {
////                Box(
////                    modifier = Modifier
////                        .fillMaxHeight()
////                        .fillMaxWidth(progress.value)
////                        .clip(RoundedCornerShape(2.dp))
////                        .background(
////                            Brush.horizontalGradient(listOf(white, accent))
////                        )
////                )
////            }
////
////            Spacer(Modifier.height(14.dp))
////
////            Text(
////                text = "Loading your data…",
////                color = whiteMuted,
////                fontSize = 11.sp,
////                letterSpacing = 0.4.sp,
////                modifier = Modifier.alpha(barAlpha.value)
////            )
////        }
////
////        Column(
////            modifier = Modifier
////                .align(Alignment.BottomCenter)
////                .padding(bottom = 28.dp)
////                .alpha(flagAlpha.value)
////                .offset(y = flagSlide.value.dp),
////            horizontalAlignment = Alignment.CenterHorizontally
////        ) {
////            Image(
////                painter = painterResource(R.drawable.nepal_flag),
////                contentDescription = "Nepal flag",
////                modifier = Modifier
////                    .size(width = 34.dp, height = 42.dp)
////                    .clip(RoundedCornerShape(3.dp))
////            )
////
////            Spacer(Modifier.height(10.dp))
////
////            Text(
////                text = "Made in Nepal",
////                color = white,
////                fontSize = 12.sp,
////                fontWeight = FontWeight.SemiBold,
////                letterSpacing = 1.4.sp
////            )
////
////            Spacer(Modifier.height(4.dp))
////
////            Text(
////                text = "by Biplap Neupane",
////                color = accent,
////                fontSize = 11.sp,
////                fontWeight = FontWeight.Medium,
////                letterSpacing = 0.8.sp
////            )
////
////            Spacer(Modifier.height(6.dp))
////
////            Text(
////                text = "Version 1.0",
////                color = whiteMuted,
////                fontSize = 9.sp,
////                letterSpacing = 0.5.sp
////            )
////        }
////    }
////}
////
////@Composable
////private fun ParticleField(rotation: Float, color: Color) {
////    Box(
////        modifier = Modifier
////            .fillMaxSize()
////            .rotate(rotation),
////        contentAlignment = Alignment.Center
////    ) {
////        val count = 14
////        val radius = 260f
////        for (i in 0 until count) {
////            val angle = (i * (360f / count)) * (Math.PI / 180.0)
////            val x = (cos(angle) * radius).toFloat()
////            val y = (sin(angle) * radius).toFloat()
////            val size = when (i % 3) {
////                0 -> 6.dp
////                1 -> 4.dp
////                else -> 3.dp
////            }
////            val alpha = when (i % 3) {
////                0 -> 0.32f
////                1 -> 0.22f
////                else -> 0.16f
////            }
////            Box(
////                modifier = Modifier
////                    .offset(x = x.dp, y = y.dp)
////                    .size(size)
////                    .clip(CircleShape)
////                    .background(color.copy(alpha = alpha))
////            )
////        }
////    }
////}
//
//
//package com.example.ui.screens
//
//import androidx.compose.animation.AnimatedContent
//import androidx.compose.animation.core.Animatable
//import androidx.compose.animation.core.CubicBezierEasing
//import androidx.compose.animation.core.LinearEasing
//import androidx.compose.animation.core.RepeatMode
//import androidx.compose.animation.core.animateFloat
//import androidx.compose.animation.core.infiniteRepeatable
//import androidx.compose.animation.core.rememberInfiniteTransition
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.animation.togetherWith
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberUpdatedState
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.rotate
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Shadow
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.R
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import kotlin.math.cos
//import kotlin.math.sin
//
//@Composable
//fun SplashScreen(onFinished: () -> Unit) {
//
//    val currentOnFinished by rememberUpdatedState(onFinished)
//
//    val smoothEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
//    val gentleEase = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
//
//    // ── entrance animatables ───────────────────────────────────
//    val haloAlpha   = remember { Animatable(0f) }
//    val ringAlpha   = remember { Animatable(0f) }
//    val logoScale   = remember { Animatable(0.82f) }
//    val logoRotate  = remember { Animatable(-4f) }
//    val logoAlpha   = remember { Animatable(0f) }
//    val textAlpha   = remember { Animatable(0f) }
//    val textSlide   = remember { Animatable(10f) }
//    val tagAlpha    = remember { Animatable(0f) }
//    val loaderAlpha = remember { Animatable(0f) }
//    val flagAlpha   = remember { Animatable(0f) }
//    val flagSlide   = remember { Animatable(8f) }
//    val progress    = remember { Animatable(0f) }
//
//    // ── pulse ──────────────────────────────────────────────────
//    val pulse = rememberInfiniteTransition(label = "pulse")
//    val pulseScale by pulse.animateFloat(
//        initialValue = 0.94f,
//        targetValue = 1.06f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(
//                durationMillis = 2000,
//                easing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
//            ),
//            repeatMode = RepeatMode.Reverse
//        ),
//        label = "pulseScale"
//    )
//
//    // ── shimmer ────────────────────────────────────────────────
//    val shimmer = rememberInfiniteTransition(label = "shimmer")
//    val shimmerRot by shimmer.animateFloat(
//        initialValue = 0f, targetValue = 360f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(2000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ), label = "shimmerRot"
//    )
//
//    // ── particles ──────────────────────────────────────────────
//    val drift = rememberInfiniteTransition(label = "drift")
//    val particleRot by drift.animateFloat(
//        initialValue = 0f, targetValue = 360f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(34000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ), label = "particleRot"
//    )
//
//    // ── orbit rings ────────────────────────────────────────────
//    val orbit = rememberInfiniteTransition(label = "orbit")
//    val orbitA by orbit.animateFloat(
//        initialValue = 0f, targetValue = 360f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(14000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ), label = "orbitA"
//    )
//    val orbitB by orbit.animateFloat(
//        initialValue = 360f, targetValue = 0f,
//        animationSpec = infiniteRepeatable(
//            animation = tween(20000, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart
//        ), label = "orbitB"
//    )
//
//    // ══════════════════════════════════════════════════════════
//    //  ⚡ FAST entrance timeline — everything within 500ms
//    // ══════════════════════════════════════════════════════════
//    LaunchedEffect(Unit) {
//        launch { haloAlpha.animateTo(1f, tween(350, easing = smoothEase)) }
//        launch { ringAlpha.animateTo(1f, tween(400, easing = smoothEase)) }
//        launch {
//            logoScale.animateTo(1f, tween(400, easing = smoothEase))
//            logoRotate.animateTo(0f, tween(400, easing = smoothEase))
//            logoAlpha.animateTo(1f, tween(300, easing = gentleEase))
//        }
//        launch {
//            // ⚡ NO delay — text appears with logo
//            textAlpha.animateTo(1f, tween(300, easing = gentleEase))
//            textSlide.animateTo(0f, tween(300, easing = smoothEase))
//        }
//        launch {
//            // ⚡ subtitle — also nearly instant
//            delay(50)
//            tagAlpha.animateTo(1f, tween(280, easing = gentleEase))
//        }
//        launch {
//            delay(100)
//            loaderAlpha.animateTo(1f, tween(250, easing = gentleEase))
//            progress.animateTo(
//                targetValue = 1f,
//                animationSpec = tween(
//                    durationMillis = 900,
//                    easing = LinearEasing
//                )
//            )
//        }
//        launch {
//            delay(60)
//            flagAlpha.animateTo(1f, tween(320, easing = gentleEase))
//            flagSlide.animateTo(0f, tween(320, easing = smoothEase))
//        }
//    }
//
//    // ── safety completion ──────────────────────────────────────
//    LaunchedEffect(Unit) {
//        delay(1100)
//        if (progress.value < 1f) progress.snapTo(1f)
//    }
//
//    // ── ⚡ FASTER exit timer (1800ms total) ────────────────────
//    LaunchedEffect(Unit) {
//        delay(1800)
//        currentOnFinished()
//    }
//
//    // ── palette ────────────────────────────────────────────────
//    val topColor    = Color(0xFF0B1020)
//    val midColor    = Color(0xFF1E1B4B)
//    val bottomColor = Color(0xFF4F46E5)
//    val accentA     = Color(0xFFA5B4FC)
//    val accentB     = Color(0xFF67E8F9)
//    val accentC     = Color(0xFFF0ABFC)
//    val white       = Color.White
//    val whiteSoft   = Color.White.copy(alpha = 0.78f)
//    val whiteMuted  = Color.White.copy(alpha = 0.55f)
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(topColor, midColor, bottomColor)
//                )
//            )
//    ) {
//        // ── ambient glows ──────────────────────────────────────
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    Brush.radialGradient(
//                        colors = listOf(
//                            accentA.copy(alpha = 0.28f),
//                            Color.Transparent
//                        ),
//                        center = Offset(0.15f, 0.15f),
//                        radius = 700f
//                    )
//                )
//        )
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(
//                    Brush.radialGradient(
//                        colors = listOf(
//                            accentC.copy(alpha = 0.20f),
//                            Color.Transparent
//                        ),
//                        center = Offset(1f, 0.9f),
//                        radius = 800f
//                    )
//                )
//        )
//
//        // ── particles ──────────────────────────────────────────
//        ParticleField(rotation = particleRot, color = white)
//
//        // ══════════════════════════════════════════════════════
//        //  MAIN CONTENT — perfectly centered column
//        // ══════════════════════════════════════════════════════
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(bottom = 120.dp)
//                .padding(horizontal = 28.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            // ── LOGO + halo + orbit rings (compact 170dp) ──
//            Box(
//                modifier = Modifier.size(170.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                // halo (clipped inside circle)
//                Box(
//                    modifier = Modifier
//                        .size(170.dp)
//                        .clip(CircleShape)
//                        .graphicsLayer {
//                            scaleX = pulseScale
//                            scaleY = pulseScale
//                        }
//                        .alpha(haloAlpha.value)
//                        .background(
//                            Brush.radialGradient(
//                                colors = listOf(
//                                    accentA.copy(alpha = 0.55f),
//                                    accentA.copy(alpha = 0.15f),
//                                    Color.Transparent
//                                )
//                            ),
//                            shape = CircleShape
//                        )
//                )
//
//                // orbit rings
//                Box(
//                    modifier = Modifier
//                        .size(155.dp)
//                        .alpha(ringAlpha.value),
//                    contentAlignment = Alignment.Center
//                ) {
//                    OrbitRing(155.dp, orbitA, 1.2f, 3, accentA.copy(alpha = 0.55f))
//                    OrbitRing(128.dp, orbitB, 1.0f, 4, accentB.copy(alpha = 0.45f))
//                }
//
//                // logo card — perfectly centered
//                Box(
//                    modifier = Modifier
//                        .size(100.dp)
//                        .scale(logoScale.value)
//                        .rotate(logoRotate.value)
//                        .alpha(logoAlpha.value)
//                        .clip(RoundedCornerShape(26.dp))
//                        .background(
//                            Brush.linearGradient(
//                                colors = listOf(
//                                    Color.White.copy(alpha = 0.24f),
//                                    Color.White.copy(alpha = 0.08f)
//                                )
//                            )
//                        )
//                        .border(
//                            width = 1.dp,
//                            brush = Brush.linearGradient(
//                                colors = listOf(
//                                    Color.White.copy(alpha = 0.55f),
//                                    Color.White.copy(alpha = 0.10f)
//                                )
//                            ),
//                            shape = RoundedCornerShape(26.dp)
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(100.dp)
//                            .clip(RoundedCornerShape(26.dp))
//                            .background(
//                                Brush.radialGradient(
//                                    colors = listOf(
//                                        accentA.copy(alpha = 0.35f),
//                                        Color.Transparent
//                                    )
//                                )
//                            )
//                    )
//                    Icon(
//                        imageVector = Icons.Default.Home,
//                        contentDescription = null,
//                        tint = white,
//                        modifier = Modifier.size(48.dp)
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(34.dp))
//
//            // ── TITLE ──
//            Text(
//                text = "Room Rent & Utility",
//                color = white,
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Bold,
//                textAlign = TextAlign.Center,
//                maxLines = 1,
//                style = TextStyle(
//                    shadow = Shadow(
//                        color = Color.Black.copy(alpha = 0.30f),
//                        offset = Offset(0f, 3f),
//                        blurRadius = 16f
//                    )
//                ),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .alpha(textAlpha.value)
//                    .offset(y = textSlide.value.dp)
//            )
//
//            Spacer(Modifier.height(6.dp))
//
//            // ── SUBTITLE ──
//            Text(
//                text = "Offline Meter & Rent Notebook",
//                color = whiteSoft,
//                fontSize = 12.sp,
//                letterSpacing = 0.5.sp,
//                textAlign = TextAlign.Center,
//                maxLines = 1,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .alpha(tagAlpha.value)
//            )
//
//            Spacer(Modifier.height(40.dp))
//
//            // ══════════════════════════════════════════════════
//            //  PROGRESS RING
//            // ══════════════════════════════════════════════════
//            Box(
//                modifier = Modifier
//                    .size(100.dp)
//                    .alpha(loaderAlpha.value),
//                contentAlignment = Alignment.Center
//            ) {
//                val fillAngle = 360f * progress.value.coerceIn(0f, 1f)
//
//                // track + progress arc
//                Canvas(modifier = Modifier.fillMaxSize()) {
//                    val strokeW = 4.dp.toPx()
//                    val radius = size.minDimension / 2f - strokeW / 2f
//                    val centerPt = Offset(size.width / 2f, size.height / 2f)
//
//                    drawCircle(
//                        color = Color.White.copy(alpha = 0.10f),
//                        radius = radius,
//                        center = centerPt,
//                        style = Stroke(width = strokeW)
//                    )
//
//                    if (fillAngle > 0f) {
//                        drawArc(
//                            brush = Brush.sweepGradient(
//                                colors = listOf(
//                                    accentA,
//                                    accentB,
//                                    accentC,
//                                    accentC
//                                ),
//                                center = centerPt
//                            ),
//                            startAngle = -90f,
//                            sweepAngle = fillAngle,
//                            useCenter = false,
//                            topLeft = Offset(strokeW / 2f, strokeW / 2f),
//                            size = Size(
//                                size.width - strokeW,
//                                size.height - strokeW
//                            ),
//                            style = Stroke(
//                                width = strokeW,
//                                cap = StrokeCap.Round
//                            )
//                        )
//                    }
//                }
//
//                // rotating shimmer
//                Canvas(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .graphicsLayer { rotationZ = shimmerRot }
//                ) {
//                    val strokeW = 4.dp.toPx()
//                    val radius = size.minDimension / 2f - strokeW / 2f
//                    val centerPt = Offset(size.width / 2f, size.height / 2f)
//                    drawArc(
//                        brush = Brush.sweepGradient(
//                            colors = listOf(
//                                Color.Transparent,
//                                Color.White.copy(alpha = 0.45f),
//                                Color.Transparent
//                            ),
//                            center = centerPt
//                        ),
//                        startAngle = -90f,
//                        sweepAngle = 360f,
//                        useCenter = false,
//                        topLeft = Offset(strokeW / 2f, strokeW / 2f),
//                        size = Size(
//                            size.width - strokeW,
//                            size.height - strokeW
//                        ),
//                        style = Stroke(
//                            width = strokeW,
//                            cap = StrokeCap.Butt
//                        )
//                    )
//                }
//
//                // tip dot
//                if (fillAngle > 2f && fillAngle < 358f) {
//                    Canvas(modifier = Modifier.fillMaxSize()) {
//                        val strokeW = 4.dp.toPx()
//                        val radius = size.minDimension / 2f - strokeW / 2f
//                        val angleRad = Math.toRadians(-90.0 + fillAngle)
//                        val tipX = center.x + (radius * cos(angleRad)).toFloat()
//                        val tipY = center.y + (radius * sin(angleRad)).toFloat()
//
//                        drawCircle(
//                            color = accentC.copy(alpha = 0.45f),
//                            radius = 6.dp.toPx(),
//                            center = Offset(tipX, tipY)
//                        )
//                        drawCircle(
//                            color = Color.White,
//                            radius = 2.5.dp.toPx(),
//                            center = Offset(tipX, tipY)
//                        )
//                    }
//                }
//
//                // percentage
//                val percent = (progress.value * 100).toInt().coerceIn(0, 100)
//
//                Row(
//                    verticalAlignment = Alignment.Bottom,
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Text(
//                        text = "$percent",
//                        color = Color.White,
//                        fontSize = 28.sp,
//                        fontWeight = FontWeight.Bold,
//                        letterSpacing = (-1).sp
//                    )
//                    Text(
//                        text = "%",
//                        color = whiteSoft,
//                        fontSize = 13.sp,
//                        fontWeight = FontWeight.Medium,
//                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            // ── STATUS TEXT ────────────────────────────────────
//            val statusMessages = remember {
//                listOf(
//                    "Warming up",
//                    "Loading rooms",
//                    "Fetching records",
//                    "Almost ready"
//                )
//            }
//            var statusIndex by remember { mutableStateOf(0) }
//
//            LaunchedEffect(Unit) {
//                while (statusIndex < statusMessages.lastIndex) {
//                    delay(380)
//                    statusIndex =
//                        (statusIndex + 1).coerceAtMost(statusMessages.lastIndex)
//                }
//            }
//
//            AnimatedContent(
//                targetState = statusMessages[statusIndex],
//                transitionSpec = {
//                    (
//                            fadeIn(animationSpec = tween(250)) +
//                                    slideInVertically(
//                                        animationSpec = tween(250, easing = smoothEase)
//                                    ) { it / 3 }
//                            ).togetherWith(
//                            fadeOut(animationSpec = tween(180)) +
//                                    slideOutVertically(
//                                        animationSpec = tween(180, easing = smoothEase)
//                                    ) { -it / 3 }
//                        )
//                },
//                label = "statusText"
//            ) { msg ->
//                Text(
//                    text = msg,
//                    color = whiteSoft,
//                    fontSize = 11.sp,
//                    letterSpacing = 1.2.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//
//        // ══════════════════════════════════════════════════════
//        //  SIGNATURE — bottom-most
//        // ══════════════════════════════════════════════════════
//        Column(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(bottom = 22.dp)
//                .alpha(flagAlpha.value)
//                .offset(y = flagSlide.value.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Image(
//                painter = painterResource(R.drawable.nepal_flag),
//                contentDescription = "Nepal flag",
//                modifier = Modifier
//                    .size(width = 26.dp, height = 34.dp)
//                    .clip(RoundedCornerShape(3.dp))
//            )
//            Spacer(Modifier.height(7.dp))
//            Text(
//                text = "Made in Nepal",
//                color = white,
//                fontSize = 11.sp,
//                fontWeight = FontWeight.SemiBold,
//                letterSpacing = 1.6.sp
//            )
//            Spacer(Modifier.height(3.dp))
//            Text(
//                text = "by Biplap Neupane",
//                color = accentA,
//                fontSize = 10.sp,
//                fontWeight = FontWeight.Medium,
//                letterSpacing = 0.9.sp
//            )
//            Spacer(Modifier.height(3.dp))
//            Text(
//                text = "Version 1.0",
//                color = whiteMuted,
//                fontSize = 9.sp,
//                letterSpacing = 0.5.sp
//            )
//        }
//    }
//}
//
///* ────────────────────────────────────────────────────────────
//   Orbit ring
//   ──────────────────────────────────────────────────────────── */
//@Composable
//private fun OrbitRing(
//    size: androidx.compose.ui.unit.Dp,
//    rotation: Float,
//    strokeWidth: Float,
//    segmentCount: Int,
//    color: Color
//) {
//    Canvas(
//        modifier = Modifier
//            .size(size)
//            .graphicsLayer { rotationZ = rotation }
//    ) {
//        val radius = this.size.minDimension / 2f - strokeWidth
//        val center = this.center
//        val gapDeg = 360f / segmentCount
//        val arcDeg = gapDeg * 0.55f
//
//        repeat(segmentCount) { i ->
//            val startAngle = i * gapDeg
//            drawArc(
//                color = color,
//                startAngle = startAngle,
//                sweepAngle = arcDeg,
//                useCenter = false,
//                topLeft = Offset(center.x - radius, center.y - radius),
//                size = Size(radius * 2, radius * 2),
//                style = Stroke(
//                    width = strokeWidth,
//                    cap = StrokeCap.Round
//                )
//            )
//        }
//    }
//}
//
///* ────────────────────────────────────────────────────────────
//   Particle field
//   ──────────────────────────────────────────────────────────── */
//private data class ParticleData(
//    val x: Float,
//    val y: Float,
//    val size: androidx.compose.ui.unit.Dp,
//    val alpha: Float
//)
//
//@Composable
//private fun ParticleField(rotation: Float, color: Color) {
//    val particles = remember {
//        val rings = listOf(200f, 270f, 340f)
//        val perRing = listOf(6, 6, 6)
//        val out = mutableListOf<ParticleData>()
//        rings.forEachIndexed { ringIdx, radius ->
//            val count = perRing[ringIdx]
//            repeat(count) { i ->
//                val angle = (i * (360f / count)) * (Math.PI / 180.0)
//                val x = (cos(angle) * radius).toFloat()
//                val y = (sin(angle) * radius).toFloat()
//                val size = when (ringIdx) {
//                    0 -> 4.dp
//                    1 -> 3.dp
//                    else -> 2.dp
//                }
//                val alpha = when (ringIdx) {
//                    0 -> 0.32f
//                    1 -> 0.22f
//                    else -> 0.14f
//                }
//                out.add(ParticleData(x, y, size, alpha))
//            }
//        }
//        out
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .graphicsLayer { rotationZ = rotation },
//        contentAlignment = Alignment.Center
//    ) {
//        particles.forEach { p ->
//            Box(
//                modifier = Modifier
//                    .offset(x = p.x.dp, y = p.y.dp)
//                    .size(p.size)
//                    .clip(CircleShape)
//                    .background(color.copy(alpha = p.alpha))
//            )
//        }
//    }
//}


package com.example.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    val currentOnFinished by rememberUpdatedState(onFinished)

    val smoothEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    val gentleEase = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

    // ── entrance animatables ───────────────────────────────────
    val logoScale   = remember { Animatable(0.7f) }
    val logoAlpha   = remember { Animatable(0f) }
    val textAlpha   = remember { Animatable(0f) }
    val tagAlpha    = remember { Animatable(0f) }
    val loaderAlpha = remember { Animatable(0f) }
    val flagAlpha   = remember { Animatable(0f) }
    val progress    = remember { Animatable(0f) }

    // ── shimmer ────────────────────────────────────────────────
    val shimmer = rememberInfiniteTransition(label = "shimmer")
    val shimmerRot by shimmer.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "shimmerRot"
    )

    // ── particles ──────────────────────────────────────────────
    val drift = rememberInfiniteTransition(label = "drift")
    val particleRot by drift.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(34000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "particleRot"
    )

    // ── entrance timeline ──────────────────────────────────────
    LaunchedEffect(Unit) {
        launch { logoScale.animateTo(1f, tween(500, easing = smoothEase)) }
        launch { logoAlpha.animateTo(1f, tween(400, easing = gentleEase)) }
        launch { textAlpha.animateTo(1f, tween(400, easing = gentleEase)) }
        launch {
            delay(100)
            tagAlpha.animateTo(1f, tween(300, easing = gentleEase))
        }
        launch {
            delay(200)
            loaderAlpha.animateTo(1f, tween(300, easing = gentleEase))
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(900, easing = LinearEasing)
            )
        }
        launch {
            delay(100)
            flagAlpha.animateTo(1f, tween(400, easing = gentleEase))
        }
    }

    // ── safety completion ──────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(1200)
        if (progress.value < 1f) progress.snapTo(1f)
    }

    // ── exit timer ─────────────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(1800)
        currentOnFinished()
    }

    // ── palette ────────────────────────────────────────────────
    val topColor    = Color(0xFF0B1020)
    val midColor    = Color(0xFF1E1B4B)
    val bottomColor = Color(0xFF4F46E5)
    val accentA     = Color(0xFFA5B4FC)
    val accentB     = Color(0xFF67E8F9)
    val accentC     = Color(0xFFF0ABFC)
    val white       = Color.White
    val whiteSoft   = Color.White.copy(alpha = 0.78f)
    val whiteMuted  = Color.White.copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(topColor, midColor, bottomColor)
                )
            )
    ) {
        // ── ambient glows ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentA.copy(alpha = 0.28f),
                            Color.Transparent
                        ),
                        center = Offset(0.15f, 0.15f),
                        radius = 700f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentC.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = Offset(1f, 0.9f),
                        radius = 800f
                    )
                )
        )

        // ── particles ──────────────────────────────────────────
        ParticleField(rotation = particleRot, color = white)

        // ══════════════════════════════════════════════════════
        //  MAIN CONTENT
        // ══════════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ══════════════════════════════════════════════════
            //  LOGO CARD — no circles, bigger icon
            // ══════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .clip(RoundedCornerShape(38.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.55f),
                                Color.White.copy(alpha = 0.10f)
                            )
                        ),
                        shape = RoundedCornerShape(38.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // subtle inner glow
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(38.dp))
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accentA.copy(alpha = 0.30f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // 🏢 Modern apartment logo — bigger
                ModernApartmentLogo(
                    modifier = Modifier.size(110.dp),
                    color = white
                )
            }

            Spacer(Modifier.height(40.dp))

            // ── TITLE ──
            Text(
                text = "Room Rent & Utility",
                color = white,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.30f),
                        offset = Offset(0f, 3f),
                        blurRadius = 16f
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(textAlpha.value)
            )

            Spacer(Modifier.height(6.dp))

            // ── SUBTITLE ──
            Text(
                text = "Offline Meter & Rent Notebook",
                color = whiteSoft,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(tagAlpha.value)
            )

            Spacer(Modifier.height(40.dp))

            // ══════════════════════════════════════════════════
            //  PROGRESS RING
            // ══════════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .alpha(loaderAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                val fillAngle = 360f * progress.value.coerceIn(0f, 1f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 4.dp.toPx()
                    val radius = size.minDimension / 2f - strokeW / 2f
                    val centerPt = Offset(size.width / 2f, size.height / 2f)

                    drawCircle(
                        color = Color.White.copy(alpha = 0.10f),
                        radius = radius,
                        center = centerPt,
                        style = Stroke(width = strokeW)
                    )

                    if (fillAngle > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    accentA,
                                    accentB,
                                    accentC,
                                    accentC
                                ),
                                center = centerPt
                            ),
                            startAngle = -90f,
                            sweepAngle = fillAngle,
                            useCenter = false,
                            topLeft = Offset(strokeW / 2f, strokeW / 2f),
                            size = Size(
                                size.width - strokeW,
                                size.height - strokeW
                            ),
                            style = Stroke(
                                width = strokeW,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = shimmerRot }
                ) {
                    val strokeW = 4.dp.toPx()
                    val radius = size.minDimension / 2f - strokeW / 2f
                    val centerPt = Offset(size.width / 2f, size.height / 2f)
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.45f),
                                Color.Transparent
                            ),
                            center = centerPt
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(strokeW / 2f, strokeW / 2f),
                        size = Size(
                            size.width - strokeW,
                            size.height - strokeW
                        ),
                        style = Stroke(
                            width = strokeW,
                            cap = StrokeCap.Butt
                        )
                    )
                }

                if (fillAngle > 2f && fillAngle < 358f) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeW = 4.dp.toPx()
                        val radius = size.minDimension / 2f - strokeW / 2f
                        val angleRad = Math.toRadians(-90.0 + fillAngle)
                        val tipX = center.x + (radius * cos(angleRad)).toFloat()
                        val tipY = center.y + (radius * sin(angleRad)).toFloat()

                        drawCircle(
                            color = accentC.copy(alpha = 0.45f),
                            radius = 6.dp.toPx(),
                            center = Offset(tipX, tipY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = Offset(tipX, tipY)
                        )
                    }
                }

                val percent = (progress.value * 100).toInt().coerceIn(0, 100)

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$percent",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "%",
                        color = whiteSoft,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Loading your data…",
                color = whiteSoft,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.alpha(loaderAlpha.value)
            )
        }

        // ── SIGNATURE ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()          // ✅ respects gesture/nav bar
                .padding(bottom = 22.dp)
                .alpha(flagAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.nepal_flag),
                contentDescription = "Nepal flag",
                modifier = Modifier
                    .size(width = 42.dp, height = 54.dp)   // ← BIGGER
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.height(7.dp))
            Text(
                text = "Made in Nepal",
                color = white,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.6.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "by Biplap Neupane",
                color = accentA,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.9.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Version 1.0",
                color = whiteMuted,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/* ════════════════════════════════════════════════════════════
   🏢 MODERN APARTMENT LOGO
   3-building skyline with antenna and windows
   ════════════════════════════════════════════════════════════ */
@Composable
private fun ModernApartmentLogo(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val corner = CornerRadius(w * 0.06f)
        val windowColor = Color(0xFF1E1B4B)

        // ── MAIN TOWER ──
        val mainLeft   = w * 0.30f
        val mainRight  = w * 0.72f
        val mainTop    = h * 0.18f
        val mainBottom = h * 0.90f

        drawRoundRect(
            color = color,
            topLeft = Offset(mainLeft, mainTop),
            size = Size(mainRight - mainLeft, mainBottom - mainTop),
            cornerRadius = corner
        )

        // ── LEFT WING ──
        val leftL = w * 0.08f
        val leftR = w * 0.30f
        val leftT = h * 0.42f
        val leftB = h * 0.90f

        drawRoundRect(
            color = color.copy(alpha = 0.85f),
            topLeft = Offset(leftL, leftT),
            size = Size(leftR - leftL, leftB - leftT),
            cornerRadius = corner
        )

        // ── RIGHT WING ──
        val rightL = w * 0.72f
        val rightR = w * 0.94f
        val rightT = h * 0.32f
        val rightB = h * 0.90f

        drawRoundRect(
            color = color.copy(alpha = 0.85f),
            topLeft = Offset(rightL, rightT),
            size = Size(rightR - rightL, rightB - rightT),
            cornerRadius = corner
        )

        // ── ANTENNA ──
        drawLine(
            color = color,
            start = Offset(w * 0.51f, h * 0.18f),
            end = Offset(w * 0.51f, h * 0.06f),
            strokeWidth = w * 0.035f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = color,
            radius = w * 0.035f,
            center = Offset(w * 0.51f, h * 0.06f)
        )

        // ── MAIN TOWER WINDOWS (2×4) ──
        val winW = (mainRight - mainLeft) * 0.20f
        val winH = (mainBottom - mainTop) * 0.11f
        val padX = (mainRight - mainLeft) * 0.14f
        val padY = (mainBottom - mainTop) * 0.10f

        for (r in 0 until 4) {
            for (c in 0 until 2) {
                val x = mainLeft + padX + c * (winW + padX)
                val y = mainTop + padY + r * (winH + padY)
                drawRoundRect(
                    color = windowColor,
                    topLeft = Offset(x, y),
                    size = Size(winW, winH),
                    cornerRadius = CornerRadius(w * 0.015f)
                )
            }
        }

        // ── LEFT WING WINDOWS (1×2) ──
        val leftWinW = (leftR - leftL) * 0.36f
        val leftWinH = (leftB - leftT) * 0.16f
        val leftPadX = (leftR - leftL - leftWinW) / 2f
        val leftPadY = (leftB - leftT) * 0.18f

        for (r in 0 until 2) {
            val x = leftL + leftPadX
            val y = leftT + leftPadY + r * (leftWinH + leftPadY)
            drawRoundRect(
                color = windowColor,
                topLeft = Offset(x, y),
                size = Size(leftWinW, leftWinH),
                cornerRadius = CornerRadius(w * 0.015f)
            )
        }

        // ── RIGHT WING WINDOWS (1×3) ──
        val rightWinW = (rightR - rightL) * 0.36f
        val rightWinH = (rightB - rightT) * 0.11f
        val rightPadX = (rightR - rightL - rightWinW) / 2f
        val rightPadY = (rightB - rightT) * 0.14f

        for (r in 0 until 3) {
            val x = rightL + rightPadX
            val y = rightT + rightPadY + r * (rightWinH + rightPadY)
            drawRoundRect(
                color = windowColor,
                topLeft = Offset(x, y),
                size = Size(rightWinW, rightWinH),
                cornerRadius = CornerRadius(w * 0.015f)
            )
        }

        // ── GROUND LINE ──
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.05f, h * 0.90f),
            size = Size(w * 0.90f, h * 0.06f),
            cornerRadius = CornerRadius(w * 0.03f)
        )
    }
}

/* ────────────────────────────────────────────────────────────
   Particle field
   ──────────────────────────────────────────────────────────── */
private data class ParticleData(
    val x: Float,
    val y: Float,
    val size: androidx.compose.ui.unit.Dp,
    val alpha: Float
)

@Composable
private fun ParticleField(rotation: Float, color: Color) {
    val particles = remember {
        val rings = listOf(200f, 270f, 340f)
        val perRing = listOf(6, 6, 6)
        val out = mutableListOf<ParticleData>()
        rings.forEachIndexed { ringIdx, radius ->
            val count = perRing[ringIdx]
            repeat(count) { i ->
                val angle = (i * (360f / count)) * (Math.PI / 180.0)
                val x = (cos(angle) * radius).toFloat()
                val y = (sin(angle) * radius).toFloat()
                val size = when (ringIdx) {
                    0 -> 4.dp
                    1 -> 3.dp
                    else -> 2.dp
                }
                val alpha = when (ringIdx) {
                    0 -> 0.32f
                    1 -> 0.22f
                    else -> 0.14f
                }
                out.add(ParticleData(x, y, size, alpha))
            }
        }
        out
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { rotationZ = rotation },
        contentAlignment = Alignment.Center
    ) {
        particles.forEach { p ->
            Box(
                modifier = Modifier
                    .offset(x = p.x.dp, y = p.y.dp)
                    .size(p.size)
                    .clip(CircleShape)
                    .background(color.copy(alpha = p.alpha))
            )
        }
    }
}