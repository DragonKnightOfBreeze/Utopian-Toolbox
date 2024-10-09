package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.ut.toolbox.core.util.*
import icu.windea.ut.toolbox.lang.UtPsiManager
import java.util.function.UnaryOperator

open class JsonPointerBasedReferenceCompletionProvider : CompletionProvider<CompletionParameters>() {
    object Keys : KeyRegistry() {
        val parameters by createKeyDelegate<CompletionParameters>(Keys)
        val keyword by createKeyDelegate<String>(Keys)
        val jElement by createKeyDelegate<JElement>(Keys)
        val languageSettings by createKeyDelegate<JsonPointerBasedLanguageSettings>(Keys)
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        //NOTE parameters.position may be incomplete

        val positionElement = parameters.position
        val keyword = positionElement.text.take(parameters.offset - positionElement.startOffset)
        val languageSettings = UtPsiManager.markIncompletePsi { JsonPointerManager.getLanguageSettings(positionElement) } ?: return
        if(languageSettings.references.isEmpty()) return
        
        context.put(Keys.parameters, parameters)
        context.put(Keys.keyword, keyword)
        context.put(Keys.languageSettings, languageSettings)

        val resultHandler = getResultHandler(context)
        val lookupStringHandler = getLookupStringHandler(context)
        val lookupElementHandler = getLookupElementHandler(context)

        val currentFile = parameters.originalFile
        val resultToUse = result.applyHandler(resultHandler)
        languageSettings.references.forEach { ref ->
            JsonPointerManager.processElements(ref, currentFile) p@{ resolved ->
                val (resolvedName) = resolved.getNameAndTextOffset()
                if(resolvedName.isNullOrEmpty()) return@p true

                val resolvedElement = resolved.psi

                val resolvedLanguageSettings = JsonPointerManager.getLanguageSettings(resolvedElement)
                val resolvedType = resolvedLanguageSettings?.declarationType

                val lookupString = resolvedName.applyHandler(lookupStringHandler)
                val lookupElement = LookupElementBuilder.create(resolvedElement, lookupString)
                    .withPresentableText(resolvedName)
                    .withTypeText(resolvedType, true)
                val lookupElementToUse = lookupElement.applyHandler(lookupElementHandler)
                resultToUse.addElement(lookupElementToUse)
                true
            }
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
