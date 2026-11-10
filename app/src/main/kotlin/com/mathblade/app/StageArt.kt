package com.mathblade.app

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mathblade.core.CombatAnimationState
import com.mathblade.core.Operation

private val Night = Color(0xFF141C2B)
private val Dusk = Color(0xFF24344A)
private val Grass = Color(0xFF2E5A3A)
private val GrassDark = Color(0xFF1E3C28)
private val Ink = Color(0xFF1A1420)
private val Skin = Color(0xFFE6C29A)
private val Paper = Color(0xFFF4EBD8)

private fun cloth(armorId: String) = when (armorId) {
    "leather" -> Color(0xFF8A5430)
    "chainmail" -> Color(0xFFB7C0CC)
    else -> Color(0xFF6E5340)
}

private fun blade(swordId: String) = when (swordId) {
    "apprentice" -> Color(0xFFE7EEF4)
    "caravan" -> Color(0xFFE6B84A)
    "remainder" -> Color(0xFF7ED0D0)
    "archmage" -> Color(0xFFC4A0FF)
    else -> Color(0xFFC46A42)
}

@Composable
fun GameAtmosphere(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        Canvas(Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF1A2740), Dusk, Color(0xFF1A2830), GrassDark)
                )
            )
            drawCircle(
                color = Color(0xFFF0D48A),
                radius = size.minDimension * 0.07f,
                center = Offset(size.width * 0.82f, size.height * 0.16f)
            )
            drawCircle(
                color = Color(0xFFFFF4C8).copy(alpha = 0.25f),
                radius = size.minDimension * 0.11f,
                center = Offset(size.width * 0.82f, size.height * 0.16f)
            )
            val hill = Path().apply {
                moveTo(0f, size.height * 0.72f)
                quadraticBezierTo(size.width * 0.35f, size.height * 0.58f, size.width, size.height * 0.7f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(hill, Grass)
            drawPath(hill, GrassDark.copy(alpha = 0.35f))
        }
        content()
    }
}

