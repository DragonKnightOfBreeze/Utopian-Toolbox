package icu.windea.ut.toolbox.jast

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.util.*

class JsonPointerBasedDeclarationUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        val elementToSearch = queryParameters.elementToSearch
        val jElement = elementToSearch.toJElement()
        if (jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return

        val languageSettings = JsonPointerManager.getLanguageSettings(elementToSearch) ?: return
        if (languageSettings.declarationType.isEmpty()) return
        val name = jElement.getName()
        if (name.isNullOrEmpty()) return

        val project = queryParameters.project
        DumbService.getInstance(project).runReadActionInSmartMode {
            val useScope = queryParameters.effectiveSearchScope
            val searchContext = UsageSearchContext.IN_CODE
            queryParameters.optimizer.searchWord(name, useScope, searchContext, false, elementToSearch)
        }
    }
}
