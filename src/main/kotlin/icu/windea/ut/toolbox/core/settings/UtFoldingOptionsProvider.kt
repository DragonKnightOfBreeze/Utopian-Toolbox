package icu.windea.ut.toolbox.core.settings

import com.intellij.application.options.editor.CodeFoldingOptionsProvider
import com.intellij.openapi.options.BeanConfigurable
import icu.windea.ut.toolbox.UtBundle

class UtFoldingOptionsProvider : BeanConfigurable<UtFoldingSettings>(
    UtFoldingSettings.getInstance(),
    UtBundle.message("settings.folding")
), CodeFoldingOptionsProvider {

    init {
        val settings = instance
        checkBox(UtBundle.message("settings.folding.emptyMarkdownLink"), settings::emptyMarkdownLink)
    }
}
