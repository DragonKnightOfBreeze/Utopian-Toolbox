package icu.windea.ut.toolbox.jast

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.jast.JastManager.parseJsonPointer

object LanguageSchemaManager {
    fun resolveDeclarationType(languageSchema: LanguageSchema, position: JElement): String {
        val v = languageSchema.declarationType
        return doResolveFromTextOrJsonPointer(v, position, "(unresolved)")
    }

    fun resolveDeclarationDescription(languageSchema: LanguageSchema, position: JElement): String {
        val v = languageSchema.declarationDescription
        return doResolveFromTextOrJsonPointer(v, position)
    }

    fun resolveDeclarationProperties(languageSchema: LanguageSchema, position: JElement): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for ((k, v) in languageSchema.declarationProperties) {
            val r = doResolveFromTextOrJsonPointer(v, position)
            result[k] = r
        }
        return result
    }

    private fun doResolveFromTextOrJsonPointer(value: String, position: JElement, defaultValue: String = ""): String {
        if (parseJsonPointer(value) == null) {
            return value
        }
        var r = defaultValue
        JastManager.processElementsFromParent(value, position) p@{
            val name = it.getName()?.orNull() ?: return@p true
            r = name
            false
        }
        return r
    }
    
    fun processReferences(languageSchema: LanguageSchema, currentFile: PsiFile, processor: Processor<JElement>): Boolean {
        return languageSchema.references.process { ref ->
            JastManager.processElements(ref, currentFile, processor)
        }
    }
}
