package com.mathblade.app
import com.mathblade.R

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mathblade.core.CombatAnimationState

private val Ink = Color(0xFF1A1420)
private val Paper = Color(0xFFF2EAD8)
private val Skin = Color(0xFFE8C39A)
private val SkinShade = Color(0xFFC48A62)
private val Boot = Color(0xFF2A2118)

enum class PixelPlace { CAMP, MEADOW, ROAD, TOWER, RUIN, THRONE }

fun pixelPlaceFor(imageRes: Int): PixelPlace = when (imageRes) {
    R.drawable.bg_region1 -> PixelPlace.MEADOW
    R.drawable.bg_region2 -> PixelPlace.ROAD
    R.drawable.bg_region3 -> PixelPlace.TOWER
    R.drawable.bg_region4 -> PixelPlace.RUIN
    R.drawable.bg_region5 -> PixelPlace.THRONE
    else -> PixelPlace.CAMP
}

@Composable
fun PixelBackdrop(
    place: PixelPlace,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val sky = when (place) {
        PixelPlace.CAMP -> Color(0xFF243044)
        PixelPlace.MEADOW -> Color(0xFF3A6B8C)
        PixelPlace.ROAD -> Color(0xFF8A5A3A)
        PixelPlace.TOWER -> Color(0xFF2C2448)
        PixelPlace.RUIN -> Color(0xFF4A3030)
        PixelPlace.THRONE -> Color(0xFF1A1428)
    }
    val skyLight = when (place) {
        PixelPlace.CAMP -> Color(0xFF3E5670)
        PixelPlace.MEADOW -> Color(0xFF7EB0C8)
        PixelPlace.ROAD -> Color(0xFFD08A58)
        PixelPlace.TOWER -> Color(0xFF4A3C78)
        PixelPlace.RUIN -> Color(0xFF8A5048)
        PixelPlace.THRONE -> Color(0xFF3A2858)
    }
    val ground = when (place) {
        PixelPlace.CAMP, PixelPlace.MEADOW -> Color(0xFF3E6B38)
        PixelPlace.ROAD -> Color(0xFF6A5038)
        PixelPlace.TOWER -> Color(0xFF3A3450)
        PixelPlace.RUIN -> Color(0xFF4A3830)
        PixelPlace.THRONE -> Color(0xFF2A2438)
    }
    val groundDark = when (place) {
        PixelPlace.CAMP, PixelPlace.MEADOW -> Color(0xFF2C4E28)
        PixelPlace.ROAD -> Color(0xFF4E3A28)
        PixelPlace.TOWER -> Color(0xFF282038)
        PixelPlace.RUIN -> Color(0xFF322820)
        PixelPlace.THRONE -> Color(0xFF181420)
    }
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(sky)
            drawRect(skyLight, size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.28f))
            drawCircle(
                color = if (place == PixelPlace.THRONE || place == PixelPlace.TOWER) Color(0xFFE6E0C8) else Color(0xFFF0D080),
                radius = size.minDimension * 0.06f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.78f, size.height * 0.16f)
            )
            val groundTop = size.height * 0.62f
            drawRect(
                ground,
                topLeft = androidx.compose.ui.geometry.Offset(0f, groundTop),
                size = androidx.compose.ui.geometry.Size(size.width, size.height - groundTop)
            )
            drawRect(
                groundDark,
                topLeft = androidx.compose.ui.geometry.Offset(0f, groundTop),
                size = androidx.compose.ui.geometry.Size(size.width, 8f)
            )
            val block = 10f
            var x = 0f
            while (x < size.width) {
                drawRect(
                    color = if (place == PixelPlace.CAMP) Color(0xFF8A5A3A) else Color(0xFF2A2420),
                    topLeft = androidx.compose.ui.geometry.Offset(x + size.width * 0.08f, groundTop - block * 4),
                    size = androidx.compose.ui.geometry.Size(block * 2, block * 4)
                )
                drawRect(
                    color = Color(0xFF3E6B38),
                    topLeft = androidx.compose.ui.geometry.Offset(x + size.width * 0.05f, groundTop - block * 6),
                    size = androidx.compose.ui.geometry.Size(block * 3, block * 2)
                )
                x += size.width * 0.34f
            }
        }
        content()
    }
}

