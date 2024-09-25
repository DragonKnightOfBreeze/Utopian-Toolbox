package icu.windea.ut.toolbox.jsonSchema

import com.intellij.json.pointer.JsonPointerPosition
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.jsonSchema.extension.JsonLikePsiWalker
import com.jetbrains.jsonSchema.extension.adapters.JsonArrayValueAdapter
import com.jetbrains.jsonSchema.extension.adapters.JsonObjectValueAdapter
import com.jetbrains.jsonSchema.extension.adapters.JsonPropertyAdapter
import com.jetbrains.jsonSchema.extension.adapters.JsonValueAdapter
import com.jetbrains.jsonSchema.impl.JsonSchemaVariantsTreeBuilder
import icu.windea.ut.toolbox.*

object JsonPointerUrlManager {
    fun parseJsonPointer(jsonPointer: String): List<String>? {
        val s = jsonPointer.removePrefixOrNull("#/") ?: return null
        val r = s.split('/').mapNotNull { it.orNull() }
        return r
    }
    
    fun processElements(jsonPointerUrl: String, currentFile: PsiFile, processor: (PsiElement) -> Boolean): Boolean {
        val splitter = JsonSchemaVariantsTreeBuilder.SchemaUrlSplitter(jsonPointerUrl)
        val fileProcessor = { file: PsiFile ->
            processElementsInFile(splitter.relativePath, file, processor)
        }
        return if(splitter.isAbsolute) {
            processFiles(splitter.schemaId!!, currentFile, fileProcessor)
        } else {
            fileProcessor(currentFile)
        }
    }

    /**
     * @param path ANT路径表达式，相对于项目目录，或者相对于当前文件[currentFile]（此时需要以"./"开头）。
     */
    fun processFiles(path: String, currentFile: PsiFile, processor: (PsiFile) -> Boolean): Boolean {
        val isWildcard = path.contains('*') || path.contains('?')
        val fileName = path.substringAfterLast('/')
        val isWildcardFileName = fileName.contains('*') || fileName.contains('?')
        val isRelative = path.startsWith("./")
        val relPath = if(isRelative) path.removePrefix("./").trim('/') else path.trim('/')

        val project = currentFile.project
        val rootFile = if(isRelative) currentFile.parent?.virtualFile else project.guessProjectDir()
        if(rootFile == null) return true
        val absPath = "${rootFile.path}/$relPath".normalizePath()

        if(!isWildcard) {
            val vFile = VfsUtil.findRelativeFile(absPath, null) ?: return true
            if(vFile.isDirectory) return true
            val file = vFile.findPsiFile(project) ?: return true
            return processor(file)
        }
        if(!isWildcardFileName) {
            return FilenameIndex.processFilesByName(fileName, true, GlobalSearchScope.projectScope(project)) p@{ vFile ->
                if(vFile.isDirectory) return@p true
                if(!vFile.path.matchesAntPattern(absPath)) return@p true
                val file = vFile.findPsiFile(project) ?: return@p true
                processor(file)
            }
        }
        return ProjectRootManager.getInstance(project).fileIndex.iterateContentUnderDirectory(rootFile) p@{ vFile ->
            if(vFile.isDirectory) return@p true
            if (!vFile.path.matchesAntPattern(absPath)) return@p true
            val file = vFile.findPsiFile(project) ?: return@p true
            processor(file)
        }
    }

    /**
     * @see jsonPointer JSON指针。需要以"#/"开头。
     */
    fun processElementsInFile(jsonPointer: String, file: PsiFile, processor: (PsiElement) -> Boolean): Boolean {
        if(!jsonPointer.startsWith("#/")) return true
        val walker = JsonLikePsiWalker.getWalker(file) ?: return true
        val roots = walker.getRoots(file)
        if(roots.isNullOrEmpty()) return true
        val position = JsonPointerPosition.parsePointer(jsonPointer) ?: return true
        if(position.isEmpty) {
            return roots.process { root -> processor(root) }
        }
        return roots.process p@{ root ->
            val valueAdapter = walker.createValueAdapter(root)?.castOrNull<JsonArrayValueAdapter>() ?: return@p true
            doProcessElementsInFile(position, walker, valueAdapter, processor)
        }
    }

    private fun doProcessElementsInFile(
        position: JsonPointerPosition,
        walker: JsonLikePsiWalker,
        adapter: Any,
        processor: (PsiElement) -> Boolean
    ): Boolean {
        if(position.isEmpty) {
            return when(adapter) {
                is JsonPropertyAdapter -> processor(adapter.delegate)
                is JsonValueAdapter -> processor(adapter.delegate)
                else -> true
            }
        }
        
        val children = when(adapter) {
            is JsonPropertyAdapter -> adapter.values.singleOrNull()?.castOrNull<JsonObjectValueAdapter>()?.propertyList
            is JsonArrayValueAdapter -> adapter.elements
            else -> null
        }
        if(children.isNullOrEmpty()) return true
        val p = position.firstName ?: position.firstIndex.toString()
        position.skip(1)
        return children.process p@{ child ->
            //if(p == "-" && child !is )
            doProcessElementsInFile(position, walker, child, processor)
        }
    }
}
