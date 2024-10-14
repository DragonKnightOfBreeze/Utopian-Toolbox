package icu.windea.ut.toolbox.lang.json

import com.intellij.json.psi.*
import com.intellij.openapi.application.*

object JsonManager {
    fun isPropertyKey(element: JsonValue): Boolean {
        return runReadAction {
            element.parent.let { it is JsonProperty && it.nameElement == element }
        }
    }
}
