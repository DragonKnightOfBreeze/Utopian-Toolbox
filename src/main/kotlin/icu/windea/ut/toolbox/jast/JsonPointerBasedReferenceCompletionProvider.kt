package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.ut.toolbox.core.util.*
import java.util.function.UnaryOperator

open class JsonPointerBasedReferenceCompletionProvider : CompletionProvider<CompletionParameters>() {
    object Keys : KeyRegistry() {
        val parameters by createKeyDelegate<CompletionParameters>(Keys)
        val keyword by createKeyDelegate<String>(Keys)
        val element by createKeyDelegate<PsiElement>(Keys)
        val jElement by createKeyDelegate<JElement>(Keys)
        val languageSettings by createKeyDelegate<JsonPointerBasedLanguageSettings>(Keys)
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        //TODO parameters.position may be incomplete
        
        val positionElement = parameters.position
        val jElement = positionElement.parents(true).firstNotNullOfOrNull { it.toJElement() } ?: return
        if(jElement !is JProperty && jElement !is JPropertyKey && jElement !is JString) return
        val element = jElement.psi

        val languageSettings = JsonPointerManager.getLanguageSettings(element) ?: return
        if(languageSettings.references.isEmpty()) return

        val keyword = element.text.take(parameters.offset - element.startOffset)

        context.put(Keys.parameters, parameters)
        context.put(Keys.keyword, keyword)
        context.put(Keys.element, element)
        context.put(Keys.jElement, jElement)
        context.put(Keys.languageSettings, languageSettings)

        val resultHandler = getResultHandler(context)
        val lookupStringHandler = getLookupStringHandler(context)
        val lookupElementHandler = getLookupElementHandler(context)

        ProgressManager.checkCanceled()
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
