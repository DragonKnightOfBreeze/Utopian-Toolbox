package icu.windea.ut.toolbox.lang.properties

import com.intellij.lang.documentation.*
import com.intellij.lang.properties.psi.*
import com.intellij.psi.*
import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.core.documentation.*

/**
 * Properties - 在本地化文本中换行时不自动缩进（覆盖IDEA的默认实现）
 */
class PropertiesDocumentationProvider : AbstractDocumentationProvider() {
	override fun getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement?): String? {
		return when {
			element is Property -> getPropertyInfo(element)
			else -> null
		}
	}

	override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
		return when {
			element is Property -> getPropertyDoc(element)
			else -> null
		}
	}

	private fun getPropertyInfo(property: Property): String {
		return buildDocumentation {
			buildPropertyDefinition(property)
		}
	}

	private fun getPropertyDoc(property: Property): String {
		return buildDocumentation {
			buildPropertyDefinition(property)
			buildPropertyContent(property)
		}
	}

	private fun DocumentationBuilder.buildPropertyDefinition(property: Property) {
		val fileName = property.containingFile?.name
		val key = property.name ?: "(anonymous)"
        buildDocumentation {
            definition {
                if(fileName != null) {
                    append("[").append(fileName.escapeXml()).append("]")
                    append("<br>")
                }
                append(key.escapeXml())
            }
        }
	}

	private fun DocumentationBuilder.buildPropertyContent(property: Property) {
		val value = property.value
		if(value != null) {
			content {
				append(value.handleHtmlI18nPropertyValue())
			}
		}
	}
}
