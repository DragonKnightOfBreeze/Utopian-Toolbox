package icu.windea.ut.toolbox.jast

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.jast.JsonPointerManager.parseJsonPointer

object JsonPointerBasedLanguageSettingsManager {
    fun resolveDeclarationType(languageSettings: JsonPointerBasedLanguageSettings, position: JElement): String {
        val v = languageSettings.declarationType
        return doResolveFromTextOrJsonPointer(v, position, "(unresolved)")
    }

    fun resolveDeclarationDescription(languageSettings: JsonPointerBasedLanguageSettings, position: JElement): String {
        val v = languageSettings.declarationDescription
        return doResolveFromTextOrJsonPointer(v, position)
    }

    fun resolveDeclarationProperties(languageSettings: JsonPointerBasedLanguageSettings, position: JElement): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for ((k, v) in languageSettings.declarationProperties) {
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
        JsonPointerManager.processElementsFromParent(value, position) p@{
            val name = it.getName()?.orNull() ?: return@p true
            r = name
            false
        }
        return r
    }
    
    fun processReferences(languageSettings: JsonPointerBasedLanguageSettings, currentFile: PsiFile, processor: Processor<JElement>): Boolean {
        return languageSettings.references.process { ref ->
            JsonPointerManager.processElements(ref, currentFile, processor)
        }
    }
}
