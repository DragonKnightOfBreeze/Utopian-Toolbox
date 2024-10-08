package icu.windea.ut.toolbox.jast

import com.intellij.openapi.util.ModificationTracker

/**
 * 基于JSON指针的扩展语言设置。
 * @property isDeclaration 是否可以作为声明，从而可在其他位置被引用。
 * @property references 引用目标的JSON指针路径，可以有多个。
 * @property hintForReferences 是否为引用提供语言高亮。
 * @property inspectionForReferences 是否为引用提供代码检查。
 * @property completionForReferences 是否为引用提供代码补全。
 */
data class JsonPointerBasedLanguageSettings(
    val isDeclaration: Boolean = false,
    val references: Set<String> = emptySet(),
    val hintForReferences: Boolean = true,
    val inspectionForReferences: Boolean = true,
    val completionForReferences: Boolean = true,
    val modificationTrackers: Set<ModificationTracker> = emptySet(),
)
