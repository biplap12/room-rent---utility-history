package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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

    // ── easing curves ───────────────────────────────────────────
    val smoothEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    val gentleEase = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    val softEase   = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)

    // ── animatable states ───────────────────────────────────────
    val glowAlpha  = remember { Animatable(0f) }
    val logoScale  = remember { Animatable(0.55f) }
    val logoRotate = remember { Animatable(-8f) }
    val logoAlpha  = remember { Animatable(0f) }
    val textAlpha  = remember { Animatable(0f) }
    val textSlide  = remember { Animatable(20f) }
    val tagAlpha   = remember { Animatable(0f) }
    val barAlpha   = remember { Animatable(0f) }
    val progress   = remember { Animatable(0f) }
    val flagAlpha  = remember { Animatable(0f) }
    val flagSlide  = remember { Animatable(12f) }

    // ── ambient pulse ───────────────────────────────────────────
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = softEase),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = softEase),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // ── slow particle drift ─────────────────────────────────────
    val drift = rememberInfiniteTransition(label = "drift")
    val particleRot by drift.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleRot"
    )

    // ── parallel entrance animations ────────────────────────────
    LaunchedEffect(Unit) {
        launch {
            glowAlpha.animateTo(1f, tween(900, easing = smoothEase))
        }
        launch {
            logoScale.animateTo(1f, tween(1000, easing = smoothEase))
            logoRotate.animateTo(0f, tween(1000, easing = smoothEase))
            logoAlpha.animateTo(1f, tween(700, easing = gentleEase))
        }
        launch {
            delay(400)
            textAlpha.animateTo(1f, tween(700, easing = gentleEase))
            textSlide.animateTo(0f, tween(700, easing = smoothEase))
        }
        launch {
            delay(700)
            tagAlpha.animateTo(1f, tween(600, easing = gentleEase))
        }
        launch {
            delay(900)
            barAlpha.animateTo(1f, tween(500, easing = gentleEase))
            progress.animateTo(1f, tween(1600, easing = softEase))
        }
        launch {
            delay(200)
            flagAlpha.animateTo(1f, tween(700, easing = gentleEase))
            flagSlide.animateTo(0f, tween(700, easing = smoothEase))
        }

        // total splash duration
        delay(3000)
        onFinished()
    }

    // ── palette ─────────────────────────────────────────────────
    val topColor    = Color(0xFF1E1B4B)
    val midColor    = Color(0xFF4F46E5)
    val bottomColor = Color(0xFF0EA5E9)
    val accent      = Color(0xFFC7D2FE)
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
        // floating particles
        ParticleField(rotation = particleRot, color = white)

        // soft radial glow behind logo
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-52).dp)
                .size(340.dp)
                .scale(pulseScale)
                .alpha(glowAlpha.value * pulseAlpha)
                .blur(70.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.85f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // ── main content ────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // logo card
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(logoScale.value)
                    .rotate(logoRotate.value)
                    .alpha(logoAlpha.value)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                white.copy(alpha = 0.28f),
                                white.copy(alpha = 0.10f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                )
                Icon(
                    imageVector = Icons.Default.Apartment,
                    contentDescription = null,
                    tint = white,
                    modifier = Modifier.size(66.dp)
                )
            }

            Spacer(Modifier.height(36.dp))

            // title
            Text(
                text = "Room Rent & Utility",
                color = white,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.22f),
                        offset = Offset(0f, 2f),
                        blurRadius = 14f
                    )
                ),
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .offset(y = textSlide.value.dp)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Offline Meter & Rent Notebook",
                color = whiteSoft,
                fontSize = 13.sp,
                letterSpacing = 0.6.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(tagAlpha.value)
            )

            Spacer(Modifier.height(56.dp))

            // progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(3.dp)
                    .alpha(barAlpha.value)
                    .clip(RoundedCornerShape(2.dp))
                    .background(white.copy(alpha = 0.18f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.value)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(listOf(white, accent))
                        )
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Loading your data…",
                color = whiteMuted,
                fontSize = 11.sp,
                letterSpacing = 0.4.sp,
                modifier = Modifier.alpha(barAlpha.value)
            )
        }

        // ── bottom signature with Nepal flag ────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .alpha(flagAlpha.value)
                .offset(y = flagSlide.value.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.nepal_flag),
                contentDescription = "Nepal flag",
                modifier = Modifier
                    .size(width = 34.dp, height = 42.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Made in Nepal",
                color = white,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.4.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "by Biplap Neupane",
                color = accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Version 1.0",
                color = whiteMuted,
                fontSize = 9.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/* ────────────────────────────────────────────────────────────────
   Ambient floating particles
   ──────────────────────────────────────────────────────────────── */
@Composable
private fun ParticleField(rotation: Float, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .rotate(rotation),
        contentAlignment = Alignment.Center
    ) {
        val count = 14
        val radius = 260f
        for (i in 0 until count) {
            val angle = (i * (360f / count)) * (Math.PI / 180.0)
            val x = (cos(angle) * radius).toFloat()
            val y = (sin(angle) * radius).toFloat()
            val size = when (i % 3) {
                0 -> 6.dp
                1 -> 4.dp
                else -> 3.dp
            }
            val alpha = when (i % 3) {
                0 -> 0.32f
                1 -> 0.22f
                else -> 0.16f
            }
            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size(size)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
        }
    }
}