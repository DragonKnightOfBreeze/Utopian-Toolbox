package icu.windea.ut.toolbox.lang.json

import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonValue

object JsonManager {
    fun isPropertyKey(element: JsonValue): Boolean {
        return element.parent.let { it is JsonProperty && it.nameElement == element }
    }
}
