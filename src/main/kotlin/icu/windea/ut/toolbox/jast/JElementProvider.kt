package icu.windea.ut.toolbox.jast

import com.intellij.lang.LanguageExtension
import com.intellij.psi.PsiElement

interface JElementProvider {
    fun getJElement(element: PsiElement): JElement?
    
    companion object INSTANCE {
        val EP_NAME = LanguageExtension<JElementProvider>("icu.windea.ut.toolbox.jElementProvider")
    }
}

