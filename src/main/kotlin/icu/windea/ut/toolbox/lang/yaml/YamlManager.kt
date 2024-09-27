package icu.windea.ut.toolbox.lang.yaml

object YamlManager {
    fun isNull(text: String): Boolean {
        return text.lowercase() == "null"
    }

    fun toBoolean(text: String): Boolean? {
        return when(text.lowercase()) {
            "true", "yes", "on" -> true
            "false", "no", "off" -> false
            else -> null
        }
    }

    fun toNumber(text: String): Double? {
        return text.toDoubleOrNull()
    }
}
