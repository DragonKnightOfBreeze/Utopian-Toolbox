package icu.windea.ut.toolbox.lang.md

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.ut.toolbox.settings.*
import org.intellij.plugins.markdown.lang.psi.impl.*

/**
 * 用于折叠标签文本为空的Markdown内联链接。（`[](...)`）
 */
@Suppress("UnstableApiUsage")
class EmptyMarkdownLinkFoldingBuilder : FoldingBuilderEx(), DumbAware {
    object Constants {
        private const val GROUP_NAME = "markdown.emptyLink"
        val FOLDING_GROUP = FoldingGroup.newGroup(GROUP_NAME)
    }

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        root.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is MarkdownInlineLink) {
                    if (element.parent is MarkdownImage) return //排除Markdown图片链接
                    val linkText = element.linkText
                    if (linkText != null && linkText.contentElements.none()) {
                        descriptors.add(FoldingDescriptor(element.node, element.textRange, Constants.FOLDING_GROUP))
                    }
                    return
                }
                super.visitElement(element)
            }
        })
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode) = ""

    override fun isCollapsedByDefault(node: ASTNode) = UtFoldingSettings.getInstance().emptyMarkdownLink
}
