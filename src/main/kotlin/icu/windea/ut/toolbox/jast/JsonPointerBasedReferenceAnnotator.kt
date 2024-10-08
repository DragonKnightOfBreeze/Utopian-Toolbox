package icu.windea.ut.toolbox.jast

import com.intellij.lang.annotation.*
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.psi.*
import com.intellij.refactoring.suggested.startOffset

/**
 * @see JsonPointerBasedLanguageSettings.hintForReferences
 */
class JsonPointerBasedReferenceAnnotator: Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val jElement = element.toJElement()
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return 

        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return
        if(languageSettings.references.isEmpty()) return
        if(!languageSettings.hintForReferences) return

        val references = PsiReferenceService.getService().getContributedReferences(element)
        for(reference in references) {
            if(reference !is JsonPointerBasedReferenceProvider.Reference) continue
            val range = reference.rangeInElement.shiftRight(element.startOffset)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE).create()
        }
    }
}
