package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MalayGold
import com.example.ui.theme.MalayGoldLight
import com.example.ui.theme.ToskaAccent
import com.example.ui.theme.ToskaBright
import com.example.ui.theme.ToskaDark
import com.example.ui.theme.ToskaDarkMedium
import com.example.ui.theme.ToskaTeal
import kotlinx.coroutines.delay

enum class DownloadType {
    AUDIO_WAV,
    AUDIO_GENERAL,
    IMAGE,
    VIDEO,
    DOCUMENT,
    ARCHIVE,
    APK,
    GENERAL
}

data class DownloadSuccessEvent(
    val id: Long = System.currentTimeMillis(),
    val fileName: String,
    val type: DownloadType,
    val locationLabel: String = "Penyimpanan HP"
)

/**
 * Modern, elegant Malay-themed floating Toast & Notification Card for completed downloads across ALL file formats.
 */
@Composable
fun ModernDownloadNotificationBanner(
    event: DownloadSuccessEvent?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(event) {
        if (event != null) {
            delay(4000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = event != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(250)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(200)
        ) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("modern_download_notification_banner")
    ) {
        if (event != null) {
            val (badgeIcon, typeTitle, typeBadgeBg) = when (event.type) {
                DownloadType.AUDIO_WAV -> Triple(
                    Icons.Default.GraphicEq,
                    "Audio WAV (Studio)",
                    Color(0xFF00695C)
                )
                DownloadType.AUDIO_GENERAL -> Triple(
                    Icons.Default.Audiotrack,
                    "Audio / MP3",
                    Color(0xFF004D40)
                )
                DownloadType.IMAGE -> Triple(
                    Icons.Default.Image,
                    "Gambar / Foto",
                    Color(0xFF005B52)
                )
                DownloadType.VIDEO -> Triple(
                    Icons.Default.Videocam,
                    "Video / MP4",
                    Color(0xFF004D40)
                )
                DownloadType.DOCUMENT -> Triple(
                    Icons.Default.Description,
                    "Dokumen / PDF",
                    Color(0xFF1E3A3A)
                )
                DownloadType.ARCHIVE -> Triple(
                    Icons.Default.Folder,
                    "Arsip ZIP / RAR",
                    Color(0xFF0E3A33)
                )
                DownloadType.APK -> Triple(
                    Icons.Default.Android,
                    "Aplikasi APK",
                    Color(0xFF025547)
                )
                DownloadType.GENERAL -> Triple(
                    Icons.Default.DownloadDone,
                    "Berkas Unduhan",
                    Color(0xFF1E3A3A)
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = MalayGold.copy(alpha = 0.35f),
                        spotColor = ToskaBright.copy(alpha = 0.55f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        BorderStroke(
                            width = 1.4.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MalayGoldLight,
                                    ToskaTeal,
                                    MalayGold
                                )
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onDismiss() },
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    ToskaDarkMedium.copy(alpha = 0.96f),
                                    ToskaDark.copy(alpha = 0.98f),
                                    Color(0xFF041E1E)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Glowing decorative icon avatar with checkmark badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                ToskaAccent,
                                                typeBadgeBg,
                                                Color(0xFF021616)
                                            )
                                        )
                                    )
                                    .border(
                                        BorderStroke(1.2.dp, MalayGold),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = badgeIcon,
                                    contentDescription = null,
                                    tint = MalayGoldLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Tiny checkmark status badge on the bottom corner
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00E676))
                                    .border(BorderStroke(1.dp, Color(0xFF041E1E)), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF041E1E),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Text details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Berhasil Diunduh",
                                    color = MalayGoldLight,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.3.sp
                                )

                                // Category pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MalayGold.copy(alpha = 0.22f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = typeTitle,
                                        color = MalayGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = event.fileName,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Tersimpan di ${event.locationLabel}",
                                color = ToskaTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Close button
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

