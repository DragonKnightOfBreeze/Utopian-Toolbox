package icu.windea.ut.toolbox.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*

@Service(Service.Level.APP)
@State(name = "UtFoldingSettings", storages = [Storage("editor.xml")], category = SettingsCategory.CODE)
class UtFoldingSettings : PersistentStateComponent<UtFoldingSettings> {
    /**
     * @see icu.windea.ut.toolbox.lang.md.EmptyMarkdownLinkFoldingBuilder
     */
    var emptyMarkdownLink = true
    /**
     * @see icu.windea.ut.toolbox.jast.LanguageSchema.DeclarationContainer.enableFolding
     */
    var declarationContainer = false

    override fun getState() = this

    override fun loadState(state: UtFoldingSettings) = XmlSerializerUtil.copyBean(state, this)

    companion object {
        @JvmStatic
        fun getInstance() = service<UtFoldingSettings>()
    }
}
