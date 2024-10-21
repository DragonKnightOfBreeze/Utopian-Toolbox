package icu.windea.ut.toolbox.jast

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.ut.toolbox.*

class UnresolvedLanguageSchemaBasedReferenceInspection : LocalInspectionTool() {
    object Constants {
        const val URLS_LIMIT = 3
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)

                val jElement = element.toJElement()
                if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return

                val languageSchema = JastManager.getLanguageSchema(element) ?: return
                if (languageSchema.reference.url.isEmpty()) return
                if (!languageSchema.reference.enableInspection) return

                val references = PsiReferenceService.getService().getContributedReferences(element)
                for (reference in references) {
                    if (reference !is LanguageSchemaBasedReferenceProvider.Reference) continue
                    if (reference.resolve() != null) continue

                    val name = reference.name
                    val url = reference.languageSchema.reference.url
                    val message = UtBundle.message("inspection.unresolvedLanguageSchemaBasedReference.desc", name, url)
                    holder.registerProblem(reference, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                }
            }
        }
    }
}
