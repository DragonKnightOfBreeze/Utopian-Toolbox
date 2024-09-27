package icu.windea.ut.toolbox.jast

import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.jsonSchema.impl.JsonSchemaVariantsTreeBuilder
import icu.windea.ut.toolbox.*

object JsonPointerManager {
    /**
     * @see jsonPointer JSON指针。需要以"#/"开头。
     */
    fun parseJsonPointer(jsonPointer: String): List<String>? {
        val s = jsonPointer.removePrefixOrNull("#/") ?: return null
        val r = s.split('/').mapNotNull { it.orNull() }
        return r
    }
    
    /**
     * @see jsonPointerUrl JSON指针路径，包含可选的文件路径（ANT路径表达式）与JSON指针两部分。
     */
    fun processElements(jsonPointerUrl: String, currentFile: PsiFile, processor: (JElement) -> Boolean): Boolean {
        val splitter = JsonSchemaVariantsTreeBuilder.SchemaUrlSplitter(jsonPointerUrl)
        val fileProcessor = { file: PsiFile ->
            processElementsInFile(splitter.relativePath, file, processor)
        }
        return if(splitter.schemaId != null) {
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
    fun processElementsInFile(jsonPointer: String, file: PsiFile, processor: (JElement) -> Boolean): Boolean {
        val location = parseJsonPointer(jsonPointer) ?: return true
        val topLevelValues = JElementManager.getTopLevelValues(file)
        if(topLevelValues.isEmpty()) return true
        return doProcessElementsInFile(location, -1, topLevelValues, processor)
    }
    
    private fun doProcessElementsInFile(location: List<String>, index: Int, elements: List<JElement>, processor: (JElement) -> Boolean): Boolean {
        if(index == -1) return elements.process(processor)
        
        //TODO
        val step = location.getOrNull(index) ?: return true
        return elements.process p@{
            processor(it)
            doProcessElementsInFile(location, index+1, elements, processor)
        }
    }
}
