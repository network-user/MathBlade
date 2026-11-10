package com.mathblade.app
import com.mathblade.R

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Full-screen background picture (bg_* are opaque RGB images, shown
 * full-screen as intended). A dark scrim keeps text readable.
 */
@Composable
fun FullscreenBackground(imageRes: Int, content: @Composable () -> Unit) {
    PixelBackdrop(pixelPlaceFor(imageRes), modifier = Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalContentColor provides Color(0xFFF2EAD8)) {
            content()
        }
    }
}

/**
 * An opaque picture inside a rounded card, cropped, on a dark surface.
 * Never used as a transparent sprite over a background.
 */
@Composable
fun FramedImage(
    imageRes: Int?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A14))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.coerceAtMost(96.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (imageRes != null) {
                PixelItem(imageRes)
            } else {
                SilhouetteBox(modifier = Modifier.size(72.dp))
            }
        }
    }
}

/** Procedural fallback when a drawable is missing: a dark figure silhouette. */
@Composable
fun SilhouetteBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(Color(0xFF1E1A14)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(96.dp)) {
            val cx = size.width / 2f
            drawCircle(
                color = Color(0xFF3A3226),
                radius = size.width * 0.16f,
                center = Offset(cx, size.height * 0.22f)
            )
            drawOval(
                color = Color(0xFF3A3226),
                topLeft = Offset(cx - size.width * 0.22f, size.height * 0.42f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.44f, size.height * 0.52f)
            )
        }
    }
}

@Composable
fun HeartsRow(lives: Int, maxLives: Int, modifier: Modifier = Modifier) {
    val heart = stringResource(R.string.heart_symbol)
    Row(modifier = modifier) {
        repeat(maxLives) { index ->
            Text(
                text = heart,
                fontSize = 26.sp,
                color = if (index < lives) Color(0xFFC0392B) else Color(0xFF55504A)
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
fun HpBar(current: Int, max: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { if (max > 0) current.toFloat() / max.toFloat() else 0f },
            modifier = Modifier.fillMaxWidth().height(10.dp),
            color = Color(0xFFC0392B),
            trackColor = Color(0xFF3A3226)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.battle_enemy_hp, current, max),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun GoldXpRow(gold: Int, xp: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        Text(
            text = stringResource(R.string.common_gold, gold),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.common_xp, xp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MenuCard(
    titleRes: Int,
    descRes: Int,
    imageRes: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A14))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageRes != null) {
                PixelItem(imageRes, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(descRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFA99B7E)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(vertical = 8.dp)
    )
}
