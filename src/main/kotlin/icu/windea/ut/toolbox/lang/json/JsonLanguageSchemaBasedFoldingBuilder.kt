package icu.windea.ut.toolbox.lang.json

import com.intellij.json.psi.JsonObject
import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import icu.windea.ut.toolbox.jast.*
import icu.windea.ut.toolbox.settings.*

class JsonLanguageSchemaBasedFoldingBuilder: FoldingBuilder, DumbAware {
    override fun buildFoldRegions(node: ASTNode, document: Document): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()
        collectDescriptorsRecursively(node, document, descriptors)
        return descriptors.toTypedArray()
    }

    private fun collectDescriptorsRecursively(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>) {
        run {
            val element = node.psi
            if(element !is JsonObject) return@run
            val jElement = element.toJElementOfType<JObject>() ?: return@run
            val languageSchema = JastManager.getLanguageSchema(jElement.psi) ?: return@run 
            val declarationContainerText = LanguageSchemaManager.resolveDeclarationContainerText(languageSchema, jElement)
            if(declarationContainerText.isEmpty()) return@run
            val placeholderText = "<$declarationContainerText>"
            descriptors += FoldingDescriptor(node, node.textRange, null, placeholderText)
        }
        for (child in node.getChildren(null)) {
            collectDescriptorsRecursively(child, document, descriptors)
        }
    }
    
    override fun getPlaceholderText(node: ASTNode): String? {
        return null
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return UtFoldingSettings.getInstance().declarationContainer
    }
}
