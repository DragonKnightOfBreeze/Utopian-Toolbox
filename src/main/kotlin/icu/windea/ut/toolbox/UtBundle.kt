package icu.windea.ut.toolbox

import com.intellij.DynamicBundle
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.UtBundle"

object UtBundle : DynamicBundle(BUNDLE) {
	@Nls
	@JvmStatic
	fun message(@NonNls @PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
		return getMessage(key, *params)
	}
	
	@Nls
	@JvmStatic
	fun lazyMessage(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): () -> String {
		return { getMessage(key, *params) }
	}
}
