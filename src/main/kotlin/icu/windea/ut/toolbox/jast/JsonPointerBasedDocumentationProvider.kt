package icu.windea.ut.toolbox.jast

import com.intellij.lang.documentation.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*

class JsonPointerBasedDocumentationProvider : DocumentationProvider {
    override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?, targetOffset: Int): PsiElement? {
        //用于兼容一些特殊的作为键或值的PsiElement，例如，JSON文件中的属性的值
        //参见：
        //com.intellij.codeInsight.documentation.DocumentationManager.findTargetElementAndContext
        //-> com.intellij.codeInsight.documentation.DocumentationManager.findTargetElementAtOffset
        //-> com.intellij.codeInsight.documentation.DocumentationManager.doFindTargetElementAtOffset
        //-> com.intellij.codeInsight.documentation.DocumentationManager.customElement

        if (contextElement == null) return null
        val jElement = contextElement.parents(true).firstNotNullOfOrNull { it.toJElement() } ?: return null
        if (jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null
        val element = jElement.psi
        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return null
        if (languageSettings.declarationId.isEmpty() && languageSettings.references.isEmpty()) return null
        return element
    }
}
