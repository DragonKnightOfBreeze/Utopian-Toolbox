package icu.windea.ut.toolbox.jast

import com.intellij.platform.backend.documentation.*
import com.intellij.psi.*
import com.intellij.psi.util.*

class JsonPointerBasedDeclarationDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        val jElement = element.parents(true).firstNotNullOfOrNull { it.toJElement() } ?: return null
        if (jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null
        return documentationTarget(jElement)
    }

    private fun documentationTarget(jElement: JElement): DocumentationTarget? {
        val element = jElement.psi
        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return null
        if (languageSettings.declarationType.isNotEmpty()) {
            //declaration
            val (name) = jElement.getNameAndTextOffset()
            if (name.isNullOrEmpty()) return null
            return JsonPointerBasedDeclarationDocumentationTarget(element)
        } else if (languageSettings.references.isNotEmpty()) {
            //reference
            val references = PsiReferenceService.getService().getContributedReferences(element)
            for (reference in references) {
                if (reference !is JsonPointerBasedReferenceProvider.Reference) continue
                val resolveElements = reference.getResolvedElements()
                for (resolveElement in resolveElements) {
                    val resolvedJElement = resolveElement.toJElement() ?: continue
                    val documentationTarget = documentationTarget(resolvedJElement)
                    if (documentationTarget != null) return documentationTarget
                }
                break
            }
        }
        return null
    }
}
