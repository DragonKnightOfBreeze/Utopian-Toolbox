package icu.windea.ut.toolbox.jast

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.*

interface LanguageSchemaProvider {
    fun getLanguageSchema(element: PsiElement): LanguageSchema?

    fun getModificationTracker(element: PsiElement): ModificationTracker?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<LanguageSchemaProvider>("icu.windea.ut.toolbox.languageSchemaProvider")
    }
}
