package icu.windea.ut.toolbox.jast

import com.intellij.openapi.util.ModificationTracker

/**
 * 基于JSON指针的扩展语言设置。
 * @property declarationType 作为声明时的类型。作为声明时，可以在其他位置被引用。
 * @property hintForDeclarations 是否为声明提供代码高亮。
 * @property declarationForKey 是否将属性的键作为声明。
 * @property references 引用目标的JSON指针路径，可以有多个。
 * @property hintForReferences 是否为引用提供代码高亮。
 * @property inspectionForReferences 是否为引用提供代码检查。
 * @property completionForReferences 是否为引用提供代码补全。
 */
data class JsonPointerBasedLanguageSettings(
    val declarationType: String = "",
    val hintForDeclarations: Boolean = true,
    val declarationForKey: Boolean = false,
    val references: Set<String> = emptySet(),
    val hintForReferences: Boolean = true,
    val inspectionForReferences: Boolean = true,
    val completionForReferences: Boolean = true,
    val modificationTrackers: Set<ModificationTracker> = emptySet(),
) {
    fun checkDeclarationForKey(element: JElement): Boolean {
        return when {
            element is JProperty || element is JPropertyKey -> declarationForKey
            element is JValue -> !declarationForKey
            else -> false
        }
    }
}
