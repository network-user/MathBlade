package com.mathblade.core

/**
 * Math operation ("school" of a monster). The [id] is a stable, language
 * neutral identifier; all user-visible text lives in :app strings.
 */
enum class Operation(val id: String) {
    ADD("add"),
    SUB("sub"),
    MUL("mul"),
    DIV("div"),
    REMAINDER("remainder"),
    POWER("power"),
    SQRT("sqrt"),
    PERCENT("percent"),
    COMBO("combo");

    /**
     * Extra damage factor for choosing this school.
     * Addition and subtraction stay at 1. Harder schools pay more.
     */
    fun damageCoefficient(): Double = when (this) {
        ADD, SUB -> 1.0
        MUL, DIV -> 1.3
        PERCENT, REMAINDER -> 1.5
        POWER, SQRT -> 1.7
        COMBO -> 2.0
    }

    companion object {
        fun fromId(id: String): Operation? = entries.find { it.id == id }
    }
}
