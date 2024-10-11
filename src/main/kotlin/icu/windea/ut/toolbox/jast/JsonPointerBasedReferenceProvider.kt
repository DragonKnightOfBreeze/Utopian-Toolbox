package icu.windea.ut.toolbox.jast

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.util.ProcessingContext

/**
 * @see JsonPointerBasedLanguageSettings.references
 */
@Suppress("RedundantOverride")
class JsonPointerBasedReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jElement = element.toJElement()
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return PsiReference.EMPTY_ARRAY

        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return PsiReference.EMPTY_ARRAY

        val (name, textOffset) = jElement.getNameAndTextOffset()
        if(name.isNullOrEmpty()) return PsiReference.EMPTY_ARRAY

        if(languageSettings.declarationType.isNotEmpty()) {
            val range = getRange(jElement, name, textOffset)
            return arrayOf(SelfReference(element, range, jElement, name, languageSettings))
        } else if(languageSettings.references.isNotEmpty()) {
            val range = getRange(jElement, name, textOffset)
            return arrayOf(Reference(element, range, jElement, name, languageSettings))
        }
        return PsiReference.EMPTY_ARRAY
    }

    private fun getRange(jElement: JElement?, name: String, textOffset: Int): TextRange {
        val startOffset = when {
            jElement is JProperty -> jElement.keyElement?.psi?.startOffsetInParent ?: 0
            else -> 0
        }
        val range = TextRange.from(startOffset + textOffset, name.length)
        return range
    }

    class SelfReference(
        element: PsiElement,
        range: TextRange,
        val jElement: JElement,
        val name: String,
        val languageSettings: JsonPointerBasedLanguageSettings
    ) : PsiReferenceBase<PsiElement>(element, range) {
        override fun handleElementRename(newElementName: String): PsiElement {
            return super.handleElementRename(newElementName) //delegate to ElementManipulators
        }

        override fun resolve(): PsiElement {
            return element
        }
    }

    class Reference(
        element: PsiElement,
        range: TextRange,
        val jElement: JElement,
        val name: String,
        val languageSettings: JsonPointerBasedLanguageSettings
    ) : PsiPolyVariantReferenceBase<PsiElement>(element, range) {
        val project by lazy { element.project }

        override fun handleElementRename(newElementName: String): PsiElement {
            return super.handleElementRename(newElementName) //delegate to ElementManipulators
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
            val currentFile = element.containingFile ?: return ResolveResult.EMPTY_ARRAY
            val result = mutableSetOf<ResolveResult>()
            languageSettings.references.forEach { ref ->
                JsonPointerManager.processElements(ref, currentFile) { resolved ->
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
