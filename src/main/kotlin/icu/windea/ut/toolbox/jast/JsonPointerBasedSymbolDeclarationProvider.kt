@file:Suppress("UnstableApiUsage")

package icu.windea.ut.toolbox.jast

import com.intellij.model.Symbol
import com.intellij.model.psi.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class JsonPointerBasedSymbolDeclarationProvider: PsiSymbolDeclarationProvider {
    override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<PsiSymbolDeclaration> {
        val jElement = element.toJElement()
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return emptySet()
        
        val (name, textOffset) = jElement.getNameAndTextOffset()
        if(name.isNullOrEmpty()) return emptySet()
        
        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return emptySet()
        if(languageSettings.declarationType.isNotEmpty()) {
            val range = jElement.getRangeInElement(name, textOffset)
            val declaration = Declaration(element, range, jElement)
            return setOf(declaration)
        }
        return emptySet()
    }
    
    class Declaration(
        val element: PsiElement,
        val range: TextRange,
        val jElement: JElement
    ): PsiSymbolDeclaration {
        override fun getDeclaringElement(): PsiElement {
            return element
        }

        override fun getRangeInDeclaringElement(): TextRange {
            return range
        }

        override fun getSymbol(): Symbol {
            return PsiSymbolService.getInstance().asSymbol(element)
        }
    }
}
