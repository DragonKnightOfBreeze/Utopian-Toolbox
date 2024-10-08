package icu.windea.ut.toolbox.jast

import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.DocumentationTargetProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.util.parents

class JsonPointerBasedDeclarationDocumentationTargetProvider : DocumentationTargetProvider {
    override fun documentationTargets(file: PsiFile, offset: Int): List<DocumentationTarget> {
        return documentationTarget(file, offset)?.let { listOf(it) } ?: emptyList()
    }

    private fun documentationTarget(file: PsiFile, offset: Int): DocumentationTarget? {
        val locationElement = file.findElementAt(offset) ?: return null
        val jElement = locationElement.parents(true).firstNotNullOfOrNull { it.toJElement() } ?: return null
        return documentationTarget(jElement)
    }

    private fun documentationTarget(jElement: JElement): DocumentationTarget? {
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null
        val element = jElement.psi

        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return null
        if(languageSettings.declarationType.isNotEmpty()) {
            if(!languageSettings.checkDeclarationForKey(jElement)) return null

            val type = languageSettings.declarationType
            if(type.isEmpty()) return null
            val (name) = jElement.getNameAndTextOffset()
            if(name.isNullOrEmpty()) return null

            return JsonPointerBasedDeclarationDocumentationTarget(element)
        } else if(languageSettings.references.isNotEmpty()) {
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
