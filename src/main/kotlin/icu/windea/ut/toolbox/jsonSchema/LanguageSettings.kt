package icu.windea.ut.toolbox.jsonSchema

data class LanguageSettings(
    val declarations: Set<String>,
    val references: Set<String>,
    val completion: Set<String>,
)
