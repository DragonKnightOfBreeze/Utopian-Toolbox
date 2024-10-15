package icu.windea.ut.toolbox.jast

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.*

interface JsonPointerBasedLanguageSettingsProvider {
    fun getLanguageSettings(element: PsiElement): JsonPointerBasedLanguageSettings?

    fun getModificationTracker(element: PsiElement): ModificationTracker?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<JsonPointerBasedLanguageSettingsProvider>("icu.windea.ut.toolbox.jsonPointerBasedLanguageSettingsProvider")
    }
}
