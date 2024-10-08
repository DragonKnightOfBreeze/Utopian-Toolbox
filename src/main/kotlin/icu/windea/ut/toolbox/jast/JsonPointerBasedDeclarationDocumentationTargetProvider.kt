package icu.windea.ut.toolbox.jast

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents

class JsonPointerBasedDeclarationDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        val jElement = element.parents(true).firstNotNullOfOrNull { it.toJElement() } ?: return null
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null
        
        val declarationElement = jElement.psi
        val elementWithDocumentation = declarationElement.navigationElement ?: declarationElement

        val languageSettings = JsonPointerManager.getLanguageSettings(declarationElement) ?: return null
        if(!languageSettings.checkDeclarationForKey(jElement)) return null
        val type = languageSettings.declarationType
        if(type.isEmpty()) return null
        val (name) = jElement.getNameAndTextOffset()
        if(name.isNullOrEmpty()) return null
        
        return JsonPointerBasedDeclarationDocumentationTarget(elementWithDocumentation, originalElement)
    }
}
