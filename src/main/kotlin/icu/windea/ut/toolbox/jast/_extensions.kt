@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.ut.toolbox.jast

import com.intellij.psi.PsiElement

inline fun PsiElement.toJElement(): JElement? = JElementManager.getJElement(this)

inline fun <reified T: JElement> PsiElement.toJElement(): T? = JElementManager.getJElement<T>(this)
