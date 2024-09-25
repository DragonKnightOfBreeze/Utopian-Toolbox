package icu.windea.ut.toolbox.jast

import com.intellij.json.psi.*
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parentOfTypes
import icu.windea.ut.toolbox.createChildIterator

class JsonJProperty(
    override val psi: JsonProperty
) : JProperty {
    override val keyElement: JPropertyKey get() = psi.nameElement.toJElement<JPropertyKey>()!!
    override val valueElement: JValue? get() = psi.value?.toJElement<JValue>()
    override val text: String get() = psi.text
    override val key: String get() = psi.nameElement.name.orEmpty()
    override val value: String? get() = psi.value?.name
    override val parent: JElement? get() = psi.parentOfType<JsonObject>()?.toJElement()
}

class JsonJPropertyKey(
    override val psi: JsonValue
) : JPropertyKey {
    override val text: String get() = psi.text
    override val value: String get() = if (psi is JsonStringLiteral) psi.value else psi.text
    override val parent: JElement? get() = psi.parentOfType<JsonProperty>()?.toJElement()
}

sealed class JsonJValue(
    override val psi: JsonValue
) : JValue {
    override val text: String get() = psi.text
    override val parent: JElement? get() = psi.parentOfTypes(JsonProperty::class, JsonArray::class)?.toJElement()
}

class JsonJNull(
    override val psi: JsonNullLiteral
) : JsonJValue(psi), JNull

class JsonJBoolean(
    override val psi: JsonBooleanLiteral
) : JsonJValue(psi), JBoolean {
    override val value: Boolean get() = psi.value
}

class JsonJNumber(
    override val psi: JsonNumberLiteral
) : JsonJValue(psi), JNumber {
    override val value: Double get() = psi.value
}

class JsonJString(
    override val psi: JsonStringLiteral
) : JsonJValue(psi), JString {
    override val value: String get() = psi.value
}

class JsonJObject(
    override val psi: JsonObject
) : JsonJValue(psi), JObject {
    override val elementsIterator: Iterator<JProperty>
        get() = psi.createChildIterator<JsonProperty, JProperty>({ true }, { it.toJElement<JProperty>()!! })
}
