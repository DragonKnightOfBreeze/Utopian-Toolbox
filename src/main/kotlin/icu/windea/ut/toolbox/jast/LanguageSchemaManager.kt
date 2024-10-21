package icu.windea.ut.toolbox.jast

import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.jast.JastManager.parseJsonPointer

object LanguageSchemaManager {
    fun resolveDeclarationType(languageSchema: LanguageSchema, element: JElement): String {
        val v = languageSchema.declaration.type
        return doResolveFromTextOrJsonPointer(v, element, "(unresolved)")
    }

    fun resolveDeclarationDescription(languageSchema: LanguageSchema, element: JElement): String {
        val v = languageSchema.declaration.description
        return doResolveFromTextOrJsonPointer(v, element)
    }

    fun resolveDeclarationExtraProperties(languageSchema: LanguageSchema, element: JElement): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for ((k, v) in languageSchema.declaration.extraProperties) {
            val r = doResolveFromTextOrJsonPointer(v, element)
            result[k] = r
        }
        return result
    }
    
    fun resolveDeclarationContainerText(languageSchema: LanguageSchema,  element: JElement): String {
        val v = languageSchema.declarationContainer.url
        return doResolveFromTextOrJsonPointer(v, element)
    }

    private fun doResolveFromTextOrJsonPointer(value: String, jElement: JElement, defaultValue: String = ""): String {
        if (parseJsonPointer(value) == null) {
            return value
        }
        var r = defaultValue
        JastManager.processElementsInContainer(value, jElement) p@{
            val name = it.getName()?.orNull() ?: return@p true
            r = name
            false
        }
        return r
    }
}
