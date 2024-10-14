package icu.windea.ut.toolbox.lang.yaml

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.*
import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.jast.*
import org.jetbrains.yaml.*
import org.jetbrains.yaml.psi.*
import org.jetbrains.yaml.psi.impl.*
import java.util.function.*

class YamlJsonPointerBasedReferenceCompletionContributor : CompletionContributor() {
    private val pattern = or(
        psiElement(YAMLTokenTypes.SCALAR_KEY),
        psiElement().withParent(YAMLPlainTextImpl::class.java),
        psiElement().withParent(YAMLQuotedText::class.java),
    )

    init {
        extend(CompletionType.BASIC, pattern, Provider())
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }

    class Provider : JsonPointerBasedReferenceCompletionProvider() {
        override fun getResultHandler(context: ProcessingContext): UnaryOperator<CompletionResultSet>? {
            val keyword = context.get(Keys.keyword) ?: return null
            val quoted = keyword.isLeftQuoted('"') || keyword.isLeftQuoted('"')
            if (!quoted) return null

            return UnaryOperator { it.withPrefixMatcher(keyword.drop(1)) }
        }

        override fun getLookupElementHandler(context: ProcessingContext): UnaryOperator<LookupElementBuilder>? {
            val keyword = context.get(Keys.keyword) ?: return null
            val quoted = keyword.isLeftQuoted('"') || keyword.isLeftQuoted('"')
            if (!quoted) return null

            val quoteChar = keyword.firstOrNull() ?: '"'
            return UnaryOperator {
                it.withInsertHandler { c, _ ->
                    val editor = c.editor
                    val caretOffset = editor.caretModel.offset
                    val charsSequence = editor.document.charsSequence
                    val rightQuoted = charsSequence.get(caretOffset) == quoteChar && charsSequence.get(caretOffset - 1) != '\\'
                    if (rightQuoted) {
                        //将光标移到右引号之后
                        editor.caretModel.moveToOffset(caretOffset + 1)
                    } else {
                        //插入缺失的右引号，然后将光标移到右引号之后
                        EditorModificationUtil.insertStringAtCaret(editor, quoteChar.toString(), false)
                    }
                }
            }
        }
    }
}
