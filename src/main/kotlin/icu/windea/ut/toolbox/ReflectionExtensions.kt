@file:Suppress("UNCHECKED_CAST")

package icu.windea.ut.toolbox

import java.lang.reflect.*

inline fun tryGetField(action: () -> Field): Field? {
    return try {
        action()
    } catch(e: NoSuchFieldException) {
        null
    }
}

inline fun tryGetMethod(action: () -> Method): Method? {
    return try {
        action()
    } catch(e: NoSuchMethodException) {
        null
    }
}

fun <T : Class<*>> Type.genericType(index: Int): T? {
    return castOrNull<ParameterizedType>()?.actualTypeArguments?.getOrNull(index) as? T
}
