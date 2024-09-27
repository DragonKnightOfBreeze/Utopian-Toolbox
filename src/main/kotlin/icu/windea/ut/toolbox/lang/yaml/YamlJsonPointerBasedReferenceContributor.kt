package icu.windea.ut.toolbox.lang.yaml

import com.intellij.patterns.PlatformPatterns.or
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import icu.windea.ut.toolbox.jast.JsonPointerBasedReferenceProvider
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.YAMLQuotedText
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl

class YamlJsonPointerBasedReferenceContributor : PsiReferenceContributor() {
    private val pattern = or(
        psiElement(YAMLTokenTypes.SCALAR_KEY),
        psiElement(YAMLPlainTextImpl::class.java),
        psiElement(YAMLQuotedText::class.java),
    )

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(pattern, JsonPointerBasedReferenceProvider())
    }
}
