package com.mathblade.app
import com.mathblade.R

/**
 * Drawable mapping. All PNGs are RGB without an alpha channel, so they
 * must never be used as transparent sprites over a background: UI shows
 * them only inside rounded cards with ContentScale.Crop on a dark
 * surface, and bg_* images full-screen. Unknown ids fall back to a
 * procedural Canvas silhouette (see Ui.kt), so the build never depends
 * on a picture being present.
 */

fun monsterDrawableRes(monsterId: String): Int? = when (monsterId) {
    "slime_add" -> R.drawable.slime_add
    "wolf_sub" -> R.drawable.wolf_sub
    "bat_mul" -> R.drawable.bat_mul
    "tax_div" -> R.drawable.tax_div
    "usurer_pct" -> R.drawable.usurer_pct
    "golem_rem1" -> R.drawable.golem_rem1
    "golem_rem2" -> R.drawable.golem_rem2
    "griffin_pow" -> R.drawable.griffin_pow
    "root_beast" -> R.drawable.root_beast
    "scribe_combo" -> R.drawable.scribe_combo
    "ancient_slime" -> R.drawable.ancient_slime
    "caravan_master" -> R.drawable.caravan_master
    "remainder_keeper" -> R.drawable.remainder_keeper
    "chronicler" -> R.drawable.chronicler
    "archchancellor" -> R.drawable.archchancellor
    else -> null
}

fun swordDrawableRes(swordId: String): Int? = when (swordId) {
    "rusty" -> R.drawable.sword_rusty
    "apprentice" -> R.drawable.sword_apprentice
    "caravan" -> R.drawable.sword_caravan
    "remainder" -> R.drawable.sword_remainder
    "archmage" -> R.drawable.sword_archmage
    else -> null
}

fun armorDrawableRes(armorId: String): Int? = when (armorId) {
    "canvas" -> R.drawable.armor_canvas
    "leather" -> R.drawable.armor_leather
    "chainmail" -> R.drawable.armor_chainmail
    else -> null
}

fun amuletDrawableRes(amuletId: String): Int? = when (amuletId) {
    "beads" -> R.drawable.amulet_beads
    "hourglass" -> R.drawable.amulet_hourglass
    else -> null
}

fun regionBackgroundRes(regionId: Int): Int = when (regionId) {
    1 -> R.drawable.bg_region1
    2 -> R.drawable.bg_region2
    3 -> R.drawable.bg_region3
    4 -> R.drawable.bg_region4
    5 -> R.drawable.bg_region5
    else -> R.drawable.bg_camp
}
