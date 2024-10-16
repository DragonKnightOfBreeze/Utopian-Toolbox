package icu.windea.ut.toolbox.jast

import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.refactoring.suggested.*
import icu.windea.ut.toolbox.lang.*

/**
 * @see JsonPointerBasedLanguageSettings.hintForDeclarations
 * @see JsonPointerBasedLanguageSettings.hintForReferences
 */
class JsonPointerBasedReferenceAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val jElement = element.toJElement()
        if (jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return

        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return
        if (languageSettings.declarationId.isNotEmpty()) {
            if (!languageSettings.hintForDeclarations) return

            val (name, textOffset) = jElement.getNameAndTextOffset()
            if (name.isNullOrEmpty()) return
            val range = TextRange.from(textOffset, name.length).shiftRight(element.startOffset)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(UtAttributesKeys.JSON_POINTER_BASED_DECLARATION).create()
        } else if (languageSettings.references.isNotEmpty()) {
            if (!languageSettings.hintForReferences) return

            val references = PsiReferenceService.getService().getContributedReferences(element)
            for (reference in references) {
                if (reference !is JsonPointerBasedReferenceProvider.Reference) continue
                val range = reference.rangeInElement.shiftRight(element.startOffset)
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(UtAttributesKeys.JSON_POINTER_BASED_REFERENCE).create()
            }
        }
    }
}
