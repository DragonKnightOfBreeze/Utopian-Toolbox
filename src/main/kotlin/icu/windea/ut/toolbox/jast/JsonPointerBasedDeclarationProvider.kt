@file:Suppress("UnstableApiUsage")

package icu.windea.ut.toolbox.jast

import com.intellij.model.Symbol
import com.intellij.model.psi.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

class JsonPointerBasedDeclarationProvider: PsiSymbolDeclarationProvider {
    override fun getDeclarations(element: PsiElement, offsetInElement: Int): Collection<PsiSymbolDeclaration> {
        val jElement = element.toJElement()
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return emptySet()
        
        val (name, textOffset) = jElement.getNameAndTextOffset()
        if(name.isNullOrEmpty()) return emptySet()
        
        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return emptySet()
        if(languageSettings.declarationType.isNotEmpty()) {
            val range = getRange(jElement, name, textOffset)
            val declaration = Declaration(element, range, jElement)
            return setOf(declaration)
        }
        return emptySet()
    }

    private fun getRange(jElement: JElement?, name: String, textOffset: Int): TextRange {
        val startOffset = when {
            jElement is JProperty -> jElement.keyElement?.psi?.startOffsetInParent ?: 0
            else -> 0
        }
        val range = TextRange.from(startOffset + textOffset, name.length)
        return range
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
