package icu.windea.ut.toolbox.lang.yaml

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.ut.toolbox.castOrNull
import icu.windea.ut.toolbox.jast.*
import icu.windea.ut.toolbox.toChildIterator
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.*
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl

sealed class YamlJElement : JElement {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other != null && other.javaClass == this.javaClass && (other as JElement).psi == this.psi
    }

    override fun hashCode(): Int {
        return psi.hashCode()
    }
}

class YamlJProperty(
    override val psi: YAMLKeyValue
) : YamlJElement(), JProperty {
    override val keyElement: JPropertyKey? get() = psi.key?.toJElementOfType()
    override val valueElement: JValue? get() = psi.value?.toJElementOfType()
    override val parent: JObject? get() = psi.parent?.toJElementOfType()
}

class YamlJPropertyKey(
    override val psi: PsiElement
) : YamlJElement(), JPropertyKey {
    override val value: String? get() = doGetValue()
    override val textOffset: Int get() = doGetTextOffset()
    override val parent: JProperty? get() = psi.parent?.toJElementOfType<JProperty>()

    private fun doGetValue(): String? {
        return if(psi.elementType == YAMLTokenTypes.SCALAR_KEY) psi.text else null
    }

    private fun doGetTextOffset(): Int {
        return 0
    }
}

sealed class YamlJValue(
    override val psi: YAMLValue
) : YamlJElement(), JValue {
    override val parent: JElement? get() = psi.parent?.toJElementOfTypes(JProperty::class.java, JArray::class.java)
}

sealed class YamlJLiteral(
    override val psi: YAMLScalar
) : YamlJValue(psi), JLiteral

class YamlJNull(
    override val psi: YAMLScalar
) : YamlJLiteral(psi), JNull

class YamlJBoolean(
    override val psi: YAMLPlainTextImpl
) : YamlJLiteral(psi), JBoolean {
    override val value: Boolean get() = psi.text.toBooleanByYaml()

    private fun String.toBooleanByYaml(): Boolean {
        return when(this.lowercase()) {
            "true", "yes", "on" -> true
            "false", "no", "off" -> false
            else -> false //fallback
        }
    }
}

class YamlJNumber(
    override val psi: YAMLPlainTextImpl
) : YamlJLiteral(psi), JNumber {
    override val value: Double get() = psi.text.toNumberByYaml()

    private fun String.toNumberByYaml(): Double {
        return this.toDoubleOrNull() ?: 0.0 //fallback
    }
}

class YamlJString(
    override val psi: YAMLScalar
) : YamlJLiteral(psi), JString {
    override val value: String get() = doGetValue()
    override val textOffset: Int get() = doGetTextOffset()

    private fun doGetValue(): String {
        return psi.textValue
    }

    private fun doGetTextOffset(): Int {
        return if(psi is YAMLQuotedText) 1 else 0 //simple implementation
    }
}

sealed class YamlJContainer(
    override val psi: YAMLCompoundValue
) : YamlJValue(psi), JContainer

class YamlJArray(
    override val psi: YAMLSequence
) : YamlJContainer(psi), JArray {
    override val elementsSequence: Sequence<JValue> by lazy { psi.toChildIterator { it.castOrNull<YAMLSequenceItem>()?.value?.toJElementOfType<JValue>() }.asSequence() }
    override val elements: List<JValue> by lazy { elementsSequence.toList() }
}

class YamlJObject(
    override val psi: YAMLMapping
) : YamlJContainer(psi), JObject {
    override val elementsSequence: Sequence<JProperty> by lazy { psi.toChildIterator { it.castOrNull<YAMLKeyValue>()?.toJElementOfType<JProperty>() }.asSequence() }
    override val elements: List<JProperty> by lazy { elementsSequence.toList() }
}
