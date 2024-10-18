@file:Suppress("UnstableApiUsage")

package icu.windea.ut.toolbox.jast

import com.intellij.model.*
import com.intellij.platform.backend.documentation.*
import com.intellij.platform.backend.presentation.*
import com.intellij.pom.*
import com.intellij.psi.*
import icu.windea.ut.toolbox.core.documentation.*

class LanguageSchemaBasedDeclarationDocumentationTarget(
    val element: PsiElement
) : DocumentationTarget {
    override fun createPointer(): Pointer<out DocumentationTarget> {
        val elementPtr = element.createSmartPointer()
        return Pointer {
            val element = elementPtr.dereference() ?: return@Pointer null
            LanguageSchemaBasedDeclarationDocumentationTarget(element)
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
    if (jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null

    val languageSchema = JastManager.getLanguageSchema(element) ?: return null
    if (languageSchema.declaration.id.isEmpty()) return null
    val name = jElement.getName()
    if (name.isNullOrEmpty()) return null
    val type = LanguageSchemaManager.resolveDeclarationType(languageSchema, jElement)

    return TargetPresentation.builder(name).containerText(type).presentation()
}

private const val SECTIONS_INFO = 0

private fun computeLocalDocumentation(element: PsiElement, quickNavigation: Boolean): String? {
    val jElement = element.toJElement()
    if (jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null

    val languageSchema = JastManager.getLanguageSchema(element) ?: return null
    if (languageSchema.declaration.id.isEmpty()) return null
    val name = jElement.getName()
    if (name.isNullOrEmpty()) return null
    val type = LanguageSchemaManager.resolveDeclarationType(languageSchema, jElement)
    val description = LanguageSchemaManager.resolveDeclarationDescription(languageSchema, jElement)

    return buildDocumentation {
        definition {
            append(name)
            grayed { append(" (").append(type).append(")") }
        }
        if (description.isNotEmpty()) {
            content {
                append(description)
            }
        }
        if (!quickNavigation) {
            initSections(1)
            getSections(SECTIONS_INFO)?.let { infoSections ->
                val properties = LanguageSchemaManager.resolveDeclarationExtraProperties(languageSchema, jElement)
                if (properties.isNotEmpty()) {
                    properties.forEach { (k, v) ->
                        if (k.isNotEmpty()) {
                            infoSections[k] = v
                        }
                    }
                }
            }
            buildSections()
        }
    }
}
