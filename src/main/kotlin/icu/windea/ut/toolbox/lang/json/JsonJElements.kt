package icu.windea.ut.toolbox.lang.json

import com.intellij.json.psi.*
import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.jast.*

sealed class JsonJElement : JElement {
    override fun equals(other: Any?): Boolean {
        if(this === other) return true
        return other != null && other.javaClass == this.javaClass && (other as JElement).psi == this.psi
    }

    override fun hashCode(): Int {
        return psi.hashCode()
    }
}

class JsonJProperty(
    override val psi: JsonProperty
) : JsonJElement(), JProperty {
    override val keyElement: JPropertyKey get() = psi.nameElement.toJElementOfType<JPropertyKey>()!!
    override val valueElement: JValue? get() = psi.value?.toJElementOfType<JValue>()
    override val parent: JObject? get() = psi.parent?.toJElementOfType()
}

class JsonJPropertyKey(
    override val psi: JsonValue
) : JsonJElement(), JPropertyKey {
    override val value: String? get() = doGetValue()
    override val textOffset: Int get() = doGetTextOffset()
    override val parent: JProperty? get() = psi.parent?.toJElementOfType<JProperty>()

    private fun doGetValue(): String? {
        return when(psi) {
            is JsonStringLiteral -> psi.value
            is JsonReferenceExpression -> psi.text //JSON5 unquoted property key
            else -> null
        }
    }
    
    private fun doGetTextOffset(): Int {
        return when(psi) {
            is JsonReferenceExpression -> 0 //JSON5 unquoted property key
            else -> 1
        }
    }
}

sealed class JsonJValue(
    override val psi: JsonValue
) : JsonJElement(), JValue {
    override val parent: JElement? get() = psi.parent?.toJElementOfTypes(JProperty::class.java, JArray::class.java)
}

sealed class JsonJLiteral(
    override val psi: JsonLiteral
) : JsonJValue(psi), JLiteral

class JsonJNull(
    override val psi: JsonNullLiteral
) : JsonJLiteral(psi), JNull

class JsonJBoolean(
    override val psi: JsonBooleanLiteral
) : JsonJLiteral(psi), JBoolean {
    override val value: Boolean get() = psi.value
}

class JsonJNumber(
    override val psi: JsonNumberLiteral
) : JsonJLiteral(psi), JNumber {
    override val value: Double get() = psi.value
}

class JsonJString(
    override val psi: JsonStringLiteral
) : JsonJLiteral(psi), JString {
    override val value: String get() = psi.value
    override val textOffset: Int get() = 1
}

sealed class JsonJContainer(
    override val psi: JsonContainer
) : JsonJValue(psi), JContainer

class JsonJArray(
    override val psi: JsonArray
) : JsonJContainer(psi), JArray {
    override val elementsSequence: Sequence<JValue> by lazy { psi.toChildIterator { it.castOrNull<JsonValue>()?.toJElementOfType<JValue>() }.asSequence() }
    override val elements: List<JValue> by lazy { elementsSequence.toList() }
}

class JsonJObject(
    override val psi: JsonObject
) : JsonJContainer(psi), JObject {
    override val elementsSequence: Sequence<JProperty> by lazy { psi.toChildIterator { it.castOrNull<JsonProperty>()?.toJElementOfType<JProperty>() }.asSequence() }
    override val elements: List<JProperty> by lazy { elementsSequence.toList() }
}
