package icu.windea.ut.toolbox.lang.yaml

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import icu.windea.ut.toolbox.jast.*
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.*
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl

class YamlJElementProvider : JElementProvider {
    override fun getTopLevelValue(file: PsiFile): JValue? {
        if(file !is YAMLFile) return null
        return PsiTreeUtil.getChildOfType(file, YAMLDocument::class.java)?.topLevelValue?.toJElementOfType<JValue>()
    }

    override fun getTopLevelValues(file: PsiFile): List<JValue> {
        if(file !is YAMLFile) return emptyList()
        return PsiTreeUtil.getChildrenOfType(file, YAMLDocument::class.java)?.mapNotNull { it.topLevelValue?.toJElementOfType<JValue>() }.orEmpty()
    }

    override fun getJElement(element: PsiElement, targetType: Class<out JElement>): JElement? {
        return when {
            element is YAMLKeyValue -> when {
                targetType.isAssignableFrom(JProperty::class.java) -> YamlJProperty(element)
                else -> null
            }

            element.elementType == YAMLTokenTypes.SCALAR_KEY -> when {
                targetType.isAssignableFrom(JPropertyKey::class.java) -> YamlJPropertyKey(element)
                else -> null
            }

            element is YAMLPlainTextImpl -> when {
                targetType.isAssignableFrom(JLiteral::class.java) -> YamlJString(element)
                targetType.isAssignableFrom(JNull::class.java) -> when {
                    YamlManager.isNull(element.text) -> YamlJNull(element)
                    else -> null
                }

                targetType.isAssignableFrom(JBoolean::class.java) -> when {
                    YamlManager.toBoolean(element.text) != null -> YamlJBoolean(element)
                    else -> null
                }

                targetType.isAssignableFrom(JNumber::class.java) -> when {
                    YamlManager.toNumber(element.text) != null -> YamlJNumber(element)
                    else -> null
                }

                targetType.isAssignableFrom(JString::class.java) -> YamlJString(element)
                else -> null
            }

            element is YAMLScalar -> when {
                targetType.isAssignableFrom(JString::class.java) -> YamlJString(element)
                else -> null
            }

            element is YAMLSequence -> when {
                targetType.isAssignableFrom(JArray::class.java) -> YamlJArray(element)
                else -> null
            }

            element is YAMLMapping -> when {
                targetType.isAssignableFrom(JObject::class.java) -> YamlJObject(element)
                else -> null
            }

            else -> null
        }
    }
}
