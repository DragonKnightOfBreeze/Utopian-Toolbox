package icu.windea.ut.toolbox.jast

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement

class JsonPointerBasedDeclarationDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        val jElement = element.toJElement()
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null

        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return null
        val type = languageSettings.declarationType
        if(type.isEmpty()) return null
        val (name) = jElement.getNameAndTextOffset()
        if(name.isNullOrEmpty()) return null
        
        val elementWithDocumentation = element.navigationElement ?: element
        return JsonPointerBasedDeclarationDocumentationTarget(elementWithDocumentation, originalElement)
    }
}
