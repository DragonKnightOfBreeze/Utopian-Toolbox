@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.ut.toolbox.jast

import com.intellij.psi.PsiElement
import icu.windea.ut.toolbox.util.Tuple2

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
