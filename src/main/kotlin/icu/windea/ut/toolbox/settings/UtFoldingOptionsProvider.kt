package icu.windea.ut.toolbox.settings

import com.intellij.application.options.editor.*
import com.intellij.openapi.options.*
import icu.windea.ut.toolbox.*

class UtFoldingOptionsProvider : BeanConfigurable<UtFoldingSettings>(
    UtFoldingSettings.getInstance(),
    UtBundle.message("settings.folding")
), CodeFoldingOptionsProvider {
    init {
        val settings = instance
        checkBox(UtBundle.message("settings.folding.emptyMarkdownLink"), settings::emptyMarkdownLink)
        checkBox(UtBundle.message("settings.folding.declarationContainer"), settings::declarationContainer)
    }
}
