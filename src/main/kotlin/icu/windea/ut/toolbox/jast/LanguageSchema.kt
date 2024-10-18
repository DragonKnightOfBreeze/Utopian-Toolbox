package icu.windea.ut.toolbox.jast

import com.intellij.psi.*
import com.intellij.util.*

/**
 * @property declarationId 作为声明时的标识符。
 * @property declarationType 作为声明时的类型。以文本或者目标相对于当前父节点的JSON指针表示。
 * @property declarationDescription 作为声明时的描述。以文本或者目标相对于当前父节点的JSON指针表示。
 * @property declarationProperties 作为声明时的额外属性。以文本或者目标相对于当前父节点的JSON指针表示。
 * @property hintForDeclarations 是否为声明提供代码高亮。
 * @property references 引用目标的JSON指针路径，可以有多个。
 * @property hintForReferences 是否为引用提供代码高亮。
 * @property inspectionForReferences 是否为引用提供代码检查。
 * @property completionForReferences 是否为引用提供代码补全。
 */
data class LanguageSchema(
    val declarationId: String = "",
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
        return LanguageSchemaManager.resolveDeclarationType(this, position)
    }

    fun resolveDeclarationDescription(position: JElement): String {
        return LanguageSchemaManager.resolveDeclarationDescription(this, position)
    }

    fun resolveDeclarationProperties(position: JElement): Map<String, String> {
        return LanguageSchemaManager.resolveDeclarationProperties(this, position)
    }

    fun processReferences(currentFile: PsiFile, processor: Processor<JElement>): Boolean {
        return LanguageSchemaManager.processReferences(this, currentFile, processor)
    }
}
