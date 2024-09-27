package icu.windea.ut.toolbox.jast

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

object JElementManager {
    fun getTopLevelValue(file: PsiFile): JValue? {
        val provider = JElementProvider.EP_NAME.forLanguage(file.language)
        return provider.getTopLevelValue(file)
    }

    fun getTopLevelValues(file: PsiFile): List<JValue> {
        val provider = JElementProvider.EP_NAME.forLanguage(file.language)
        return provider.getTopLevelValues(file)
    }

    fun getJElement(element: PsiElement): JElement? {
        val provider = JElementProvider.EP_NAME.forLanguage(element.language)
        return provider.getJElement(element, JElement::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : JElement> getJElement(element: PsiElement, targetType: Class<out T>): T? {
        val provider = JElementProvider.EP_NAME.forLanguage(element.language)
        return provider.getJElement(element, targetType) as? T
    }

    fun <T : JElement> getJElement(element: PsiElement, vararg targetTypes: Class<out T>): JElement? {
        val provider = JElementProvider.EP_NAME.forLanguage(element.language)
        if(targetTypes.isEmpty()) return provider.getJElement(element, JElement::class.java)
        return targetTypes.firstNotNullOfOrNull { targetType -> provider.getJElement(element, targetType) }
    }
}
