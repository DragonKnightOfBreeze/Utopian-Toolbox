package icu.windea.ut.toolbox.jsonSchema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.psi.PsiElement
import com.jetbrains.jsonSchema.JsonDependencyModificationTracker
import com.jetbrains.jsonSchema.impl.JsonSchemaObject
import com.jetbrains.jsonSchema.impl.light.JsonSchemaNodePointer
import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.jast.*

class JsonSchemaJsonPointerBasedLanguageSettingsProvider : JsonPointerBasedLanguageSettingsProvider {
    override fun getLanguageSettings(element: PsiElement): JsonPointerBasedLanguageSettings? {
        val jElement = element.toJElement() ?: return null
        val forKey = jElement is JProperty || jElement is JPropertyKey
        
        val schemas = JsonSchemaManager.getSchemas(element) ?: return null
        val list = schemas.mapNotNull { schema -> getLanguageSettings(schema, forKey) }
        val modificationTrackers = setOf(JsonDependencyModificationTracker.forProject(element.project))
        val languageSettings = JsonPointerManager.mergeLanguageSettings(list, modificationTrackers)
        return languageSettings
    }

    fun getLanguageSettings(schemaObject: JsonSchemaObject, forKey: Boolean): JsonPointerBasedLanguageSettings? {
        var schema = schemaObject
        if(forKey) schema = schema.propertyNamesSchema ?: return null
        val schemaNode = schema.castOrNull<JsonSchemaNodePointer<ObjectNode>>()?.rawSchemaNode ?: return null
        val node = schemaNode.get("\$languageSettings") ?: return null
        return JsonPointerBasedLanguageSettings(
            declarationType = node.get("declarationType")?.textValue().orEmpty(),
            hintForDeclarations = node.get("hintForDeclarations")?.booleanValue() ?: true,
            references = node.get("references")?.toStringOrStringSetValue().orEmpty(),
            hintForReferences = node.get("hintForReferences")?.booleanValue() ?: true,
            inspectionForReferences = node.get("inspectionForReferences")?.booleanValue() ?: true,
            completionForReferences = node.get("completionForReferences")?.booleanValue() ?: true,
        )
    }

    private fun JsonNode.toStringOrStringSetValue(): Set<String>? {
        return when {
            this.isArray -> this.elements()?.asSequence()?.mapNotNullTo(mutableSetOf()) { it.textValue() }
            else -> this.textValue()?.let { setOf(it) }
        }
    }
}