private val HeroIdleA = listOf(
    "......hhhh......",
    ".....hssssh.....",
    ".....hskksh.....",
    ".....hsoosh.....",
    ".....hssssh.....",
    "......hhhh......",
    "..ccccccWWWWW...",
    ".cccccccccccc...",
    ".cccccccccccc...",
    "..cccccccccc....",
    "....ccc..ccc....",
    "....bbb..bbb....",
    "....bbb..bbb...."
)

private val HeroIdleB = listOf(
    "......hhhh......",
    ".....hssssh.....",
    ".....hskksh.....",
    ".....hsoosh.....",
    ".....hssssh.....",
    "......hhhh......",
    "..ccccccWWWWW...",
    ".cccccccccccc...",
    ".cccccccccccc...",
    "..cccccccccc....",
    "...ccc....ccc...",
    "...bbb....bbb...",
    "...bbb....bbb..."
)

private val HeroAttack = listOf(
    "......hhhh......",
    ".....hssssh.....",
    ".....hskksh.....",
    ".....hsoosh.....",
    ".....hssssh.....",
    "......hhhh......",
    "..ccccccccWWW...",
    ".cccccccccWWW...",
    ".cccccccccccc...",
    "..cccccccccc....",
    "....ccc..ccc....",
    "....bbb..bbb....",
    "....bbb..bbb...."
)

private val Slime = listOf(
    "............",
    "...gggggg...",
    "..gggggggg..",
    ".ggwwggwwgg.",
    ".ggkkggkkgg.",
    ".gggggggggg.",
    "..gggggggg..",
    "...gggggg...",
    "....gggg...."
)

private val Wolf = listOf(
    "....eeeeee..",
    "...eeeeeeee.",
    "..eeweeeweee",
    "..eekeeekeee",
    ".eeeeeeeeeee",
    ".eeeeeeeeeee",
    "..eee..eee..",
    "..eee..eee.."
)

private val Bat = listOf(
    "ww........ww",
    "www......www",
    ".www.kk.www.",
    "..wwkkkkww..",
    "...wkookw...",
    "...wkkkkww..",
    "..www..www.."
)

private val Hood = listOf(
    "...hhhhhh...",
    "..hhsssshh..",
    "..hskooksh..",
    "..hssssssh..",
    "...hhhhhh...",
    "..cccccccc..",
    ".cccccccccc.",
    "..ccc..ccc..",
    "..bbb..bbb.."
)

private val Golem = listOf(
    "..rrrrrrrr..",
    ".rrwwrrwwrr.",
    ".rrkkrrkkrr.",
    ".rrrrrrrrrr.",
    "rrrrrrrrrrrr",
    ".rrrrrrrrrr.",
    "..rrr..rrr..",
    "..rrr..rrr.."
)

private val Bird = listOf(
    "......yyyy..",
    "....yyyyyy..",
    "..yyyyykyy..",
    "yyyyyyyyyyy.",
    ".yyyyyyyyy..",
    "..yyyyyyy...",
    "...yy..yy..."
)

private val Beast = listOf(
    "..gggggggg..",
    ".ggwwggwwgg.",
    ".ggkkggkkgg.",
    "gggggggggggg",
    ".gggggggggg.",
    "..ggg..ggg..",
    ".ggg....ggg."
)

private val Crown = listOf(
    "g.g.g.g.g.g.",
    "gggggggggggg",
    ".gggggggggg."
)

private fun clothColor(armorId: String): Color = when (armorId) {
    "leather" -> Color(0xFF8A5A32)
    "chainmail" -> Color(0xFF9AA4B2)
    else -> Color(0xFF6E5A3A)
}

private fun swordColor(swordId: String): Color = when (swordId) {
    "apprentice" -> Color(0xFFD5DDE6)
    "caravan" -> Color(0xFFE6B84A)
    "remainder" -> Color(0xFF6EC8C8)
    "archmage" -> Color(0xFFB48CFF)
    else -> Color(0xFFB56848)
}

private fun heroPalette(armorId: String, swordId: String): Map<Char, Color> {
    val cloth = clothColor(armorId)
    return mapOf(
        'h' to cloth.copy(alpha = 1f).let { Color(0xFF3A2E22) },
        's' to SkinShade,
        'k' to Skin,
        'o' to Ink,
        'c' to cloth,
        'b' to Boot,
        'W' to swordColor(swordId)
    )
}

