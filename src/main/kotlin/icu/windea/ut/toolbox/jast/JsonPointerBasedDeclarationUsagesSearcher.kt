package icu.windea.ut.toolbox.jast

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiReference
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor

class JsonPointerBasedDeclarationUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        val elementToSearch = queryParameters.elementToSearch
        val jElement = elementToSearch.toJElement()
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return
        
        val languageSettings = JsonPointerManager.getLanguageSettings(elementToSearch) ?: return
        if(!languageSettings.checkDeclarationForKey(jElement)) return
        val type = languageSettings.declarationType
        if(type.isEmpty()) return
        val (name) = jElement.getNameAndTextOffset()
        if(name.isNullOrEmpty()) return

        val project = queryParameters.project
        DumbService.getInstance(project).runReadActionInSmartMode {
            val useScope = queryParameters.effectiveSearchScope
            val searchContext = UsageSearchContext.IN_CODE
            queryParameters.optimizer.searchWord(name, useScope, searchContext, false, elementToSearch)
        }
    }
}
