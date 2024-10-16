package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.util.*
import com.intellij.platform.backend.navigation.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icu.windea.ut.toolbox.core.*
import java.util.*
import javax.swing.*

/**
 * @see JsonPointerBasedLanguageSettings.references
 */
class JsonPointerBasedReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jElement = element.toJElement()
        if (jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return PsiReference.EMPTY_ARRAY

        val (name, textOffset) = jElement.getNameAndTextOffset()
        if (name.isNullOrEmpty()) return PsiReference.EMPTY_ARRAY

        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return PsiReference.EMPTY_ARRAY
        if (languageSettings.declarationId.isNotEmpty()) {
            val range = jElement.getRangeInElement(name, textOffset)
            return arrayOf(SelfReference(element, range, name, jElement, languageSettings))
        } else if (languageSettings.references.isNotEmpty()) {
            val range = jElement.getRangeInElement(name, textOffset)
            val reference = Reference(element, range, name, jElement, languageSettings)
            return arrayOf(reference)
        }
        return PsiReference.EMPTY_ARRAY
    }

    class ReferenceElement(
        parent: PsiElement,
        private val range: TextRange,
        private val name: String,
        private val type: String,
        val declarationId: String,
        val readWriteAccess: Access,
    ) : RenameableFakePsiElement(parent), PsiNameIdentifierOwner, NavigatablePsiElement {
        override fun getIcon(): Icon? {
            return parent.getIcon(0)
        }

        override fun getName(): String {
            return name
        }

        override fun getTypeName(): String {
            return type
        }

        override fun getTextOffset(): Int {
            return parent.textRange.startOffset + range.startOffset 
        }

        override fun getTextLength(): Int {
            return name.length
        }
        
        override fun getNameIdentifier(): PsiElement {
            return this
        }

        override fun getTextRange(): TextRange {
            return TextRange.from(textOffset, textLength)
        }
        
        override fun equals(other: Any?): Boolean {
            if (other == null) return false
            if (this === other) return true
            return other is ReferenceElement && name == other.name && declarationId == other.declarationId && project == other.project
        }

        override fun hashCode(): Int {
            return Objects.hash(name, declarationId, project)
        }
    }

    class SelfReference(
        element: PsiElement,
        range: TextRange,
        val name: String,
        val jElement: JElement,
        val languageSettings: JsonPointerBasedLanguageSettings
    ) : PsiReferenceBase<PsiElement>(element, range) {
        val resolvedElement by lazy {
            val type = languageSettings.resolveDeclarationType(jElement)
            val declarationId = languageSettings.declarationId
            ReferenceElement(element, range, name, type, declarationId, Access.Write)
        }
        
        override fun resolve(): PsiElement {
            return resolvedElement
        }
    }

    class Reference(
        element: PsiElement,
        range: TextRange,
        val name: String,
        val jElement: JElement,
        val languageSettings: JsonPointerBasedLanguageSettings
    ) : PsiPolyVariantReferenceBase<PsiElement>(element, range) {
        val project by lazy { element.project }
        
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
            languageSettings.processReferences(currentFile) p@{ resolved ->
                val (resolvedName, resolvedTextOffset) = resolved.getNameAndTextOffset()
                if(resolvedName.isNullOrEmpty()) return@p true
                if (name != resolvedName) return@p true
                val resolvedRange = jElement.getRangeInElement(resolvedName, resolvedTextOffset)
                val resolvedLanguageSettings = JsonPointerManager.getLanguageSettings(resolved.psi) ?: return@p true
                val resolvedDeclarationId = resolvedLanguageSettings.declarationId
                val resolvedType = resolvedLanguageSettings.resolveDeclarationType(resolved)
                val resolvedElement = ReferenceElement(resolved.psi, resolvedRange, resolvedName, resolvedType, resolvedDeclarationId, Access.Read)
                result += PsiElementResolveResult(resolvedElement)
                true
            }
            return result.toTypedArray()
        }

        fun getResolvedElements(): Collection<PsiElement> {
            return multiResolve(false).mapNotNull { it.element?.castOrNull<ReferenceElement>()?.parent }
        }
    }
}
