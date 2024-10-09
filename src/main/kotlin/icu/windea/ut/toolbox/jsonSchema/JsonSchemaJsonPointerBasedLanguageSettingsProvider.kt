package icu.windea.ut.toolbox.jsonSchema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.jetbrains.jsonSchema.JsonDependencyModificationTracker
import com.jetbrains.jsonSchema.impl.JsonSchemaObject
import com.jetbrains.jsonSchema.impl.light.JsonSchemaNodePointer
import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.jast.*
import icu.windea.ut.toolbox.lang.UtPsiManager

class JsonSchemaJsonPointerBasedLanguageSettingsProvider : JsonPointerBasedLanguageSettingsProvider {
    override fun getLanguageSettings(element: PsiElement): JsonPointerBasedLanguageSettings? {
        val schemas = getSchemas(element) ?: return null
        val list = schemas.mapNotNull { schema -> getLanguageSettings(schema) }
        if(list.isEmpty()) return null
        val modificationTrackers = setOf(JsonDependencyModificationTracker.forProject(element.project))
        val languageSettings = JsonPointerManager.mergeLanguageSettings(list, modificationTrackers)
        return languageSettings
    }

    private fun getSchemas(element: PsiElement): Collection<JsonSchemaObject>? {
        if(UtPsiManager.isIncompletePsi()) {
            val parentJElement = element.parents(false).firstNotNullOfOrNull { it.toJElement().takeIf { e -> e is JArray || e is JProperty || e is JObject } } ?: return null
            val parentSchemas = JsonSchemaManager.getSchemas(parentJElement.psi) ?: return null
            val schemas = when {
                //string in array, or property key in property (typing text -> property -> object -> array) 
                parentJElement is JArray -> {
                    parentSchemas.mapNotNull { it.itemsSchema }.ifEmpty { parentSchemas.mapNotNull { it.propertyNamesSchema } }
                }
                //property key or property value in property
                parentJElement is JProperty -> {
                    val keyEndOffset = parentJElement.keyElement?.psi?.endOffset
                    val endOffset = element.endOffset
                    val isKey = keyEndOffset == null || endOffset <= keyEndOffset
                    if(isKey) parentSchemas.mapNotNull { it.propertyNamesSchema } else parentSchemas
                }
                //property key in property (typing text -> object) 
                parentJElement is JObject -> {
                    parentSchemas.mapNotNull { it.propertyNamesSchema }
                }
                else -> parentSchemas
            }
            return schemas
        }
        val jElement = element.toJElement() ?: return null
        val originalSchemas = JsonSchemaManager.getSchemas(element) ?: return null
        val isKey = jElement is JProperty || jElement is JPropertyKey
        val schemas = if(isKey) originalSchemas.mapNotNull { it.propertyNamesSchema } else originalSchemas
        return schemas
    }

    fun getLanguageSettings(schema: JsonSchemaObject): JsonPointerBasedLanguageSettings? {
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
