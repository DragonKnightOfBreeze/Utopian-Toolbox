package icu.windea.ut.toolbox.jast

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import com.intellij.refactoring.suggested.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.ut.toolbox.core.util.*

open class JsonPointerBasedReferenceCompletionProvider : CompletionProvider<CompletionParameters>() {
    object Keys : KeyRegistry() {
        val parameters by createKeyDelegate<CompletionParameters>(Keys)
        val keyword by createKeyDelegate<String>(Keys)
        val element by createKeyDelegate<PsiElement>(Keys)
        val jElement by createKeyDelegate<JElement>(Keys)
        val languageSettings by createKeyDelegate<JsonPointerBasedLanguageSettings>(Keys)
    }

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
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

        ProgressManager.checkCanceled()
        val currentFile = parameters.originalFile
        val resultToUse = handleResult(context, result)
        languageSettings.references.forEach { ref ->
            JsonPointerManager.processElements(ref, currentFile) p@{ resolved ->
                val (resolvedName) = resolved.getNameAndTextOffset()
                if(resolvedName.isNullOrEmpty()) return@p true

                val resolvedElement = resolved.psi

                val resolvedLanguageSettings = JsonPointerManager.getLanguageSettings(resolvedElement)
                val resolvedType = resolvedLanguageSettings?.declarationType

                val lookupElement = LookupElementBuilder.create(resolvedElement, resolvedName)
                    .withTypeText(resolvedType, true)
                val lookupElementToUse = handleLookupElement(context, lookupElement)
                resultToUse.addElement(lookupElementToUse)
                true
            }
        }
    }
    
    protected open fun handleResult(context: ProcessingContext, result: CompletionResultSet): CompletionResultSet {
        return result
    }

    protected open fun handleLookupElement(context: ProcessingContext, lookupElement: LookupElementBuilder): LookupElementBuilder {
        return lookupElement
    }
}
