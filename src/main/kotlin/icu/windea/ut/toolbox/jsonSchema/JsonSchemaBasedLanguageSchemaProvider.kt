package icu.windea.ut.toolbox.jsonSchema

import com.fasterxml.jackson.databind.node.*
import com.fasterxml.jackson.module.kotlin.*
import com.intellij.openapi.components.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.jetbrains.jsonSchema.ide.*
import com.jetbrains.jsonSchema.impl.*
import com.jetbrains.jsonSchema.impl.light.*
import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.core.data.*
import icu.windea.ut.toolbox.jast.*
import icu.windea.ut.toolbox.lang.*

class JsonSchemaBasedLanguageSchemaProvider : LanguageSchemaProvider {
    override fun getLanguageSchema(element: PsiElement): LanguageSchema? {
        val schemas = getSchemas(element) ?: return null
        val languageSchemas = schemas.mapNotNull { schema -> getLanguageSchema(schema) }
        val result = mergeLanguageSchema(languageSchemas)
        return result
    }

    override fun getModificationTracker(element: PsiElement): ModificationTracker? {
        //NOTE IDE源码中要求jsonSchema文件是标准的json文件，否则这里不会正常更新状态
        //com.jetbrains.jsonSchema.JsonSchemaVfsListener.JsonSchemaUpdater.onFileChange

        return element.project.service<JsonSchemaService>().castOrNull<ModificationTracker>()
    }

    private fun getSchemas(element: PsiElement): Collection<JsonSchemaObject>? {
        if (UtPsiManager.isIncompletePsi()) {
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
                    if (isKey) parentSchemas.mapNotNull { it.propertyNamesSchema } else parentSchemas
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
        val schemas = if (isKey) originalSchemas.mapNotNull { it.propertyNamesSchema } else originalSchemas
        return schemas
    }

    fun getLanguageSchema(schema: JsonSchemaObject): LanguageSchema? {
        return doGetLanguageSchema(schema)
    }

    private fun doGetLanguageSchema(schema: JsonSchemaObject): LanguageSchema? {
        when (schema) {
            is JsonSchemaNodePointer<*> -> {
                doGetLanguageSchemaFromJackson(schema)?.let { return it }
            }
            is MergedJsonSchemaObject -> {
                doGetLanguageSchema(schema.base)?.let { return it }
                doGetLanguageSchema(schema.other)?.let { return it }
            }
        }
        return null
    }

    private fun doGetLanguageSchemaFromJackson(schema: JsonSchemaObject): LanguageSchema? {
        val node = schema.castOrNull<JsonSchemaNodePointer<Any>>()?.rawSchemaNode ?: return null
        val languageSchemaNode = node.castOrNull<ObjectNode>()?.get("\$languageSchema") ?: return null
        return jsonMapper.treeToValue<LanguageSchema>(languageSchemaNode)
    }
    
    fun mergeLanguageSchema(languageSchemas: List<LanguageSchema>): LanguageSchema? {
        if(languageSchemas.isEmpty()) return null
        
        val declarationSchema = languageSchemas.firstNotNullOfOrNull { s -> s.declaration.takeIf { it.id.isNotEmpty() } }
        val declarationContainerSchema = languageSchemas.firstNotNullOfOrNull { s -> s.declarationContainer.takeIf { it.url.isNotEmpty() } }
        val referenceSchema = languageSchemas.firstNotNullOfOrNull { s -> s.reference.takeIf { it.url.isNotEmpty() } }
        return LanguageSchema(
            declaration = declarationSchema ?: LanguageSchema.Declaration(),
            declarationContainer = declarationContainerSchema ?: LanguageSchema.DeclarationContainer(),
            reference = referenceSchema ?: LanguageSchema.Reference(),
        )
    }
}
