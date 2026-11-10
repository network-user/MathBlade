package com.mathblade.app
import com.mathblade.R

import android.content.Context
import com.mathblade.core.Difficulty
import com.mathblade.core.MathProblem
import com.mathblade.core.Operation

/** Maps stable :core ids to string resources. All visible text lives in strings.xml. */

fun operationNameRes(operation: Operation): Int = when (operation) {
    Operation.ADD -> R.string.operation_add
    Operation.SUB -> R.string.operation_sub
    Operation.MUL -> R.string.operation_mul
    Operation.DIV -> R.string.operation_div
    Operation.REMAINDER -> R.string.operation_remainder
    Operation.POWER -> R.string.operation_power
    Operation.SQRT -> R.string.operation_sqrt
    Operation.PERCENT -> R.string.operation_percent
    Operation.COMBO -> R.string.operation_combo
}

fun difficultyNameRes(difficulty: Difficulty): Int = when (difficulty) {
    Difficulty.EASY -> R.string.difficulty_easy
    Difficulty.MEDIUM -> R.string.difficulty_medium
    Difficulty.HARD -> R.string.difficulty_hard
}

fun regionNameRes(regionId: Int): Int = when (regionId) {
    1 -> R.string.region_outskirts
    2 -> R.string.region_trade_route
    3 -> R.string.region_remainder_tower
    4 -> R.string.region_power_ruins
    5 -> R.string.region_throne_hall
    else -> R.string.region_outskirts
}

fun monsterNameRes(monsterId: String): Int = when (monsterId) {
    "slime_add" -> R.string.monster_slime_add
    "wolf_sub" -> R.string.monster_wolf_sub
    "bat_mul" -> R.string.monster_bat_mul
    "tax_div" -> R.string.monster_tax_div
    "usurer_pct" -> R.string.monster_usurer_pct
    "golem_rem1" -> R.string.monster_golem_rem1
    "golem_rem2" -> R.string.monster_golem_rem2
    "griffin_pow" -> R.string.monster_griffin_pow
    "root_beast" -> R.string.monster_root_beast
    "scribe_combo" -> R.string.monster_scribe_combo
    "ancient_slime" -> R.string.monster_ancient_slime
    "caravan_master" -> R.string.monster_caravan_master
    "remainder_keeper" -> R.string.monster_remainder_keeper
    "chronicler" -> R.string.monster_chronicler
    "archchancellor" -> R.string.monster_archchancellor
    else -> R.string.monster_unknown
}

fun swordNameRes(swordId: String): Int = when (swordId) {
    "rusty" -> R.string.sword_rusty
    "apprentice" -> R.string.sword_apprentice
    "caravan" -> R.string.sword_caravan
    "remainder" -> R.string.sword_remainder
    "archmage" -> R.string.sword_archmage
    else -> R.string.sword_rusty
}

fun swordDescRes(swordId: String): Int = when (swordId) {
    "rusty" -> R.string.sword_rusty_desc
    "apprentice" -> R.string.sword_apprentice_desc
    "caravan" -> R.string.sword_caravan_desc
    "remainder" -> R.string.sword_remainder_desc
    "archmage" -> R.string.sword_archmage_desc
    else -> R.string.sword_rusty_desc
}

fun armorNameRes(armorId: String): Int = when (armorId) {
    "canvas" -> R.string.armor_canvas
    "leather" -> R.string.armor_leather
    "chainmail" -> R.string.armor_chainmail
    else -> R.string.armor_canvas
}

fun armorDescRes(armorId: String): Int = when (armorId) {
    "canvas" -> R.string.armor_canvas_desc
    "leather" -> R.string.armor_leather_desc
    "chainmail" -> R.string.armor_chainmail_desc
    else -> R.string.armor_canvas_desc
}

fun amuletNameRes(amuletId: String?): Int = when (amuletId) {
    "beads" -> R.string.amulet_beads
    "hourglass" -> R.string.amulet_hourglass
    else -> R.string.amulet_none
}

fun amuletDescRes(amuletId: String?): Int = when (amuletId) {
    "beads" -> R.string.amulet_beads_desc
    "hourglass" -> R.string.amulet_hourglass_desc
    else -> R.string.amulet_none
}

fun titleNameRes(titleId: String): Int = when (titleId) {
    "title_apprentice" -> R.string.title_apprentice
    "title_squire" -> R.string.title_squire
    "title_knight" -> R.string.title_knight
    "title_master" -> R.string.title_master
    "title_archmage" -> R.string.title_archmage
    else -> R.string.title_apprentice
}

fun questNameRes(questId: String): Int = when (questId) {
    "main_region_1" -> R.string.quest_main_region_1
    "main_region_2" -> R.string.quest_main_region_2
    "main_region_3" -> R.string.quest_main_region_3
    "main_region_4" -> R.string.quest_main_region_4
    "main_region_5" -> R.string.quest_main_region_5
    "forge_level1" -> R.string.quest_forge_level1
    "herald_daily" -> R.string.quest_herald_daily
    "treasurer_500" -> R.string.quest_treasurer_500
    "mentor_region2_flawless" -> R.string.quest_mentor_region2_flawless
    else -> R.string.quest_main_region_1
}

fun errandNameRes(errandId: String): Int = when (errandId) {
    "defeat_school" -> R.string.errand_defeat_school
    "solve_10" -> R.string.errand_solve_10
    "flawless_battle" -> R.string.errand_flawless_battle
    "upgrade_sword" -> R.string.errand_upgrade_sword
    "finish_daily" -> R.string.errand_finish_daily
    else -> R.string.errand_solve_10
}

fun contractNameRes(contractId: String): Int = when (contractId) {
    "streak15" -> R.string.contract_streak15
    "fast10easy" -> R.string.contract_fast10easy
    "duel_boss" -> R.string.contract_duel_boss
    else -> R.string.contract_streak15
}

/**
 * Renders a problem from numbers and operation only.
 * :app builds the text; :core holds no Russian phrases.
 */
fun formatProblem(context: Context, problem: MathProblem): String {
    val o = problem.operands
    return when (problem.operation) {
        Operation.ADD -> context.getString(R.string.problem_add, o[0], o[1])
        Operation.SUB -> context.getString(R.string.problem_sub, o[0], o[1])
        Operation.MUL -> context.getString(R.string.problem_mul, o[0], o[1])
        Operation.DIV -> context.getString(R.string.problem_div, o[0], o[1])
        Operation.REMAINDER -> context.getString(R.string.problem_remainder, o[0], o[1])
        Operation.POWER -> when (problem.exponent) {
            3 -> context.getString(R.string.problem_power3, o[0])
            else -> context.getString(R.string.problem_power2, o[0])
        }
        Operation.SQRT -> context.getString(R.string.problem_sqrt, o[0])
        Operation.PERCENT -> context.getString(R.string.problem_percent, o[0], o[1])
        Operation.COMBO -> if (problem.innerSubtract) {
            context.getString(R.string.problem_combo_sub, o[0], o[1], o[2])
        } else {
            context.getString(R.string.problem_combo_add, o[0], o[1], o[2])
        }
    }
}
