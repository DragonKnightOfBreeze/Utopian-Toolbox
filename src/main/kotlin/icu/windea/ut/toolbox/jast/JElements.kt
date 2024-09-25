package icu.windea.ut.toolbox.jast

import com.intellij.lang.Language
import com.intellij.psi.PsiElement

interface JElement {
    val psi: PsiElement
    val parent: JElement?
    val language: Language get() = psi.language
}

interface JProperty : JElement {
    val keyElement: JPropertyKey
    val valueElement: JValue?
    val text: String
    val key: String
    val value: String?
}

interface JPropertyKey : JElement {
    val text: String
    val value: String
}

interface JValue : JElement {
    val text: String
}

interface JNull: JValue

interface JBoolean : JValue {
    val value: Boolean
}

interface JNumber : JValue {
    val value: Double
}

interface JString : JValue {
    val value: String
}

interface JObject : JValue {
    val elementsIterator: Iterator<JProperty>
    val elements: List<JProperty> get() = Iterable { elementsIterator }.toList()
    val isEmpty: Boolean get() = !elementsIterator.hasNext()
    val isNotEmpty: Boolean get() = elementsIterator.hasNext()
}

interface JArray : JValue {
    val elementsIterator: Iterator<JValue>
    val elements: List<JValue> get() = Iterable { elementsIterator }.toList()
    val isEmpty: Boolean get() = !elementsIterator.hasNext()
    val isNotEmpty: Boolean get() = elementsIterator.hasNext()
}
