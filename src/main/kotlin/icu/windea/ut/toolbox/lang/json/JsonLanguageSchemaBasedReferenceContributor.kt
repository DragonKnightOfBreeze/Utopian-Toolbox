package icu.windea.ut.toolbox.lang.json

import com.intellij.json.psi.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import icu.windea.ut.toolbox.jast.*

class JsonLanguageSchemaBasedReferenceContributor : PsiReferenceContributor() {
    private val pattern = or(
        psiElement(JsonStringLiteral::class.java),
        psiElement(JsonReferenceExpression::class.java), //JSON5 unquoted property key
    )

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(pattern, LanguageSchemaBasedReferenceProvider())
    }
}
