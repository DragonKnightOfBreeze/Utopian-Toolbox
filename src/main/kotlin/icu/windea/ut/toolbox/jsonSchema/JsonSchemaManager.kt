package icu.windea.ut.toolbox.jsonSchema

import com.intellij.psi.*
import com.jetbrains.jsonSchema.extension.*
import com.jetbrains.jsonSchema.ide.*
import com.jetbrains.jsonSchema.impl.*

object JsonSchemaManager {
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
}

