package icu.windea.ut.toolbox.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "UtFoldingSettings", storages = [Storage("editor.xml")], category = SettingsCategory.CODE)
class UtFoldingSettings : PersistentStateComponent<UtFoldingSettings>{
    /**
     * @see icu.windea.ut.toolbox.md.EmptyMarkdownLinkFoldingBuilder
     */
	var emptyMarkdownLink = true
	
	override fun getState() = this
	
	override fun loadState(state: UtFoldingSettings) = XmlSerializerUtil.copyBean(state, this)
	
	companion object {
		@JvmStatic
		fun getInstance() = service<UtFoldingSettings>()
	}
}
