package icu.windea.ut.toolbox.lang.yaml

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.patterns.PlatformPatterns.or
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.util.ProcessingContext
import icu.windea.ut.toolbox.jast.JsonPointerBasedReferenceCompletionProvider
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.YAMLQuotedText
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl

class YamlJsonPointerBasedReferenceCompletionContributor : CompletionContributor() {
    private val pattern = or(
        psiElement(YAMLTokenTypes.SCALAR_KEY),
        psiElement(YAMLTokenTypes.TEXT).withParent(YAMLPlainTextImpl::class.java),
        psiElement(YAMLTokenTypes.SCALAR_STRING).withParent(YAMLQuotedText::class.java),
        psiElement(YAMLTokenTypes.SCALAR_DSTRING).withParent(YAMLQuotedText::class.java),
    )

    init {
        extend(CompletionType.BASIC, pattern, Provider())
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }

    class Provider : JsonPointerBasedReferenceCompletionProvider() {
        override fun handleResult(context: ProcessingContext, result: CompletionResultSet): CompletionResultSet {
            val keyword = context.get(Keys.keyword) ?: return result
            if(keyword.startsWith('\'') || keyword.startsWith('"')) {
                return result.withPrefixMatcher(keyword.drop(1))
            }
            return result
        }

        override fun handleLookupElement(context: ProcessingContext, lookupElement: LookupElementBuilder): LookupElementBuilder {
            val keyword = context.get(Keys.keyword) ?: return lookupElement
            if(keyword.startsWith('\'') || keyword.startsWith('"')) {
                val quoteChar = keyword.first()
                return lookupElement.withInsertHandler { c, _ -> applyQuotedKeyOrValueInsertHandler(c, quoteChar) }
            }
            return lookupElement
        }

        private fun applyQuotedKeyOrValueInsertHandler(c: InsertionContext, quoteChar: Char) {
            val editor = c.editor
            val caretOffset = editor.caretModel.offset
            val charsSequence = editor.document.charsSequence
            val rightQuoted = charsSequence.get(caretOffset) == quoteChar && charsSequence.get(caretOffset - 1) != '\\'
            if(rightQuoted) {
                //将光标移到右引号之后
                editor.caretModel.moveToOffset(caretOffset + 1)
            } else {
                //插入缺失的右引号，然后将光标移到右引号之后
                EditorModificationUtil.insertStringAtCaret(editor, quoteChar.toString(), false)
            }
        }
    }
}
