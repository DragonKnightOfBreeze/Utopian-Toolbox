package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.navigation.NavigationRequest
import com.intellij.psi.*
import com.intellij.psi.impl.RenameableFakePsiElement
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.util.ProcessingContext
import icu.windea.ut.toolbox.core.castOrNull
import icu.windea.ut.toolbox.core.property
import java.util.*
import javax.swing.Icon

/**
 * @see JsonPointerBasedLanguageSettings.references
 */
class JsonPointerBasedReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jElement = element.toJElement()
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return PsiReference.EMPTY_ARRAY

        val (name, textOffset) = jElement.getNameAndTextOffset()
        if(name.isNullOrEmpty()) return PsiReference.EMPTY_ARRAY

        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return PsiReference.EMPTY_ARRAY
        if(languageSettings.declarationType.isNotEmpty()) {
            val range = jElement.getRangeInElement(name, textOffset)
            return arrayOf(SelfReference(element, range, jElement, name, languageSettings))
        } else if(languageSettings.references.isNotEmpty()) {
            val range = jElement.getRangeInElement(name, textOffset)
            val reference = Reference(element, range, jElement, name, languageSettings)
            return arrayOf(reference)
        }
        return PsiReference.EMPTY_ARRAY
    }

    @Suppress("UnstableApiUsage")
    class Element(
        parent: PsiElement,
        private val name: String,
        private val type: String,
        val readWriteAccess: Access
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

        override fun getNameIdentifier(): PsiElement {
            return this
        }

        override fun getTextRange(): TextRange? {
            return null //return null to avoid incorrect highlight at file start
        }

        override fun navigationRequest(): NavigationRequest? {
            return null //click to show usages
        }

        override fun navigate(requestFocus: Boolean) {
            //click to show usages
        }

        override fun canNavigate(): Boolean {
            return false //click to show usages
        }

        override fun equals(other: Any?): Boolean {
            if(other == null) return false
            if(this === other) return true
            return other is Element && name == other.name && type == other.type && project == other.project
        }

        override fun hashCode(): Int {
            return Objects.hash(name, type, project)
        }
    }

    class SelfReference(
        element: PsiElement,
        range: TextRange,
        val jElement: JElement,
        val name: String,
        val languageSettings: JsonPointerBasedLanguageSettings
    ) : PsiReferenceBase<PsiElement>(element, range) {
        @Suppress("RedundantOverride")
        override fun handleElementRename(newElementName: String): PsiElement {
            return super.handleElementRename(newElementName) //delegate to ElementManipulators
        }

        override fun resolve(): PsiElement {
            return Element(element, name, languageSettings.declarationType, Access.Write)
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
                JsonPointerManager.processElements(ref, currentFile) p@{ resolved ->
                    val (resolvedName) = resolved.getNameAndTextOffset()
                    if(resolvedName.isNullOrEmpty()) return@p true
                    val resolvedLanguageSettings = JsonPointerManager.getLanguageSettings(resolved.psi) ?: return@p true
                    if(name != resolvedName) return@p true
                    val resolvedElement = Element(resolved.psi, resolvedName, resolvedLanguageSettings.declarationType, Access.Read)
                    result += PsiElementResolveResult(resolvedElement)
                    true
                }
            }
            return result.toTypedArray()
        }
        
        fun getResolvedElements(): Collection<PsiElement> {
            return multiResolve(false).mapNotNull { it.element?.castOrNull<Element>()?.parent }
        } 
    }
}
