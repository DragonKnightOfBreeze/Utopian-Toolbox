package icu.windea.ut.toolbox.jsonSchema

import com.fasterxml.jackson.databind.JsonNode
import com.intellij.psi.PsiElement
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalker
import com.jetbrains.jsonSchema.ide.JsonSchemaService
import com.jetbrains.jsonSchema.impl.JsonSchemaObject
import com.jetbrains.jsonSchema.impl.JsonSchemaResolver
import com.jetbrains.jsonSchema.impl.light.JsonSchemaNodePointer
import icu.windea.ut.toolbox.castOrNull

object JsonSchemaManager {
    const val LANGUAGE_SETTINGS_KEY = "\$languageSettings"
    
    fun getRootSchema(element: PsiElement): JsonSchemaObject? {
        val file = element.containingFile ?: return null
        val jsonSchemaService = JsonSchemaService.Impl.get(file.project)
        val schemaObject = jsonSchemaService.getSchemaObject(file)
        return schemaObject
    }

    fun getSchemas(element: PsiElement): Collection<JsonSchemaObject>? {
        val rootSchema = getRootSchema(element) ?: return null
        val walker = JsonLikePsiWalker.getWalker(element, rootSchema) ?: return null
        val elementToCheck = walker.findElementToCheck(element) ?: return null
        val position = walker.findPosition(elementToCheck, true) ?: return null
        val schemas = JsonSchemaResolver(element.project, rootSchema, position).resolve()
        return schemas
    }
    
    fun getLanguageSettingsListFromSchemas(element: PsiElement): Collection<LanguageSettings>? {
        val schemas = getSchemas(element) ?: return null
        return schemas.mapNotNull { schema -> getLanguageSettings(schema) }
    }
    
    fun getLanguageSettings(schemaObject: JsonSchemaObject): LanguageSettings? {
        val p = schemaObject.getPropertyByName(LANGUAGE_SETTINGS_KEY) ?: return null
        val jsonNode = p.castOrNull<JsonSchemaNodePointer<JsonNode>>()?.rawSchemaNode ?: return null
        val declarations = jsonNode.get("declarations").toStringOrStringSetValue()
        val references = jsonNode.get("references").toStringOrStringSetValue()
        val completion = jsonNode.get("completion").toStringOrStringSetValue()
        return LanguageSettings(declarations, references, completion)
    }
    
    private fun JsonNode.toStringOrStringSetValue(): Set<String> {
        return when {
            this.isArray -> this.elements()?.asSequence()?.mapNotNullTo(mutableSetOf()) { it.textValue() }.orEmpty()
            else -> this.textValue()?.let { setOf(it) }.orEmpty()
        }
    }
}

