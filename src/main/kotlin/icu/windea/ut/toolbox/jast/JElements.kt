package icu.windea.ut.toolbox.jast

import com.intellij.lang.Language
import com.intellij.psi.PsiElement

interface JElement {
    val psi: PsiElement
    val parent: JElement?
    val language: Language get() = psi.language
}

interface JProperty : JElement {
    val keyElement: JPropertyKey?
    val valueElement: JValue?
}

interface JPropertyKey : JElement {
    val value: String?
}

interface JValue : JElement

interface JLiteral : JValue

interface JNull : JLiteral

interface JBoolean : JLiteral {
    val value: Boolean
}

interface JNumber : JLiteral {
    val value: Double
}

interface JString : JLiteral {
    val value: String
}

interface JContainer : JValue

interface JArray : JContainer {
    val elementsSequence: Sequence<JValue>
    val elements: List<JValue>
    val isEmpty: Boolean get() = elementsSequence.none()
    val isNotEmpty: Boolean get() = elementsSequence.any()
}

interface JObject : JContainer {
    val elementsSequence: Sequence<JProperty>
    val elements: List<JProperty>
    val isEmpty: Boolean get() = elementsSequence.none()
    val isNotEmpty: Boolean get() = elementsSequence.any()
}
