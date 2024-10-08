package icu.windea.ut.toolbox.jast

import com.intellij.codeInspection.*
import com.intellij.psi.*
import icu.windea.ut.toolbox.UtBundle
import icu.windea.ut.toolbox.core.truncate

/**
 * @see JsonPointerBasedLanguageSettings.inspectionForReferences
 */
class UnresolvedJsonPointerBasedReferenceInspection: LocalInspectionTool() {
    object Constants {
        const val URLS_LIMIT = 3
    }
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)

                val jElement = element.toJElement()
                //if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return
                if(jElement !is JString) return

                val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return
                if(languageSettings.references.isEmpty()) return
                if(!languageSettings.inspectionForReferences) return

                val references = PsiReferenceService.getService().getContributedReferences(element) //TODO
                for(reference in references) {
                    if(reference !is JsonPointerBasedReferenceProvider.Reference) continue
                    if(reference.multiResolve(false).isNotEmpty()) continue
                    
                    val name = reference.name
                    val urls = reference.languageSettings.references.truncate(Constants.URLS_LIMIT).joinToString(", ")
                    val message = UtBundle.message("inspection.unresolvedJsonPointerBasedReference.desc", name, urls)
                    holder.registerProblem(reference, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                }
            }
        }
    }
}
