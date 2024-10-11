package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.TargetElementEvaluatorEx2
import com.intellij.psi.*
import com.intellij.psi.util.parents
import com.intellij.util.ThreeState

//NOTE 这个扩展同一语言只会选用一个

class JsonPointerBasedTargetElementEvaluator : TargetElementEvaluatorEx2() {
    override fun getNamedElement(element: PsiElement): PsiElement? {
        val jElement = element.parents(true).firstNotNullOfOrNull { it.toJElement() }
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null
        val references = PsiReferenceService.getService().getContributedReferences(jElement.psi)
        for(reference in references) {
            when(reference) {
                is JsonPointerBasedReferenceProvider.SelfReference -> return reference.resolve()
                is JsonPointerBasedReferenceProvider.Reference -> return reference.multiResolve(false).firstOrNull()?.element
            }
        }
        return null
    }

    override fun isAcceptableNamedParent(parent: PsiElement): Boolean {
        return true
    }

    override fun isAcceptableReferencedElement(element: PsiElement, referenceOrReferencedElement: PsiElement?): ThreeState {
        if(referenceOrReferencedElement is JsonPointerBasedReferenceProvider.Element) return ThreeState.NO
        return ThreeState.UNSURE
    }

    override fun getTargetCandidates(reference: PsiReference): Collection<PsiElement>? {
        if(reference is JsonPointerBasedReferenceProvider.SelfReference) return emptySet()
        return null
    }
}
