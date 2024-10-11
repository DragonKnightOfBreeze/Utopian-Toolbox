package icu.windea.ut.toolbox.jast

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.PsiDocumentationTargetProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.util.parents

class JsonPointerBasedDeclarationDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        val jElement = element.parents(true).firstNotNullOfOrNull { it.toJElement() } ?: return null
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null
        return documentationTarget(jElement)
    }

    private fun documentationTarget(jElement: JElement): DocumentationTarget? {
        val element = jElement.psi
        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return null
        if(languageSettings.declarationType.isNotEmpty()) {
            //declaration
            val (name) = jElement.getNameAndTextOffset()
            if(name.isNullOrEmpty()) return null
            return JsonPointerBasedDeclarationDocumentationTarget(element)
        } else if(languageSettings.references.isNotEmpty()) {
            //reference
            val references = PsiReferenceService.getService().getContributedReferences(element)
            for(reference in references) {
                if(reference !is JsonPointerBasedReferenceProvider.Reference) continue
                val resolveResults = reference.multiResolve(false)
                for(resolveResult in resolveResults) {
                    val resolved = resolveResult.element ?: continue
                    val resolvedJElement = resolved.toJElement() ?: continue
                    val documentationTarget = documentationTarget(resolvedJElement)
                    if(documentationTarget != null) return documentationTarget
                }
                break
            }
        }
        return null
    }
}
