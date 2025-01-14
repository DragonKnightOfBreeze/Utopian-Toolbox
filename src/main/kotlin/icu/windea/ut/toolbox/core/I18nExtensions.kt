package icu.windea.ut.toolbox.core

import com.intellij.openapi.util.text.*

fun String.handleTruncatedI18nPropertyValue(): String {
	val index = indexOf("\\n")
	val suffix = if(index == -1) "" else "..."
	val truncatedValue = if(index == -1) this else substring(0, index)
	return truncatedValue.replace("\\\n", "") + suffix
}

fun String.handleHtmlI18nPropertyValue(): String {
	return StringUtil.escapeXmlEntities(replace("\\\n", "")).replace("\\n", "<br>")
}
