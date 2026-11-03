package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MalayGold
import com.example.ui.theme.MalayGoldLight
import com.example.ui.theme.MalayGoldSubtle
import com.example.ui.theme.ToskaAccent
import com.example.ui.theme.ToskaBright
import com.example.ui.theme.ToskaDark
import com.example.ui.theme.ToskaDarkMedium
import com.example.ui.theme.ToskaTeal

/**
 * Overlay penutup area kosong dengan latar warna toska gelap dan motif Melayu.
 * Intercepts touches on the decorative background while providing responsive, interactive
 * action buttons with active color changes when pressed.
 */
@Composable
fun MalayMotifOverlay(
    isTop: Boolean,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    onRefreshClick: (() -> Unit)? = null,
    onLoginClick: (() -> Unit)? = null
) {
    // Infinite rotation animation for refresh icon when refreshing/loading
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refresh_angle"
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                // Intercept and consume all touch and gesture events so clicks do not affect the underlying WebView
                awaitEachGesture {
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    } while (event.changes.any { it.pressed })
                }
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isTop) {
                        listOf(
                            ToskaDark,
                            ToskaDarkMedium,
                            Color(0xFF062828)
                        )
                    } else {
                        listOf(
                            Color(0xFF062828),
                            ToskaDarkMedium,
                            ToskaDark
                        )
                    }
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawMalayMotif(isTop = isTop)
        }

        // Top right action buttons (Login & Refresh) if on top bar
        if (isTop && (onRefreshClick != null || onLoginClick != null)) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Login Google Account button with interactive press feedback
                if (onLoginClick != null) {
                    MalayTopActionButton(
                        icon = Icons.Default.AccountCircle,
                        contentDescription = "Login Akun Google",
                        testTag = "top_login_button",
                        onClick = onLoginClick
                    )
                }

                // Refresh button with interactive press feedback & rotation
                if (onRefreshClick != null) {
                    MalayTopActionButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Muat Ulang Halaman",
                        testTag = "top_refresh_button",
                        iconRotation = if (isRefreshing) rotationAngle else 0f,
                        onClick = onRefreshClick
                    )
                }
            }
        }
    }
}

/**
 * Interactive action button with prominent color changes and tactile visual feedback when pressed.
 */
@Composable
fun MalayTopActionButton(
    icon: ImageVector,
    contentDescription: String,
    testTag: String,
    modifier: Modifier = Modifier,
    iconRotation: Float = 0f,
    isTransparent: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth color and scale animations for tactile response
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "btn_scale"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = when {
            isPressed -> Color.White
            isTransparent -> MalayGold.copy(alpha = 0.20f)
            else -> MalayGold.copy(alpha = 0.7f)
        },
        animationSpec = tween(durationMillis = 100),
        label = "btn_border"
    )

    val iconTint by animateColorAsState(
        targetValue = when {
            isPressed -> Color(0xFF041E1E)
            isTransparent -> MalayGold.copy(alpha = 0.35f)
            else -> MalayGold
        },
        animationSpec = tween(durationMillis = 100),
        label = "btn_icon_tint"
    )

    Box(
        modifier = modifier
            .size(38.dp)
            .scale(animatedScale)
            .clip(CircleShape)
            .background(
                brush = when {
                    isPressed -> {
                        // Bright vibrant golden color when pressed
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFE082),
                                MalayGold,
                                MalayGoldLight
                            )
                        )
                    }
                    isTransparent -> {
                        // Discreet transparent state when controls are hidden
                        Brush.radialGradient(
                            colors = listOf(
                                ToskaDark.copy(alpha = 0.12f),
                                ToskaDark.copy(alpha = 0.18f)
                            )
                        )
                    }
                    else -> {
                        // Normal state: Elegant semi-translucent Toska / Malay Gold
                        Brush.radialGradient(
                            colors = listOf(
                                ToskaAccent.copy(alpha = 0.85f),
                                ToskaDark.copy(alpha = 0.92f)
                            )
                        )
                    }
                },
                shape = CircleShape
            )
            .border(
                border = BorderStroke(
                    width = if (isPressed) 1.8.dp else if (isTransparent) 0.8.dp else 1.2.dp,
                    color = animatedBorderColor
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = MalayGoldLight),
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier
                .size(20.dp)
                .rotate(iconRotation)
        )
    }
}


/**
 * Draws traditional Malay motifs: Pucuk Rebung, Songket Bintang / Tampuk Manggis,
 * and Awan Larat / Siku Keluang borders.
 */
