@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.ut.toolbox.jast

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.ut.toolbox.core.util.*

inline fun PsiElement.toJElement(): JElement? = JElementManager.getJElement(this)

inline fun <reified T : JElement> PsiElement.toJElementOfType(): T? = JElementManager.getJElement(this, T::class.java)

inline fun <T : JElement> PsiElement.toJElementOfTypes(vararg targetTypes: Class<out T>): JElement? = JElementManager.getJElement(this, *targetTypes)

//JAST Extensions

fun JElement.getNameAndTextOffset(): Tuple2<String?, Int> {
    return when {
        this is JProperty -> this.keyElement?.let { it.value to it.textOffset } ?: (null to 0)
        this is JPropertyKey -> this.let { it.value to it.textOffset }
        this is JString -> this.let { it.value to it.textOffset }
        else -> null to 0
    }
}

fun JElement.getRangeInElement(name: String, textOffset: Int): TextRange {
    val startOffset = when {
        this is JProperty -> keyElement?.psi?.startOffsetInParent ?: 0
        else -> 0
    }
    val range = TextRange.from(startOffset + textOffset, name.length)
    return range
}

//JAST Search Extensions

inline fun JObject.findProperty(predicate: (JProperty) -> Boolean): JProperty? {
    return elementsSequence.find(predicate)
}

fun JObject.findProperties(predicate: (JProperty) -> Boolean): List<JProperty> {
    return elementsSequence.filter(predicate).toList()
}

fun JObject.findProperty(propertyName: String, ignoreCase: Boolean = false): JProperty? {
    return findProperty { it.keyElement?.value.equals(propertyName, ignoreCase) }
}
