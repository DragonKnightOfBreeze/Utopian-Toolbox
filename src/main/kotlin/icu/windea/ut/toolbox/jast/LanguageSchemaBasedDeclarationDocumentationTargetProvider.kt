package icu.windea.ut.toolbox.jast

import com.intellij.platform.backend.documentation.*
import com.intellij.psi.*
import com.intellij.psi.util.*

class LanguageSchemaBasedDeclarationDocumentationTargetProvider : PsiDocumentationTargetProvider {
    override fun documentationTarget(element: PsiElement, originalElement: PsiElement?): DocumentationTarget? {
        val jElement = element.parents(true).firstNotNullOfOrNull { it.toJElement() } ?: return null
        if (jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null
        return documentationTarget(jElement)
    }

    private fun documentationTarget(jElement: JElement): DocumentationTarget? {
        val element = jElement.psi
        val languageSchema = JastManager.getLanguageSchema(element) ?: return null
        if (languageSchema.declaration.id.isNotEmpty()) {
            //declaration
            val name = jElement.getName()
            if (name.isNullOrEmpty()) return null
            return LanguageSchemaBasedDeclarationDocumentationTarget(element)
        } else if (languageSchema.reference.url.isNotEmpty()) {
            //reference
            val references = PsiReferenceService.getService().getContributedReferences(element)
            for (reference in references) {
                if (reference !is LanguageSchemaBasedReferenceProvider.Reference) continue
                val resolveElement = reference.resolve()
                val resolvedJElement = resolveElement?.parent?.toJElement() ?: continue
                val documentationTarget = documentationTarget(resolvedJElement)
                if (documentationTarget != null) return documentationTarget
                break
            }
        }
        return null
    }
}
