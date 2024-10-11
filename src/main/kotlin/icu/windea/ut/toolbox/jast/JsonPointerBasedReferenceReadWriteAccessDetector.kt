package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

class JsonPointerBasedReferenceReadWriteAccessDetector : ReadWriteAccessDetector() {
    override fun isReadWriteAccessible(element: PsiElement): Boolean {
        return element is JsonPointerBasedReferenceProvider.Element
    }

    override fun isDeclarationWriteAccess(element: PsiElement): Boolean {
        if(element is JsonPointerBasedReferenceProvider.Element) return element.readWriteAccess == Access.Write
        return true
    }

    override fun getReferenceAccess(referencedElement: PsiElement, reference: PsiReference): Access {
        if(referencedElement is JsonPointerBasedReferenceProvider.Element)return referencedElement.readWriteAccess
        return Access.ReadWrite
    }

    override fun getExpressionAccess(expression: PsiElement): Access {
        return Access.Read
    }
}
