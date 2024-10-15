package icu.windea.ut.toolbox.jast

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*

/**
 * 基于JSON指针的扩展语言设置。
 * @property declarationType 作为声明时的类型。作为声明时，可以在其他位置被引用。
 * @property declarationDescription 作为声明时的描述。作为声明时，可以在其他位置被引用。
 * @property declarationProperties 作为声明时的额外属性。作为声明时，可以在其他位置被引用。
 * @property hintForDeclarations 是否为声明提供代码高亮。
 * @property references 引用目标的JSON指针路径，可以有多个。
 * @property hintForReferences 是否为引用提供代码高亮。
 * @property inspectionForReferences 是否为引用提供代码检查。
 * @property completionForReferences 是否为引用提供代码补全。
 */
data class JsonPointerBasedLanguageSettings(
    val declarationType: String = "",
    val declarationDescription: String = "",
    val declarationProperties: Map<String, String> = emptyMap(),
    val hintForDeclarations: Boolean = true,
    val references: Set<String> = emptySet(),
    val hintForReferences: Boolean = true,
    val inspectionForReferences: Boolean = true,
    val completionForReferences: Boolean = true,
) {
    fun resolveDeclarationType(position: JElement): String {
        return JsonPointerBasedLanguageSettingsManager.resolveDeclarationType(this, position)
    }

    fun resolveDeclarationDescription(position: JElement): String {
        return JsonPointerBasedLanguageSettingsManager.resolveDeclarationDescription(this, position)
    }

    fun resolveDeclarationProperties(position: JElement): Map<String, String> {
        return JsonPointerBasedLanguageSettingsManager.resolveDeclarationProperties(this, position)
    }

    fun processReferences(currentFile: PsiFile, processor: Processor<JElement>): Boolean {
        return JsonPointerBasedLanguageSettingsManager.processReferences(this, currentFile, processor)
    }
}
