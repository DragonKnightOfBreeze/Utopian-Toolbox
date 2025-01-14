package icu.windea.ut.toolbox.settings

import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.ut.toolbox.*

class UtSettingsConfigurable: BoundConfigurable(UtBundle.message("settings")), SearchableConfigurable {
    override fun getId() = "ut.toolbox"

    override fun createPanel(): DialogPanel {
        val settings = UtSettings.getInstance()
        return panel {
            group(UtBundle.message("settings.general")) {
                //indentWhenEnterInI18nText
                row {
                    checkBox(UtBundle.message("settings.general.indentWhenEnterInI18nText")).bindSelected(settings::indentWhenEnterInI18nText)
                }
            }
        }
    }
}
