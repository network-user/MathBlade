package com.mathblade.core

import java.time.LocalDate

/**
 * Local save data with a hand-written JSON codec.
 *
 * No serialization library is used (no network in an offline build to
 * fetch one): [serialize] builds JSON by hand and [deserialize] parses
 * it with a small recursive-descent parser below. An unknown [version]
 * or broken JSON never throws: [deserialize] returns the default save
 * with [SaveLoadResult.wasReset] set to true.
 */
data class RunRecord(
    val damage: Long,
    val regionId: Int,
    /** ISO date string, YYYY-MM-DD. */
    val date: String
)

data class WeeklyTournamentState(
    val weekId: String? = null,
    val bestDamage: Long = 0L
)

data class SaveData(
    val version: Int = CURRENT_VERSION,
    val gold: Int = 0,
    val xp: Int = 0,
    val totalDamage: Long = 0L,
    val unlockedRegion: Int = 1,
    val ownedSwords: Set<String> = setOf("rusty"),
    val equippedSword: String = "rusty",
    val forgeLevels: Map<String, Int> = emptyMap(),
    val armorId: String = "canvas",
    val amuletId: String? = null,
    val completedQuests: Set<String> = emptySet(),
    val daily: DailyState = DailyState(),
    val hallOfFame: List<RunRecord> = emptyList(),
    val weeklyTournament: WeeklyTournamentState = WeeklyTournamentState(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalSolved: Int = 0,
    val totalCorrect: Int = 0
) {
    companion object {
        const val CURRENT_VERSION: Int = 1
    }
}

data class SaveLoadResult(
    val save: SaveData,
    val wasReset: Boolean
)

/** Serializes a save to a JSON string. */
fun serialize(save: SaveData): String {
    val sb = StringBuilder()
    sb.append("{")
    fun field(name: String, value: String, first: Boolean = false) {
        if (!first) sb.append(",")
        sb.append("\"").append(name).append("\":").append(value)
    }
    fun str(value: String?): String =
        if (value == null) "null" else "\"" + escapeJson(value) + "\""
    fun strList(values: Collection<String>): String =
        values.joinToString(",", "[", "]") { str(it) }
    fun intMap(values: Map<String, Int>): String =
        values.entries.joinToString(",", "{", "}") { (k, v) -> str(k) + ":" + v }

    field("version", save.version.toString(), first = true)
    field("gold", save.gold.toString())
    field("xp", save.xp.toString())
    field("totalDamage", save.totalDamage.toString())
    field("unlockedRegion", save.unlockedRegion.toString())
    field("ownedSwords", strList(save.ownedSwords))
    field("equippedSword", str(save.equippedSword))
    field("forgeLevels", intMap(save.forgeLevels))
    field("armorId", str(save.armorId))
    field("amuletId", str(save.amuletId))
    field("completedQuests", strList(save.completedQuests))
    val dailyJson = buildString {
        append("{")
        append("\"attemptDate\":").append(str(save.daily.attemptDate?.toString()))
        append(",\"started\":").append(save.daily.started)
        append("}")
    }
    field("daily", dailyJson)
    val fameJson = save.hallOfFame.joinToString(",", "[", "]") { record ->
        "{\"damage\":" + record.damage +
            ",\"regionId\":" + record.regionId +
            ",\"date\":" + str(record.date) + "}"
    }
    field("hallOfFame", fameJson)
    val weeklyJson = "{\"weekId\":" + str(save.weeklyTournament.weekId) +
        ",\"bestDamage\":" + save.weeklyTournament.bestDamage + "}"
    field("weeklyTournament", weeklyJson)
    field("currentStreak", save.currentStreak.toString())
    field("bestStreak", save.bestStreak.toString())
    field("totalSolved", save.totalSolved.toString())
    field("totalCorrect", save.totalCorrect.toString())
    sb.append("}")
    return sb.toString()
}

/**
 * Parses a save. Unknown version or any malformed input returns the
 * default save with wasReset = true; this function never throws.
 */
fun deserialize(json: String): SaveLoadResult {
    return try {
        val parsed = JsonParser(json).parseValue()
        val map = parsed as? Map<*, *> ?: return SaveLoadResult(SaveData(), true)
        val version = (map["version"] as? Number)?.toInt()
        if (version != SaveData.CURRENT_VERSION) {
            return SaveLoadResult(SaveData(), true)
        }
        val dailyMap = map["daily"] as? Map<*, *>
        val daily = DailyState(
            attemptDate = (dailyMap?.get("attemptDate") as? String)?.let { LocalDate.parse(it) },
            started = dailyMap?.get("started") as? Boolean ?: false
        )
        val weeklyMap = map["weeklyTournament"] as? Map<*, *>
        val weekly = WeeklyTournamentState(
            weekId = weeklyMap?.get("weekId") as? String,
            bestDamage = (weeklyMap?.get("bestDamage") as? Number)?.toLong() ?: 0L
        )
        val hallOfFame = (map["hallOfFame"] as? List<*>).orEmpty().mapNotNull { item ->
            val record = item as? Map<*, *> ?: return@mapNotNull null
            RunRecord(
                damage = (record["damage"] as? Number)?.toLong() ?: return@mapNotNull null,
                regionId = (record["regionId"] as? Number)?.toInt() ?: return@mapNotNull null,
                date = record["date"] as? String ?: return@mapNotNull null
            )
        }
        val save = SaveData(
            version = version,
            gold = (map["gold"] as? Number)?.toInt() ?: 0,
            xp = (map["xp"] as? Number)?.toInt() ?: 0,
            totalDamage = (map["totalDamage"] as? Number)?.toLong() ?: 0L,
            unlockedRegion = (map["unlockedRegion"] as? Number)?.toInt() ?: 1,
            ownedSwords = (map["ownedSwords"] as? List<*>)
                .orEmpty().filterIsInstance<String>().toSet().ifEmpty { setOf("rusty") },
            equippedSword = map["equippedSword"] as? String ?: "rusty",
            forgeLevels = (map["forgeLevels"] as? Map<*, *>)
                .orEmpty().entries.mapNotNull { (k, v) ->
                    val key = k as? String ?: return@mapNotNull null
                    val value = (v as? Number)?.toInt() ?: return@mapNotNull null
                    key to value
                }.toMap(),
            armorId = map["armorId"] as? String ?: "canvas",
            amuletId = map["amuletId"] as? String,
            completedQuests = (map["completedQuests"] as? List<*>)
                .orEmpty().filterIsInstance<String>().toSet(),
            daily = daily,
            hallOfFame = hallOfFame,
            weeklyTournament = weekly,
            currentStreak = (map["currentStreak"] as? Number)?.toInt() ?: 0,
            bestStreak = (map["bestStreak"] as? Number)?.toInt() ?: 0,
            totalSolved = (map["totalSolved"] as? Number)?.toInt() ?: 0,
            totalCorrect = (map["totalCorrect"] as? Number)?.toInt() ?: 0
        )
        SaveLoadResult(save, false)
    } catch (_: Exception) {
        SaveLoadResult(SaveData(), true)
    }
}

internal fun escapeJson(value: String): String {
    val sb = StringBuilder()
    for (ch in value) {
        when (ch) {
            '"' -> sb.append("\\\"")
            '\\' -> sb.append("\\\\")
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            '\b' -> sb.append("\\b")
            '\u000C' -> sb.append("\\f")
            else -> {
                if (ch.code < 0x20) {
                    sb.append("\\u%04x".format(ch.code))
                } else {
                    sb.append(ch)
                }
            }
        }
    }
    return sb.toString()
}

/** Minimal JSON parser: objects, arrays, strings, numbers, booleans, null. */
internal class JsonParser(private val text: String) {
    private var pos: Int = 0

    fun parseValue(): Any? {
        skipWs()
        if (pos >= text.length) error("unexpected end of input")
        return when (val ch = text[pos]) {
            '{' -> parseObject()
            '[' -> parseArray()
            '"' -> parseString()
            't' -> expectLiteral("true", true)
            'f' -> expectLiteral("false", false)
            'n' -> expectLiteral("null", null)
            else -> parseNumber()
        }
    }

    private fun parseObject(): Map<String, Any?> {
        pos++ // {
        val result = linkedMapOf<String, Any?>()
        skipWs()
        if (pos < text.length && text[pos] == '}') {
            pos++
            return result
        }
        while (true) {
            skipWs()
            val key = parseString()
            skipWs()
            if (pos >= text.length || text[pos] != ':') error("expected ':'")
            pos++
            val value = parseValue()
            result[key] = value
            skipWs()
            if (pos >= text.length) error("unterminated object")
            when (text[pos]) {
                ',' -> pos++
                '}' -> {
                    pos++
                    return result
                }
                else -> error("expected ',' or '}'")
            }
        }
    }

    private fun parseArray(): List<Any?> {
        pos++ // [
        val result = mutableListOf<Any?>()
        skipWs()
        if (pos < text.length && text[pos] == ']') {
            pos++
            return result
        }
        while (true) {
            result.add(parseValue())
            skipWs()
            if (pos >= text.length) error("unterminated array")
            when (text[pos]) {
                ',' -> pos++
                ']' -> {
                    pos++
                    return result
                }
                else -> error("expected ',' or ']'")
            }
        }
    }

    private fun parseString(): String {
        if (pos >= text.length || text[pos] != '"') error("expected string")
        pos++ // opening quote
        val sb = StringBuilder()
        while (pos < text.length) {
            val ch = text[pos++]
            when (ch) {
                '"' -> return sb.toString()
                '\\' -> {
                    if (pos >= text.length) error("unterminated escape")
                    when (val esc = text[pos++]) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        '/' -> sb.append('/')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (pos + 4 > text.length) error("bad unicode escape")
                            val hex = text.substring(pos, pos + 4)
                            pos += 4
                            sb.append(hex.toInt(16).toChar())
                        }
                        else -> error("bad escape")
                    }
                }
                else -> sb.append(ch)
            }
        }
        error("unterminated string")
    }

    private fun parseNumber(): Number {
        val start = pos
        while (pos < text.length && text[pos] in "-+0123456789.eE") pos++
        if (start == pos) error("expected number")
        val token = text.substring(start, pos)
        return if (token.contains('.') || token.contains('e') || token.contains('E')) {
            token.toDouble()
        } else {
            token.toLong()
        }
    }

    private fun expectLiteral(literal: String, value: Any?): Any? {
        if (!text.startsWith(literal, pos)) error("expected $literal")
        pos += literal.length
        return value
    }

    private fun skipWs() {
        while (pos < text.length && text[pos].isWhitespace()) pos++
    }
}
