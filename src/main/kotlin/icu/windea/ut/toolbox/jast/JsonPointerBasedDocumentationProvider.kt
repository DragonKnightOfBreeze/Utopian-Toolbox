package icu.windea.ut.toolbox.jast

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parents

class JsonPointerBasedDocumentationProvider: DocumentationProvider {
    override fun getCustomDocumentationElement(editor: Editor, file: PsiFile, contextElement: PsiElement?, targetOffset: Int): PsiElement? {
        //用于兼容一些特殊的作为键或值的PsiElement，例如，JSON文件中的属性的值
        //参见：
        //com.intellij.codeInsight.documentation.DocumentationManager.findTargetElementAndContext
        //-> com.intellij.codeInsight.documentation.DocumentationManager.findTargetElementAtOffset
        //-> com.intellij.codeInsight.documentation.DocumentationManager.doFindTargetElementAtOffset
        //-> com.intellij.codeInsight.documentation.DocumentationManager.customElement
        
        if(contextElement == null) return null
        val jElement = contextElement.parents(true).firstNotNullOfOrNull { it.toJElement() } ?: return null
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return null
        val element = jElement.psi
        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return null
        if(languageSettings.declarationType.isEmpty() && languageSettings.references.isEmpty()) return null
        return element
    }
}
