package icu.windea.ut.toolbox.jast

import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import icu.windea.ut.toolbox.castOrNull
import icu.windea.ut.toolbox.util.*

object JElementManager {
    object Keys: KeyRegistry() {
        val key by createKeyDelegate<CachedValue<JElement>>(Keys)
    }
    
    fun getJElement(element: PsiElement): JElement? {
        return CachedValuesManager.getCachedValue(element, Keys.key) {
            val value = JElementProvider.EP_NAME.forLanguage(element.language).getJElement(element)
            CachedValueProvider.Result.create(value, element)
        }
    }
    
    inline fun <reified T: JElement> getJElement(element: PsiElement): T? {
        return getJElement(element)?.castOrNull<T>()
    }
}
