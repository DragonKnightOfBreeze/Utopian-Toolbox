package icu.windea.ut.toolbox.lang.json

import com.intellij.json.psi.JsonReferenceExpression
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns.or
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import icu.windea.ut.toolbox.jast.JsonPointerBasedReferenceProvider

class JsonJsonPointerBasedReferenceContributor : PsiReferenceContributor() {
    private val pattern = or(
        psiElement(JsonStringLiteral::class.java),
        psiElement(JsonReferenceExpression::class.java), //JSON5 unquoted property key
    )
    
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(pattern, JsonPointerBasedReferenceProvider())
    }
}
