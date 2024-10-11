package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference

class JsonPointerBasedReferenceReadWriteAccessDetector : ReadWriteAccessDetector() {
    override fun isReadWriteAccessible(element: PsiElement): Boolean {
        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return false
        return languageSettings.declarationType.isNotEmpty()
    }

    override fun isDeclarationWriteAccess(element: PsiElement): Boolean {
        return true
    }

    override fun getReferenceAccess(referencedElement: PsiElement, reference: PsiReference): Access {
        return when(reference) {
            is JsonPointerBasedReferenceProvider.SelfReference -> Access.Write
            is JsonPointerBasedReferenceProvider.Reference -> Access.Read
            else -> Access.ReadWrite
        }
    }

    override fun getExpressionAccess(expression: PsiElement): Access {
        return Access.Read
    }
}