private fun monsterArt(monsterId: String): Pair<List<String>, Map<Char, Color>> {
    val boss = monsterId == "ancient_slime" || monsterId == "caravan_master" ||
        monsterId == "remainder_keeper" || monsterId == "chronicler" ||
        monsterId == "archchancellor"
    val (rows, palette) = when (monsterId) {
        "slime_add", "ancient_slime" -> Slime to mapOf(
            'g' to Color(0xFF3E9A48), 'w' to Paper, 'k' to Ink
        )
        "wolf_sub" -> Wolf to mapOf(
            'e' to Color(0xFF6A4630), 'w' to Paper, 'k' to Ink
        )
        "bat_mul" -> Bat to mapOf(
            'w' to Color(0xFF3A3458), 'k' to Color(0xFF6A5A88), 'o' to Color(0xFFE6B84A)
        )
        "tax_div" -> Hood to mapOf(
            'h' to Color(0xFF2A3A58), 's' to SkinShade, 'k' to Skin, 'o' to Ink,
            'c' to Color(0xFF2A3A58), 'b' to Boot
        )
        "usurer_pct" -> Hood to mapOf(
            'h' to Color(0xFF6A2840), 's' to SkinShade, 'k' to Skin, 'o' to Ink,
            'c' to Color(0xFF6A2840), 'b' to Boot
        )
        "golem_rem1", "golem_rem2", "remainder_keeper" -> Golem to mapOf(
            'r' to Color(0xFF7A8494), 'w' to Paper, 'k' to Color(0xFFE6B84A)
        )
        "griffin_pow", "chronicler" -> Bird to mapOf(
            'y' to Color(0xFFC48A32), 'k' to Ink
        )
        "root_beast" -> Beast to mapOf(
            'g' to Color(0xFF2E5A30), 'w' to Paper, 'k' to Ink
        )
        "scribe_combo", "archchancellor", "caravan_master" -> Hood to mapOf(
            'h' to Color(0xFF3A2858), 's' to SkinShade, 'k' to Skin, 'o' to Ink,
            'c' to Color(0xFF3A2858), 'b' to Boot
        )
        else -> Slime to mapOf('g' to Color(0xFF3E9A48), 'w' to Paper, 'k' to Ink)
    }
    return if (boss) Crown + rows to (palette + ('g' to Color(0xFFE6B84A))) else rows to palette
}

@Composable
fun PixelActor(
    rows: List<String>,
    palette: Map<Char, Color>,
    modifier: Modifier = Modifier,
    pixel: Dp = 7.dp,
    flip: Boolean = false
) {
    val width = (rows.maxOfOrNull { it.length } ?: 1)
    Canvas(
        modifier = modifier
            .size(pixel * width, pixel * rows.size)
            .graphicsLayer { scaleX = if (flip) -1f else 1f }
    ) {
        val cell = this.size.width / width
        rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, ch ->
                val color = palette[ch] ?: return@forEachIndexed
                drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(x * cell, y * cell),
                    size = androidx.compose.ui.geometry.Size(cell + 0.5f, cell + 0.5f)
                )
            }
        }
    }
}

@Composable
fun PixelHero(
    armorId: String,
    swordId: String,
    attacking: Boolean,
    modifier: Modifier = Modifier,
    pixel: Dp = 7.dp
) {
    val idle = rememberInfiniteTransition(label = "hero-idle")
    val step by idle.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(480), RepeatMode.Reverse),
        label = "step"
    )
    val rows = if (attacking) HeroAttack else if (step < 0.5f) HeroIdleA else HeroIdleB
    PixelActor(rows, heroPalette(armorId, swordId), modifier, pixel)
}

@Composable
fun PixelMonster(
    monsterId: String,
    modifier: Modifier = Modifier,
    pixel: Dp = 8.dp
) {
    val (rows, palette) = monsterArt(monsterId)
    PixelActor(rows, palette, modifier, pixel, flip = true)
}

