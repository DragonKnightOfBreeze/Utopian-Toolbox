package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.highlighting.*
import com.intellij.psi.*

class LanguageSchemaBasedReferenceReadWriteAccessDetector : ReadWriteAccessDetector() {
    override fun isReadWriteAccessible(element: PsiElement): Boolean {
        return element is LanguageSchemaBasedReferenceProvider.ReferenceElement
    }

    override fun isDeclarationWriteAccess(element: PsiElement): Boolean {
        if (element is LanguageSchemaBasedReferenceProvider.ReferenceElement) return element.readWriteAccess == Access.Write
        return true
    }

    override fun getReferenceAccess(referencedElement: PsiElement, reference: PsiReference): Access {
        return when (reference) {
            is LanguageSchemaBasedReferenceProvider.SelfReference -> Access.Write
            is LanguageSchemaBasedReferenceProvider.Reference -> Access.Read
            else -> Access.ReadWrite
        }
    }

    override fun getExpressionAccess(expression: PsiElement): Access {
        val reference = expression.references.firstOrNull()
        return when (reference) {
            is LanguageSchemaBasedReferenceProvider.SelfReference -> Access.Write
            is LanguageSchemaBasedReferenceProvider.Reference -> Access.Read
            else -> Access.ReadWrite
        }
    }
}
