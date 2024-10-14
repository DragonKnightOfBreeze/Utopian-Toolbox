package icu.windea.ut.toolbox.jast

import com.intellij.lang.*
import com.intellij.psi.*

interface JElementProvider {
    fun getTopLevelValue(file: PsiFile): JValue?

    fun getTopLevelValues(file: PsiFile): List<JValue>

    fun getJElement(element: PsiElement, targetType: Class<out JElement>): JElement?

    companion object INSTANCE {
        val EP_NAME = LanguageExtension<JElementProvider>("icu.windea.ut.toolbox.jElementProvider")
    }
}
