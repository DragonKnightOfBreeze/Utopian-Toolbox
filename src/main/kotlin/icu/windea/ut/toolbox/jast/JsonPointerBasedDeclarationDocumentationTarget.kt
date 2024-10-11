@file:Suppress("UnstableApiUsage")

package icu.windea.ut.toolbox.jast

import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationResult
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import icu.windea.ut.toolbox.core.documentation.*

class JsonPointerBasedDeclarationDocumentationTarget(
    val element: PsiElement
) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPtr = element.createSmartPointer()
        return Pointer {
            val element = elementPtr.dereference() ?: return@Pointer null
            JsonPointerBasedDeclarationDocumentationTarget(element)
        }
    }

    override val navigatable: Navigatable?
        get() = (element.navigationElement ?: element) as? Navigatable

    override fun computePresentation(): TargetPresentation {
        return computeLocalPresentation(element) ?: TargetPresentation.builder("").presentation()
    }

    override fun computeDocumentationHint(): String? {
        return computeLocalDocumentation(element, true)
    }

    override fun computeDocumentation(): DocumentationResult {
        return DocumentationResult.asyncDocumentation {
            val html = computeLocalDocumentation(element, false) ?: return@asyncDocumentation null
            DocumentationResult.documentation(html)
        }
    }
}

private fun computeLocalPresentation(element: PsiElement): TargetPresentation? {
    val jElement = element.toJElement()
    if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null

    val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return null
    val type = languageSettings.declarationType
    if(type.isEmpty()) return null
    val (name) = jElement.getNameAndTextOffset()
    if(name.isNullOrEmpty()) return null

    return TargetPresentation.builder(name).containerText(type).presentation()
}

private const val SECTIONS_INFO = 0

private fun computeLocalDocumentation(element: PsiElement, quickNavigation: Boolean): String? {
    val jElement = element.toJElement()
    if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null

    val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return null
    val type = languageSettings.declarationType
    if(type.isEmpty()) return null
    val (name) = jElement.getNameAndTextOffset()
    if(name.isNullOrEmpty()) return null

    return buildDocumentation {
        definition {
            append(name)
        }
        if(!quickNavigation) {
            initSections(1)
            getSections(SECTIONS_INFO)?.let { infoSections ->
                infoSections["Type"] = type
            }
            buildSections()
        }
    }
}
