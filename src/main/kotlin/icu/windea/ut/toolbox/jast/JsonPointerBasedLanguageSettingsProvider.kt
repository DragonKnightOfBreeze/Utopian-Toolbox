package icu.windea.ut.toolbox.jast

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement

interface JsonPointerBasedLanguageSettingsProvider {
    fun getLanguageSettings(element: PsiElement): JsonPointerBasedLanguageSettings?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<JsonPointerBasedLanguageSettingsProvider>("icu.windea.ut.toolbox.jsonPointerBasedLanguageSettingsProvider")
    }
}
