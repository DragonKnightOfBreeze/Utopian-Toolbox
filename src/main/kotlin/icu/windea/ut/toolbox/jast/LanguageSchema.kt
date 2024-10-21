package icu.windea.ut.toolbox.jast

data class LanguageSchema(
    val declaration: Declaration = Declaration(),
    val declarationContainer: DeclarationContainer = DeclarationContainer(),
    val reference: Reference = Reference(),
) {
    /**
     * @property id 作为声明时的标识符。
     * @property type 作为声明时的类型。以文本或者目标相对于当前节点所在数组或对象节点的JSON指针表示。
     * @property description 作为声明时的描述。以文本或者目标相对于当前节点所在数组或对象节点的JSON指针表示。
     * @property extraProperties 作为声明时的额外属性。以文本或者目标相对于当前节点所在数组或对象节点的JSON指针表示。
     * @property enableHint 是否为声明启用代码高亮。
     */
    data class Declaration(
        val id: String = "",
        val type: String = "",
        val description: String = "",
        val extraProperties: Map<String, String> = emptyMap(),
        val enableHint: Boolean = true,
    )

    /**
     * @property url 作为声明容器时，其中的声明的路径。以目标相对于当前节点的JSON指针表示。
     * @property enableFolding 是否为声明容器启用特殊的代码折叠规则。
     */
    data class DeclarationContainer(
        val url: String = "",
        val enableFolding: Boolean = true
    )

    /**
     * @property url 引用目标的路径。以目标相对于当前文件的JSON指针路径表示。
     * @property enableHint 是否为引用启用代码高亮。
     * @property enableCompletion 是否为引用启用代码补全。
     * @property enableInspection 是否为引用启用相关的代码检查。
     */
    data class Reference(
        val url: String = "",
        val enableHint: Boolean = true,
        val enableCompletion: Boolean = true,
        val enableInspection: Boolean = true,
    )
}
