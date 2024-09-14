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
import com.jetbrains.jsonSchema.ide.JsonSchemaService
import com.jetbrains.jsonSchema.impl.JsonSchemaResolver
import com.jetbrains.jsonSchema.impl.JsonSchemaVariantsTreeBuilder
import icu.windea.ut.toolbox.matchesAntPattern
import icu.windea.ut.toolbox.normalizePath

object JsonPointerUrlManager {
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
        val position = JsonPointerPosition.parsePointer(jsonPointer)
        //TODO
        return true
    }
}
