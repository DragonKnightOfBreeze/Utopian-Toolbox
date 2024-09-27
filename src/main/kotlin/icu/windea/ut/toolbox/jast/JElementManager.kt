package icu.windea.ut.toolbox.jast

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import icu.windea.ut.toolbox.util.*

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

    fun <T : JElement> getJElement(element: PsiElement, targetType: Class<out T>): T? {
        val result = getJElement(element, targetType) ?: return null
        return result
    }
    
    fun <T : JElement> getJElement(element: PsiElement, vararg targetTypes: Class<out T>): JElement? {
        if(targetTypes.isEmpty()) return getJElement(element)
        return targetTypes.firstNotNullOfOrNull { targetType -> getJElement(element, targetType)  }
    }
}
