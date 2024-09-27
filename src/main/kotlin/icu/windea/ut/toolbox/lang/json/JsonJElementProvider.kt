package icu.windea.ut.toolbox.lang.json

import com.intellij.json.psi.*
import com.intellij.psi.*
import icu.windea.ut.toolbox.jast.*

class JsonJElementProvider : JElementProvider {
    override fun getTopLevelValue(file: PsiFile): JValue? {
        if (file !is JsonFile) return null
        return file.topLevelValue?.toJElementOfType()
    }

    override fun getTopLevelValues(file: PsiFile): List<JValue> {
        if (file !is JsonFile) return emptyList()
        return file.allTopLevelValues.mapNotNull { it.toJElementOfType<JValue>() }
    }

    override fun getJElement(element: PsiElement, targetType: Class<out JElement>): JElement? {
        return when {
            element is JsonProperty -> when {
                targetType.isAssignableFrom(JProperty::class.java) -> JsonJProperty(element)
                else -> null
            }
            element is JsonValue && JsonManager.isPropertyKey(element) -> when {
                targetType.isAssignableFrom(JPropertyKey::class.java) -> JsonJPropertyKey(element)
                else -> null
            }
            element is JsonNullLiteral -> when {
                targetType.isAssignableFrom(JNull::class.java) -> JsonJNull(element)
                else -> null
            }
            element is JsonBooleanLiteral -> when {
                targetType.isAssignableFrom(JBoolean::class.java) -> JsonJBoolean(element)
                else -> null
            }
            element is JsonNumberLiteral -> when {
                targetType.isAssignableFrom(JNumber::class.java) -> JsonJNumber(element)
                else -> null
            }
            element is JsonStringLiteral -> when {
                targetType.isAssignableFrom(JString::class.java) -> JsonJString(element)
                else -> null
            }
            element is JsonObject -> when {
                targetType.isAssignableFrom(JObject::class.java) -> JsonJObject(element)
                else -> null
            }
            element is JsonArray -> when {
                targetType.isAssignableFrom(JArray::class.java) -> JsonJArray(element)
                else -> null
            }
            else -> null
        }
    }
}
