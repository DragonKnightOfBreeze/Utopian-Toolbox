package icu.windea.ut.toolbox.jast

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.util.*

/**
 * @see JsonPointerBasedLanguageSettings.references
 */
class JsonPointerBasedReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jElement = element.toJElement()
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return PsiReference.EMPTY_ARRAY

        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return PsiReference.EMPTY_ARRAY
        if(languageSettings.references.isEmpty()) return PsiReference.EMPTY_ARRAY
        
        val (name, textOffset) = jElement.getNameAndTextOffset()
        if(name.isNullOrEmpty()) return PsiReference.EMPTY_ARRAY

        val startOffset = when {
            jElement is JProperty -> jElement.keyElement?.psi?.startOffsetInParent ?: 0
            else -> 0
        }
        val range = TextRange.from(startOffset + textOffset, name.length)
        val currentFile = element.containingFile ?: return PsiReference.EMPTY_ARRAY

        return arrayOf(Reference(element, range, currentFile, jElement, name, languageSettings))
    }

    class Reference(
        element: PsiElement,
        range: TextRange,
        val currentFile: PsiFile,
        val jElement: JElement,
        val name: String,
        val languageSettings: JsonPointerBasedLanguageSettings
    ) : PsiPolyVariantReferenceBase<PsiElement>(element, range) {
        val project by lazy { element.project }

        override fun handleElementRename(newElementName: String): PsiElement {
            throw IncorrectOperationException()
        }

        //cached

        private object MultiResolver : ResolveCache.PolyVariantResolver<Reference> {
            override fun resolve(ref: Reference, incompleteCode: Boolean): Array<out ResolveResult> {
                return ref.doMultiResolve()
            }
        }

        override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
            return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
        }

        private fun doMultiResolve(): Array<out ResolveResult> {
            val result = mutableSetOf<ResolveResult>()
            languageSettings.references.forEach {
                JsonPointerManager.processElements(it, currentFile) { resolved ->
                    val (resolvedName) = resolved.getNameAndTextOffset()
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
