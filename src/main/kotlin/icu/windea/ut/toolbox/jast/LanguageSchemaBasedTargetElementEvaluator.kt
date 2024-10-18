package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*

//NOTE 这个扩展同一语言只会选用一个

class LanguageSchemaBasedTargetElementEvaluator : TargetElementEvaluatorEx2() {
    override fun getNamedElement(element: PsiElement): PsiElement? {
        val jElement = element.parents(true).firstNotNullOfOrNull { it.toJElement() }
        if (jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null
        val references = PsiReferenceService.getService().getContributedReferences(jElement.psi)
        for (reference in references) {
            when (reference) {
                is LanguageSchemaBasedReferenceProvider.SelfReference -> return reference.resolve()
                is LanguageSchemaBasedReferenceProvider.Reference -> return reference.multiResolve(false).firstOrNull()?.element
            }
        }
        return null
    }

    override fun isAcceptableNamedParent(parent: PsiElement): Boolean {
        return true
    }

    override fun isAcceptableReferencedElement(element: PsiElement, referenceOrReferencedElement: PsiElement?): ThreeState {
        if (referenceOrReferencedElement is LanguageSchemaBasedReferenceProvider.ReferenceElement) return ThreeState.NO
        return ThreeState.UNSURE
    }

    override fun getTargetCandidates(reference: PsiReference): Collection<PsiElement>? {
        if (reference is LanguageSchemaBasedReferenceProvider.SelfReference) return emptySet()
        return null
    }
}