private fun DrawScope.drawMalayMotif(isTop: Boolean) {
    val width = size.width
    val height = size.height

    if (width <= 0f || height <= 0f) return

    // 1. Subtle background Songket weave grid (Tenun Melayu texture)
    val gridSpacing = 16f
    var gx = 0f
    while (gx < width) {
        drawLine(
            color = ToskaAccent.copy(alpha = 0.35f),
            start = Offset(gx, 0f),
            end = Offset(gx + height, height),
            strokeWidth = 0.75f
        )
        drawLine(
            color = ToskaAccent.copy(alpha = 0.35f),
            start = Offset(gx, height),
            end = Offset(gx + height, 0f),
            strokeWidth = 0.75f
        )
        gx += gridSpacing
    }

    // 2. Central Songket Diamonds & Stars (Bunga Tabur / Tampuk Manggis)
    val motifStep = 36f
    val centerY = height * 0.5f
    val starRadius = (height * 0.28f).coerceAtMost(18f)

    var cx = motifStep / 2f
    while (cx < width) {
        // Outer gold diamond
        val diamondPath = Path().apply {
            moveTo(cx, centerY - starRadius)
            lineTo(cx + starRadius, centerY)
            lineTo(cx, centerY + starRadius)
            lineTo(cx - starRadius, centerY)
            close()
        }
        drawPath(
            path = diamondPath,
            color = MalayGoldSubtle,
            style = Fill
        )
        drawPath(
            path = diamondPath,
            color = MalayGold.copy(alpha = 0.8f),
            style = Stroke(width = 1.2f)
        )

        // Inner 8-pointed star / Bunga Cengkeh
        val innerRadius = starRadius * 0.55f
        val petalPath = Path().apply {
            moveTo(cx, centerY - innerRadius)
            lineTo(cx, centerY + innerRadius)
            moveTo(cx - innerRadius, centerY)
            lineTo(cx + innerRadius, centerY)
            val diag = innerRadius * 0.707f
            moveTo(cx - diag, centerY - diag)
            lineTo(cx + diag, centerY + diag)
            moveTo(cx - diag, centerY + diag)
            lineTo(cx + diag, centerY - diag)
        }
        drawPath(
            path = petalPath,
            color = ToskaTeal.copy(alpha = 0.9f),
            style = Stroke(width = 1f, cap = StrokeCap.Round)
        )

        // Center tiny gold jewel dot
        drawCircle(
            color = MalayGoldLight,
            radius = 1.8f,
            center = Offset(cx, centerY)
        )

        cx += motifStep
    }

    // 3. Horizontal Ornamental Bands (Awan Larat & Siku Keluang)
    val bandThickness = 1.5f
    val borderY = if (isTop) height - 1.5f else 1.5f
    val innerBandY = if (isTop) height - (height * 0.28f) else height * 0.28f

    // Outer edge border line (gold)
    drawLine(
        color = MalayGold,
        start = Offset(0f, borderY),
        end = Offset(width, borderY),
        strokeWidth = bandThickness
    )

    // Inner gold-teal accent line
    drawLine(
        color = ToskaTeal.copy(alpha = 0.7f),
        start = Offset(0f, innerBandY),
        end = Offset(width, innerBandY),
        strokeWidth = 1f
    )

    // 4. Pucuk Rebung (Chevron / Bamboo shoots along the boundary facing the web content)
    val rebungWidth = 20f
    val rebungHeight = (height * 0.26f).coerceAtMost(16f)
    val baseY = if (isTop) height else 0f
    val tipY = if (isTop) height - rebungHeight else rebungHeight

    var rx = 0f
    while (rx < width) {
        val rebungPath = Path().apply {
            moveTo(rx, baseY)
            lineTo(rx + (rebungWidth / 2f), tipY)
            lineTo(rx + rebungWidth, baseY)
            close()
        }

        // Fill triangular shoot
        drawPath(
            path = rebungPath,
            color = ToskaBright.copy(alpha = 0.35f),
            style = Fill
        )
        // Outline shoot with Malay Gold
        drawPath(
            path = rebungPath,
            color = MalayGold.copy(alpha = 0.75f),
            style = Stroke(width = 1.1f)
        )

        // Inner decorative spine line
        drawLine(
            color = MalayGoldLight.copy(alpha = 0.9f),
            start = Offset(rx + (rebungWidth / 2f), baseY),
            end = Offset(rx + (rebungWidth / 2f), tipY),
            strokeWidth = 0.8f
        )

        rx += rebungWidth
    }

    // 5. Delicate highlight glow on top edge for depth
    val glowY = if (isTop) 0f else height
    drawLine(
        color = ToskaTeal.copy(alpha = 0.25f),
        start = Offset(0f, glowY),
        end = Offset(width, glowY),
        strokeWidth = 2f
    )
}
