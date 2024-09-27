package icu.windea.ut.toolbox.lang.md

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import icu.windea.ut.toolbox.settings.UtFoldingSettings
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownImage
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownInlineLink

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
                if(element is MarkdownInlineLink) {
                    if(element.parent is MarkdownImage) return //排除Markdown图片链接
                    val linkText = element.linkText
                    if(linkText != null && linkText.contentElements.none()) {
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
