package icu.windea.ut.toolbox.lang.json

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.json.json5.*
import com.intellij.json.psi.*
import com.intellij.openapi.editor.*
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.*
import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.jast.*
import java.util.function.*

class JsonJsonPointerBasedReferenceCompletionContributor : CompletionContributor() {
    private val keyPattern = or(
        psiElement().afterLeaf("{", ",").withSuperParent(2, JsonProperty::class.java), //after comma or brace in property
    )
    private val stringPattern = or(
        psiElement().afterLeaf(":").withSuperParent(2, JsonProperty::class.java), //after colon in property
        psiElement().afterLeaf("[", ",").withSuperParent(2, JsonArray::class.java), //after comma or bracket in array
    )

    init {
        extend(CompletionType.BASIC, keyPattern, Provider(true))
        extend(CompletionType.BASIC, stringPattern, Provider(false))
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }

    class Provider(
        private val isKey: Boolean
    ) : JsonPointerBasedReferenceCompletionProvider() {
        override fun getResultHandler(context: ProcessingContext): UnaryOperator<CompletionResultSet>? {
            val keyword = context.get(Keys.keyword) ?: return null
            val shouldQuoted = shouldQuoted(context)
            if (!shouldQuoted) return null
            val quoted = keyword.isLeftQuoted('"') || keyword.isLeftQuoted('"')
            if (!quoted) return null

            return UnaryOperator { it.withPrefixMatcher(keyword.drop(1)) }
        }

        override fun getLookupStringHandler(context: ProcessingContext): UnaryOperator<String>? {
            val keyword = context.get(Keys.keyword) ?: return null
            val shouldQuoted = shouldQuoted(context)
            if (!shouldQuoted) return null
            val quoted = keyword.isLeftQuoted('"') || keyword.isLeftQuoted('"')
            if (quoted) return null

            val quoteChar = '"'
            return UnaryOperator { it.quote(quoteChar) }
        }

        override fun getLookupElementHandler(context: ProcessingContext): UnaryOperator<LookupElementBuilder>? {
            val keyword = context.get(Keys.keyword) ?: return null
            val shouldQuoted = shouldQuoted(context)
            if (!shouldQuoted) return null
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

        private fun shouldQuoted(context: ProcessingContext): Boolean {
            val parameters = context.get(Keys.parameters) ?: return true
            return !isKey || parameters.originalFile.language != Json5Language.INSTANCE
        }
    }
}
