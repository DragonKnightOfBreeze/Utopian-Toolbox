package icons

import com.intellij.ui.*
import javax.swing.*

object UtIcons {
    object Nodes {
        @JvmField val JastDeclaration = loadIcon("icons/nodes/jastDeclaration.svg")
        @JvmField val JastDeclarationContainer = loadIcon("icons/nodes/jastDeclarationContainer.svg")
    }

    @JvmStatic
    fun loadIcon(path: String): Icon {
        return IconManager.getInstance().getIcon(path, UtIcons.javaClass.classLoader)
    }
}
