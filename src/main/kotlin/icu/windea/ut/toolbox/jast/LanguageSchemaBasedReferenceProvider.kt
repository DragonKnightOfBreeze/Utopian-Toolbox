package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.lang.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icons.*
import icu.windea.ut.toolbox.core.*
import java.util.*
import javax.swing.*

class LanguageSchemaBasedReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val jElement = element.toJElement()
        if (jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return PsiReference.EMPTY_ARRAY

        val (name, textOffset) = jElement.getNameAndTextOffset()
        if (name.isNullOrEmpty()) return PsiReference.EMPTY_ARRAY

        val languageSchema = JastManager.getLanguageSchema(element) ?: return PsiReference.EMPTY_ARRAY
        if (languageSchema.declaration.id.isNotEmpty()) {
            val range = jElement.getRangeInElement(name, textOffset)
            return arrayOf(SelfReference(element, range, name, jElement, languageSchema))
        } else if (languageSchema.reference.url.isNotEmpty()) {
            val range = jElement.getRangeInElement(name, textOffset)
            val reference = Reference(element, range, name, jElement, languageSchema)
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
        override fun getIcon(): Icon {
            return UtIcons.Nodes.JastDeclaration
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

        override fun getLanguage(): Language {
            return parent.language
        }

        override fun getProject(): Project {
            return parent.project
        }
        
        override fun isValid(): Boolean {
            return true
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
        val languageSchema: LanguageSchema
    ) : PsiReferenceBase<PsiElement>(element, range) {
        val resolvedElement by lazy {
            val type = LanguageSchemaManager.resolveDeclarationType(languageSchema, jElement)
            val declarationId = languageSchema.declaration.id
            ReferenceElement(element, range, name, type, declarationId, Access.Write)
        }

        override fun resolve(): ReferenceElement {
            val type = LanguageSchemaManager.resolveDeclarationType(languageSchema, jElement)
            val declarationId = languageSchema.declaration.id
            return ReferenceElement(element, rangeInElement, name, type, declarationId, Access.Write)
        }
    }

    class Reference(
        element: PsiElement,
        range: TextRange,
        val name: String,
        val jElement: JElement,
        val languageSchema: LanguageSchema
    ) : PsiReferenceBase<PsiElement>(element, range) {
        val project by lazy { element.project }

        //cached

        private object Resolver : ResolveCache.AbstractResolver<Reference, ReferenceElement> {
            override fun resolve(ref: Reference, incompleteCode: Boolean): ReferenceElement? {
                return ref.doResolve()
            }
        }

        override fun resolve(): ReferenceElement? {
            return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
        }

        private fun doResolve(): ReferenceElement? {
            val currentFile = element.containingFile ?: return null
            var result: ReferenceElement? = null
            JastManager.processElements(languageSchema.reference.url, currentFile, p@{ resolved ->
                val (resolvedName, resolvedTextOffset) = resolved.getNameAndTextOffset()
                if (resolvedName.isNullOrEmpty()) return@p true
                if (name != resolvedName) return@p true
                val resolvedRange = jElement.getRangeInElement(resolvedName, resolvedTextOffset)
                val resolvedLanguageSchema = JastManager.getLanguageSchema(resolved.psi) ?: return@p true
                val resolvedDeclarationId = resolvedLanguageSchema.declaration.id
                val resolvedType = LanguageSchemaManager.resolveDeclarationType(resolvedLanguageSchema, resolved)
                val resolvedElement = ReferenceElement(resolved.psi, resolvedRange, resolvedName, resolvedType, resolvedDeclarationId, Access.Read)
                result = resolvedElement
                false
            })
            return result
        }
    }
}
