package icu.windea.ut.toolbox.jast

import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.ut.toolbox.lang.*

/**
 * @see LanguageSchema.hintForDeclarations
 * @see LanguageSchema.hintForReferences
 */
class LanguageSchemaBasedReferenceAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val jElement = element.toJElement()
        if (jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return

        val languageSchema = JastManager.getLanguageSchema(element) ?: return
        if (languageSchema.declarationId.isNotEmpty()) {
            if (!languageSchema.hintForDeclarations) return

            val (name, textOffset) = jElement.getNameAndTextOffset()
            if (name.isNullOrEmpty()) return
            val range = TextRange.from(textOffset, name.length).shiftRight(element.startOffset)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(UtAttributesKeys.JSON_POINTER_BASED_DECLARATION).create()
        } else if (languageSchema.references.isNotEmpty()) {
            if (!languageSchema.hintForReferences) return

            val references = PsiReferenceService.getService().getContributedReferences(element)
            for (reference in references) {
                if (reference !is LanguageSchemaBasedReferenceProvider.Reference) continue
                val range = reference.rangeInElement.shiftRight(element.startOffset)
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(UtAttributesKeys.JSON_POINTER_BASED_REFERENCE).create()
            }
        }
    }
}
