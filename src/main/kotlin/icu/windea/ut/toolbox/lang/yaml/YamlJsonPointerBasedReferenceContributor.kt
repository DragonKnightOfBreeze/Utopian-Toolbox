package icu.windea.ut.toolbox.lang.yaml

import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import icu.windea.ut.toolbox.jast.*
import org.jetbrains.yaml.*
import org.jetbrains.yaml.psi.*
import org.jetbrains.yaml.psi.impl.*

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
