package icu.windea.ut.toolbox.lang

import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.colors.TextAttributesKey.*

object UtAttributesKeys {
    @JvmField
    val JSON_POINTER_BASED_DECLARATION = createTextAttributesKey("UT.JSON_POINTER_BASED_DECLARATION", DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE)
    @JvmField
    val JSON_POINTER_BASED_REFERENCE = createTextAttributesKey("UT.JSON_POINTER_BASED_REFERENCE", DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE)
}
