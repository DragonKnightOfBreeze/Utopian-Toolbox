package icu.windea.ut.toolbox.jsonSchema

import com.fasterxml.jackson.databind.JsonNode
import com.intellij.psi.PsiElement
import com.jetbrains.jsonSchema.JsonDependencyModificationTracker
import com.jetbrains.jsonSchema.impl.JsonSchemaObject
import com.jetbrains.jsonSchema.impl.light.JsonSchemaNodePointer
import icu.windea.ut.toolbox.castOrNull
import icu.windea.ut.toolbox.jast.*

class JsonSchemaJsonPointerBasedLanguageSettingsProvider : JsonPointerBasedLanguageSettingsProvider {
    override fun getLanguageSettings(element: PsiElement): JsonPointerBasedLanguageSettings? {
        val schemas = JsonSchemaManager.getSchemas(element) ?: return null
        val list = schemas.mapNotNull { schema -> getLanguageSettings(schema) }
        val modificationTrackers = setOf(JsonDependencyModificationTracker.forProject(element.project))
        val languageSettings = JsonPointerManager.mergeLanguageSettings(list, modificationTrackers)
        return languageSettings
    }

    fun getLanguageSettings(schemaObject: JsonSchemaObject): JsonPointerBasedLanguageSettings? {
        val p = schemaObject.getPropertyByName("\$languageSettings") ?: return null
        val jsonNode = p.castOrNull<JsonSchemaNodePointer<JsonNode>>()?.rawSchemaNode ?: return null
        return JsonPointerBasedLanguageSettings(
            references = jsonNode.get("references").toStringOrStringSetValue(),
            completion = jsonNode.get("completion").toStringOrStringSetValue(),
        )
    }

    private fun JsonNode.toStringOrStringSetValue(): Set<String> {
        return when {
            this.isArray -> this.elements()?.asSequence()?.mapNotNullTo(mutableSetOf()) { it.textValue() }.orEmpty()
            else -> this.textValue()?.let { setOf(it) }.orEmpty()
        }
    }
}
