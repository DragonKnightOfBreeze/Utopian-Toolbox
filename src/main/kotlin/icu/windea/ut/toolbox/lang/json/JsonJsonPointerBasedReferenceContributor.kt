package icu.windea.ut.toolbox.lang.json

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import icu.windea.ut.toolbox.jast.JsonPointerBasedReferenceProvider

class JsonJsonPointerBasedReferenceContributor : PsiReferenceContributor() {
    private val pattern = PlatformPatterns.psiElement(JsonStringLiteral::class.java)

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(pattern, JsonPointerBasedReferenceProvider())
    }
}
