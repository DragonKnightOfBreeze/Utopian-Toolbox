package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.refactoring.suggested.*
import com.intellij.util.*
import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.core.util.*
import icu.windea.ut.toolbox.lang.*
import java.util.function.*

open class LanguageSchemaBasedReferenceCompletionProvider : CompletionProvider<CompletionParameters>() {
    object Keys : KeyRegistry() {
        val parameters by createKeyDelegate<CompletionParameters>(Keys)
        val keyword by createKeyDelegate<String>(Keys)
        val jElement by createKeyDelegate<JElement>(Keys)
        val languageSchema by createKeyDelegate<LanguageSchema>(Keys)
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        //NOTE parameters.position may be incomplete

        val positionElement = parameters.position
        val keyword = positionElement.text.take(parameters.offset - positionElement.startOffset)
        val languageSchema = UtPsiManager.markIncompletePsi { JastManager.getLanguageSchema(positionElement) } ?: return
        if (languageSchema.reference.urls.isEmpty()) return
        if (!languageSchema.reference.enableCompletion) return

        context.put(Keys.parameters, parameters)
        context.put(Keys.keyword, keyword)
        context.put(Keys.languageSchema, languageSchema)

        val resultHandler = getResultHandler(context)
        val lookupStringHandler = getLookupStringHandler(context)
        val lookupElementHandler = getLookupElementHandler(context)

        val currentFile = parameters.originalFile
        val resultToUse = result.applyHandler(resultHandler)
        languageSchema.reference.urls.process { ref ->
            JastManager.processElements(ref, currentFile, p@{ resolved ->
                val resolvedName = resolved.getName()?.orNull() ?: return@p true
                val resolvedElement = resolved.psi

                val resolvedLanguageSchema = JastManager.getLanguageSchema(resolvedElement)
                val resolvedType = resolvedLanguageSchema?.let { LanguageSchemaManager.resolveDeclarationType(it, resolved) }

                val lookupString = resolvedName.applyHandler(lookupStringHandler)
                val lookupElement = LookupElementBuilder.create(resolvedElement, lookupString)
                    .withPresentableText(resolvedName)
                    .withTypeText(resolvedType, true)
                val lookupElementToUse = lookupElement.applyHandler(lookupElementHandler)
                resultToUse.addElement(lookupElementToUse)
                true
            })
        }
    }

    protected open fun getResultHandler(context: ProcessingContext): UnaryOperator<CompletionResultSet>? {
        return null
    }

    protected open fun getLookupStringHandler(context: ProcessingContext): UnaryOperator<String>? {
        return null
    }

    protected open fun getLookupElementHandler(context: ProcessingContext): UnaryOperator<LookupElementBuilder>? {
        return null
    }

    private fun <T> T.applyHandler(handler: UnaryOperator<T>?): T {
        return handler?.apply(this) ?: this
    }
}