@Composable
fun KnightFigure(
    armorId: String,
    swordId: String,
    attacking: Boolean,
    modifier: Modifier = Modifier
) {
    val tunic = cloth(armorId)
    val sword = blade(swordId)
    Canvas(modifier.size(150.dp, 180.dp)) {
        val w = size.width
        val h = size.height
        drawOval(
            color = Color.Black.copy(alpha = 0.28f),
            topLeft = Offset(w * 0.22f, h * 0.88f),
            size = Size(w * 0.5f, h * 0.08f)
        )
        drawRoundRect(
            color = tunic.copy(alpha = 0.85f),
            topLeft = Offset(w * 0.18f, h * 0.42f),
            size = Size(w * 0.34f, h * 0.28f),
            cornerRadius = CornerRadius(18f, 18f)
        )
        drawRoundRect(
            color = tunic,
            topLeft = Offset(w * 0.28f, h * 0.34f),
            size = Size(w * 0.36f, h * 0.4f),
            cornerRadius = CornerRadius(16f, 16f)
        )
        drawRect(BootColor, Offset(w * 0.3f, h * 0.72f), Size(w * 0.12f, h * 0.16f))
        drawRect(BootColor, Offset(w * 0.5f, h * 0.72f), Size(w * 0.12f, h * 0.16f))
        drawCircle(Skin, radius = w * 0.13f, center = Offset(w * 0.46f, h * 0.24f))
        drawArc(
            color = Ink,
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = true,
            topLeft = Offset(w * 0.3f, h * 0.08f),
            size = Size(w * 0.32f, w * 0.28f)
        )
        drawCircle(Ink, radius = 3.5f, center = Offset(w * 0.52f, h * 0.24f))
        val reach = if (attacking) w * 0.42f else w * 0.22f
        drawLine(
            color = Color(0xFF5C4632),
            start = Offset(w * 0.58f, h * 0.42f),
            end = Offset(w * 0.58f + reach * 0.35f, h * 0.28f),
            strokeWidth = 7f
        )
        drawRoundRect(
            color = sword,
            topLeft = Offset(w * 0.62f + if (attacking) reach * 0.2f else 0f, h * 0.12f),
            size = Size(w * 0.08f, if (attacking) h * 0.42f else h * 0.34f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRect(
            color = Color(0xFFE6B84A),
            topLeft = Offset(w * 0.58f + if (attacking) reach * 0.15f else 0f, h * 0.42f),
            size = Size(w * 0.16f, h * 0.035f)
        )
        drawRoundRect(
            color = Ink,
            topLeft = Offset(w * 0.08f, h * 0.4f),
            size = Size(w * 0.22f, h * 0.26f),
            cornerRadius = CornerRadius(20f, 20f),
            style = Stroke(width = 8f)
        )
    }
}

private val BootColor = Color(0xFF241910)

@Composable
fun CreatureFigure(
    monsterId: String,
    operation: Operation,
    hurt: Boolean,
    modifier: Modifier = Modifier
) {
    val tint = when (operation) {
        Operation.ADD -> Color(0xFF3EAE52)
        Operation.SUB -> Color(0xFF8A5A3C)
        Operation.MUL -> Color(0xFF5A4A88)
        Operation.DIV -> Color(0xFF3A5A88)
        Operation.PERCENT -> Color(0xFF8A3458)
        Operation.REMAINDER -> Color(0xFF7E8A9A)
        Operation.POWER -> Color(0xFFD09030)
        Operation.SQRT -> Color(0xFF2E6A40)
        Operation.COMBO -> Color(0xFF4A3870)
    }
    val boss = monsterId == "ancient_slime" || monsterId == "caravan_master" ||
        monsterId == "remainder_keeper" || monsterId == "chronicler" ||
        monsterId == "archchancellor"
    Canvas(modifier.size(if (boss) 170.dp else 140.dp, if (boss) 160.dp else 130.dp)) {
        val w = size.width
        val h = size.height
        drawOval(
            Color.Black.copy(alpha = 0.25f),
            Offset(w * 0.15f, h * 0.86f),
            Size(w * 0.7f, h * 0.1f)
        )
        when (operation) {
            Operation.ADD -> {
                drawOval(tint, Offset(w * 0.12f, h * 0.28f), Size(w * 0.76f, h * 0.58f))
                drawOval(tint.copy(alpha = 0.55f), Offset(w * 0.28f, h * 0.34f), Size(w * 0.28f, h * 0.16f))
                eye(w * 0.36f, h * 0.5f, w)
                eye(w * 0.62f, h * 0.5f, w)
            }
            Operation.SUB -> {
                val body = Path().apply {
                    moveTo(w * 0.15f, h * 0.62f)
                    lineTo(w * 0.28f, h * 0.38f)
                    lineTo(w * 0.7f, h * 0.36f)
                    lineTo(w * 0.9f, h * 0.55f)
                    lineTo(w * 0.72f, h * 0.78f)
                    lineTo(w * 0.2f, h * 0.8f)
                    close()
                }
                drawPath(body, tint)
                drawCircle(tint, w * 0.1f, Offset(w * 0.24f, h * 0.32f))
                drawCircle(tint, w * 0.08f, Offset(w * 0.38f, h * 0.26f))
                eye(w * 0.62f, h * 0.5f, w)
            }
            Operation.MUL -> {
                drawOval(tint, Offset(w * 0.32f, h * 0.28f), Size(w * 0.36f, h * 0.42f))
                val wing = Path().apply {
                    moveTo(w * 0.38f, h * 0.42f)
                    lineTo(w * 0.02f, h * 0.22f)
                    lineTo(w * 0.08f, h * 0.55f)
                    close()
                }
                val wing2 = Path().apply {
                    moveTo(w * 0.62f, h * 0.42f)
                    lineTo(w * 0.98f, h * 0.22f)
                    lineTo(w * 0.92f, h * 0.55f)
                    close()
                }
                drawPath(wing, tint)
                drawPath(wing2, tint)
                eye(w * 0.42f, h * 0.42f, w)
                eye(w * 0.56f, h * 0.42f, w)
            }
            Operation.REMAINDER -> {
                drawRoundRect(tint, Offset(w * 0.22f, h * 0.22f), Size(w * 0.56f, h * 0.58f), CornerRadius(12f, 12f))
                drawRect(tint.copy(alpha = 0.7f), Offset(w * 0.18f, h * 0.48f), Size(w * 0.64f, h * 0.12f))
                eye(w * 0.4f, h * 0.4f, w)
                eye(w * 0.6f, h * 0.4f, w)
            }
            Operation.POWER -> {
                val wing = Path().apply {
                    moveTo(w * 0.2f, h * 0.55f)
                    quadraticBezierTo(w * 0.15f, h * 0.15f, w * 0.7f, h * 0.32f)
                    lineTo(w * 0.55f, h * 0.58f)
                    close()
                }
                drawPath(wing, tint)
                drawCircle(tint, w * 0.12f, Offset(w * 0.68f, h * 0.4f))
                drawCircle(Color(0xFFE6B84A), w * 0.04f, Offset(w * 0.74f, h * 0.38f))
            }
            else -> {
                drawRoundRect(tint, Offset(w * 0.28f, h * 0.18f), Size(w * 0.44f, h * 0.62f), CornerRadius(28f, 28f))
                drawCircle(Skin, w * 0.1f, Offset(w * 0.5f, h * 0.32f))
                eye(w * 0.46f, h * 0.32f, w)
                drawRect(tint, Offset(w * 0.34f, h * 0.72f), Size(w * 0.1f, h * 0.14f))
                drawRect(tint, Offset(w * 0.56f, h * 0.72f), Size(w * 0.1f, h * 0.14f))
            }
        }
        if (boss) {
            drawRoundRect(
                Color(0xFFE6B84A),
                Offset(w * 0.28f, h * 0.06f),
                Size(w * 0.44f, h * 0.1f),
                CornerRadius(6f, 6f)
            )
        }
        if (hurt) {
            drawCircle(Color.White.copy(alpha = 0.45f), radius = w * 0.28f, center = Offset(w * 0.5f, h * 0.48f))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.eye(x: Float, y: Float, w: Float) {
    drawCircle(Paper, w * 0.045f, Offset(x, y))
    drawCircle(Ink, w * 0.02f, Offset(x, y))
}

@Composable
fun BattleStage(
    monsterId: String,
    operation: Operation,
    armorId: String,
    swordId: String,
    animation: CombatAnimationState,
    showHit: Boolean,
    modifier: Modifier = Modifier
) {
    val idle = rememberInfiniteTransition(label = "stage")
    val bob by idle.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "bob"
    )
    val attacking = animation == CombatAnimationState.HERO_ATTACK || animation == CombatAnimationState.ENEMY_DYING
    val lunge by animateFloatAsState(if (attacking) 28f else 0f, tween(160), label = "lunge")
    val knock by animateFloatAsState(if (showHit) 16f else 0f, tween(160), label = "knock")
    GameAtmosphere(modifier) {
        KnightFigure(
            armorId = armorId,
            swordId = swordId,
            attacking = attacking,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (12f + lunge).dp, y = (bob - 12f).dp)
        )
        CreatureFigure(
            monsterId = monsterId,
            operation = operation,
            hurt = showHit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-8f - knock).dp, y = (bob * 0.4f - 8f).dp)
                .graphicsLayer { alpha = if (animation == CombatAnimationState.ENEMY_DYING) 0.35f else 1f }
        )
    }
}
