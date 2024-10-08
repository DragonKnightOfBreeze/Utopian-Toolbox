package icu.windea.ut.toolbox.lang.json

import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonValue
import com.intellij.openapi.application.runReadAction

object JsonManager {
    fun isPropertyKey(element: JsonValue): Boolean {
        return runReadAction {
            element.parent.let { it is JsonProperty && it.nameElement == element }
        }
    }
}
