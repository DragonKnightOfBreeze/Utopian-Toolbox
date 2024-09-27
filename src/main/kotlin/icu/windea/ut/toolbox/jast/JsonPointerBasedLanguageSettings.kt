package icu.windea.ut.toolbox.jast

import com.intellij.openapi.util.ModificationTracker

data class JsonPointerBasedLanguageSettings(
    val references: Set<String> = emptySet(),
    val completion: Set<String> = emptySet(),
    val modificationTrackers: Set<ModificationTracker> = emptySet(),
)
