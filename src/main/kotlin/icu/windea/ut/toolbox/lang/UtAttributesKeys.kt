package icu.windea.ut.toolbox.lang

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey

object UtAttributesKeys {
    @JvmField val JSON_POINTER_BASED_DECLARATION = createTextAttributesKey("UT.JSON_POINTER_BASED_DECLARATION", DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE)
    @JvmField val JSON_POINTER_BASED_REFERENCE = createTextAttributesKey("UT.JSON_POINTER_BASED_REFERENCE", DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE)
}
