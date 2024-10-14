package icu.windea.ut.toolbox.jast

import com.intellij.openapi.extensions.*
import com.intellij.psi.*

interface JsonPointerBasedLanguageSettingsProvider {
    fun getLanguageSettings(element: PsiElement): JsonPointerBasedLanguageSettings?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<JsonPointerBasedLanguageSettingsProvider>("icu.windea.ut.toolbox.jsonPointerBasedLanguageSettingsProvider")
    }
}
