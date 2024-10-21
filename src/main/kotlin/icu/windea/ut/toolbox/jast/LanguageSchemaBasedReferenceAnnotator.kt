package icu.windea.ut.toolbox.jast

import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.ut.toolbox.lang.*

class LanguageSchemaBasedReferenceAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val jElement = element.toJElement()
        if (jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return

        val languageSchema = JastManager.getLanguageSchema(element) ?: return
        if (languageSchema.declaration.id.isNotEmpty()) {
            if (!languageSchema.declaration.enableHint) return

            val (name, textOffset) = jElement.getNameAndTextOffset()
            if (name.isNullOrEmpty()) return
            val range = TextRange.from(textOffset, name.length).shiftRight(element.startOffset)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(UtAttributesKeys.JSON_POINTER_BASED_DECLARATION).create()
        } else if (languageSchema.reference.url.isNotEmpty()) {
            if (!languageSchema.reference.enableHint) return

            val references = PsiReferenceService.getService().getContributedReferences(element)
            for (reference in references) {
                if (reference !is LanguageSchemaBasedReferenceProvider.Reference) continue
                val range = reference.rangeInElement.shiftRight(element.startOffset)
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(UtAttributesKeys.JSON_POINTER_BASED_REFERENCE).create()
            }
        }
    }
}
