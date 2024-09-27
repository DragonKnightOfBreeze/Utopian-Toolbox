package icu.windea.ut.toolbox.jast

import com.intellij.openapi.util.ModificationTracker

/**
 * 基于JSON指针的扩展语言设置。
 * @property references 用作引用的目标的JSON指针路径，可以有多个。
 */
data class JsonPointerBasedLanguageSettings(
    val references: Set<String> = emptySet(),
    val modificationTrackers: Set<ModificationTracker> = emptySet(),
)
