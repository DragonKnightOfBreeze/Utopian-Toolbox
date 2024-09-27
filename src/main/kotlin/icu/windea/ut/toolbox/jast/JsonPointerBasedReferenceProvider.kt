package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.highlighting.HighlightedReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.util.IncorrectOperationException
import com.intellij.util.ProcessingContext

class JsonPointerBasedReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jElement = element.toJElement()
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return PsiReference.EMPTY_ARRAY

        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return PsiReference.EMPTY_ARRAY
        if(languageSettings.references.isEmpty()) return PsiReference.EMPTY_ARRAY

        val startOffset = when {
            jElement is JProperty -> jElement.keyElement?.psi?.startOffsetInParent ?: 0
            else -> 0
        }
        val text = when {
            jElement is JProperty -> jElement.keyElement?.psi?.text
            else -> jElement.psi.text
        }
        if(text.isNullOrEmpty()) return PsiReference.EMPTY_ARRAY
        val name = JsonPointerManager.getNameForLanguageSettings(jElement)
        if(name.isNullOrEmpty()) return PsiReference.EMPTY_ARRAY
        val nameInTextOffset = text.indexOf(name)
        if(nameInTextOffset == -1) return PsiReference.EMPTY_ARRAY
        val range = TextRange.from(startOffset + nameInTextOffset, name.length)
        val currentFile = element.containingFile ?: return PsiReference.EMPTY_ARRAY

        return arrayOf(Reference(element, range, currentFile, jElement, name, languageSettings))
    }

    class Reference(
        element: PsiElement,
        range: TextRange,
        private val currentFile: PsiFile,
        private val jElement: JElement,
        private val name: String,
        private val languageSettings: JsonPointerBasedLanguageSettings
    ) : PsiPolyVariantReferenceBase<PsiElement>(element, range), HighlightedReference {
        override fun handleElementRename(newElementName: String): PsiElement {
            throw IncorrectOperationException()
        }

        override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
            val result = mutableSetOf<ResolveResult>()
            languageSettings.references.forEach {
                JsonPointerManager.processElements(it, currentFile) { resolved ->
                    val resolvedName = JsonPointerManager.getNameForLanguageSettings(resolved)
                    if(name == resolvedName) {
                        result += PsiElementResolveResult(resolved.psi)
                    }
                    true
                }
            }
            return result.toTypedArray()
        }
    }
}
