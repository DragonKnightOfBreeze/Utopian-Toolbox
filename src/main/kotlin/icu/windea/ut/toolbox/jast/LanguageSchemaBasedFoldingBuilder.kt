package icu.windea.ut.toolbox.jast

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import icu.windea.ut.toolbox.core.*

class LanguageSchemaBasedFoldingBuilder : FoldingBuilder, DumbAware {
    private fun getDelegate(language: Language): FoldingBuilder? {
        return LanguageFolding.INSTANCE.allForLanguage(language).find { it !is LanguageSchemaBasedFoldingBuilder }
    }
    
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val element = node.psi
        val delegate = getDelegate(element.language) ?: return FoldingDescriptor.EMPTY_ARRAY
        return delegate.buildFoldRegions(node, document)
    }

    override fun getPlaceholderText(node: ASTNode): String? {
        val element = node.psi
        run {
            val jElement = element.toJElement() 
            if(jElement !is JObject) return@run
            
            val languageSchema = JastManager.getLanguageSchema(element) ?: return@run
            if(languageSchema.declarationContainer.url.isEmpty()) return@run
            if(!languageSchema.declarationContainer.enableFolding) return@run
            
            var name: String? = null
            JastManager.processElementsInContainer(languageSchema.declarationContainer.url, jElement) {
                name = jElement.getName()?.orNull() ?: "(unresolved)"
                true 
            }
            if(name.isNullOrEmpty()) return@run
            return "<$name>"
        }
        val delegate = getDelegate(element.language) ?: return null
        return delegate.getPlaceholderText(node)
    }
    
    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        val element = node.psi
        val delegate = getDelegate(element.language) ?: return false
        return delegate.isCollapsedByDefault(node)
    }
}
