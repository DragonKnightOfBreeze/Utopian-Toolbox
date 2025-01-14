package icu.windea.ut.toolbox.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*

@Service(Service.Level.APP)
@State(name = "UtSettings", storages = [Storage("utopian-toolbox.xml")])
class UtSettings: PersistentStateComponent<UtSettings> {
    /**
     * @see icu.windea.ut.toolbox.lang.properties.EnterInPropertiesFileHandler
     */
    var indentWhenEnterInI18nText: Boolean = false

    override fun getState() = this

    override fun loadState(state: UtSettings) = XmlSerializerUtil.copyBean(state, this)

    companion object {
        @JvmStatic
        fun getInstance() = service<UtSettings>()
    }
}