@Composable
fun BattleArena(
    monsterId: String,
    armorId: String,
    swordId: String,
    animation: CombatAnimationState,
    showHit: Boolean,
    modifier: Modifier = Modifier
) {
    val attacking = animation == CombatAnimationState.HERO_ATTACK ||
        animation == CombatAnimationState.ENEMY_DYING
    val hurt = animation == CombatAnimationState.HERO_HURT
    val dying = animation == CombatAnimationState.ENEMY_DYING
    val lunge by animateFloatAsState(
        targetValue = if (attacking) 36f else 0f,
        animationSpec = tween(180),
        label = "lunge"
    )
    val shake by animateFloatAsState(
        targetValue = if (hurt) 1f else 0f,
        animationSpec = tween(80),
        label = "shake"
    )
    val knock by animateFloatAsState(
        targetValue = if (showHit) 18f else 0f,
        animationSpec = tween(160),
        label = "knock"
    )
    val sink by animateFloatAsState(
        targetValue = if (dying) 28f else 0f,
        animationSpec = tween(500),
        label = "sink"
    )
    val fade by animateFloatAsState(
        targetValue = if (dying) 0.15f else 1f,
        animationSpec = tween(500),
        label = "fade"
    )
    Box(modifier = modifier.fillMaxSize()) {
        PixelHero(
            armorId = armorId,
            swordId = swordId,
            attacking = attacking,
            pixel = 8.dp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (16f + lunge + if (hurt) 8f * shake else 0f).dp, y = (-8).dp)
        )
        PixelMonster(
            monsterId = monsterId,
            pixel = 9.dp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-16f + knock).dp, y = (-8f + sink).dp)
                .graphicsLayer { alpha = fade }
        )
        if (showHit && !dying) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            ) {
                drawCircle(Color.White.copy(alpha = 0.55f), radius = size.minDimension / 2f)
            }
        }
    }
}

@Composable
fun PixelItem(imageRes: Int?, modifier: Modifier = Modifier) {
    val monsterId = when (imageRes) {
        R.drawable.slime_add -> "slime_add"
        R.drawable.wolf_sub -> "wolf_sub"
        R.drawable.bat_mul -> "bat_mul"
        R.drawable.tax_div -> "tax_div"
        R.drawable.usurer_pct -> "usurer_pct"
        R.drawable.golem_rem1 -> "golem_rem1"
        R.drawable.golem_rem2 -> "golem_rem2"
        R.drawable.griffin_pow -> "griffin_pow"
        R.drawable.root_beast -> "root_beast"
        R.drawable.scribe_combo -> "scribe_combo"
        R.drawable.ancient_slime -> "ancient_slime"
        R.drawable.caravan_master -> "caravan_master"
        R.drawable.remainder_keeper -> "remainder_keeper"
        R.drawable.chronicler -> "chronicler"
        R.drawable.archchancellor -> "archchancellor"
        else -> null
    }
    val swordId = when (imageRes) {
        R.drawable.sword_apprentice -> "apprentice"
        R.drawable.sword_caravan -> "caravan"
        R.drawable.sword_remainder -> "remainder"
        R.drawable.sword_archmage -> "archmage"
        R.drawable.sword_rusty -> "rusty"
        else -> null
    }
    val armorId = when (imageRes) {
        R.drawable.armor_leather -> "leather"
        R.drawable.armor_chainmail -> "chainmail"
        R.drawable.armor_canvas -> "canvas"
        else -> null
    }
    Box(modifier, contentAlignment = Alignment.Center) {
        when {
            monsterId != null -> PixelMonster(monsterId, pixel = 6.dp)
            swordId != null -> PixelHero("canvas", swordId, attacking = true, pixel = 5.dp)
            armorId != null -> PixelHero(armorId, "rusty", attacking = false, pixel = 5.dp)
            imageRes == R.drawable.amulet_beads -> PixelActor(
                listOf("..gg..", ".gggg.", "..gg..", "...g.."),
                mapOf('g' to Color(0xFFE6B84A)),
                pixel = 8.dp
            )
            imageRes == R.drawable.amulet_hourglass -> PixelActor(
                listOf(".gggg.", "..gg..", "..gg..", ".gggg."),
                mapOf('g' to Color(0xFFE6D090)),
                pixel = 8.dp
            )
            imageRes == R.drawable.blacksmith_portrait -> PixelHero("leather", "apprentice", false, pixel = 5.dp)
            imageRes == R.drawable.herald_portrait -> PixelHero("canvas", "caravan", false, pixel = 5.dp)
            imageRes == R.drawable.treasurer_portrait -> PixelHero("chainmail", "caravan", false, pixel = 5.dp)
            else -> PixelHero("canvas", "rusty", false, pixel = 5.dp)
        }
    }
}
