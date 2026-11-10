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

    companion object {
        fun fromId(id: String): Operation? = entries.find { it.id == id }
    }
}
