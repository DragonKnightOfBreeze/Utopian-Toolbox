package icu.windea.ut.toolbox.lang

object UtPsiManager {
    private val incompleteMark = ThreadLocal<Boolean>()

    fun <T> markIncompletePsi(action: () -> T): T {
        try {
            incompleteMark.set(true)
            return action()
        } finally {
            incompleteMark.remove()
        }
    }

    fun isIncompletePsi(): Boolean {
        return incompleteMark.get() == true
    }
}
