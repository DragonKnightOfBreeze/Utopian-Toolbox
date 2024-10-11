@file:Suppress("UnstableApiUsage")

package icu.windea.ut.toolbox.jast

import com.intellij.model.Symbol
import com.intellij.model.psi.*
import com.intellij.model.search.SearchRequest
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiReferenceService

class JsonPointerBasedSymbolReferenceProvider: PsiSymbolReferenceProvider {
    override fun getReferences(element: PsiExternalReferenceHost, hints: PsiSymbolReferenceHints): Collection<PsiSymbolReference> {
        val references = PsiReferenceService.getService().getContributedReferences(element)
        val reference = references.find { it is JsonPointerBasedReferenceProvider.Reference } ?: return emptySet()
        return setOf(PsiSymbolService.getInstance().asSymbolReference(reference))
    }

    override fun getSearchRequests(project: Project, target: Symbol): Collection<SearchRequest> {
        return emptySet()
    }
}
