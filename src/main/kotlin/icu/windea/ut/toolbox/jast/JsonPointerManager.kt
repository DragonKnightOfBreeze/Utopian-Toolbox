package icu.windea.ut.toolbox.jast

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.*
import com.jetbrains.jsonSchema.impl.JsonSchemaVariantsTreeBuilder
import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.core.util.*
import icu.windea.ut.toolbox.lang.UtPsiManager

object JsonPointerManager {
    object Keys : KeyRegistry() {
        val languageSettings by createKeyDelegate<CachedValue<JsonPointerBasedLanguageSettings>>(Keys)
    }

    /**
     * @see jsonPointer JSON指针。需要以"#/"开头。"*"用于匹配任意属性，"-"用于匹配数组中的任意值或者属性的值。
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
        ProgressManager.checkCanceled()
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
        ProgressManager.checkCanceled()
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
                ProgressManager.checkCanceled()
                if(vFile.isDirectory) return@p true
                if(!vFile.path.matchesAntPattern(absPath)) return@p true
                val file = vFile.findPsiFile(project) ?: return@p true
                processor(file)
            }
        }
        return ProjectRootManager.getInstance(project).fileIndex.iterateContentUnderDirectory(rootFile) p@{ vFile ->
            ProgressManager.checkCanceled()
            if(vFile.isDirectory) return@p true
            if(!vFile.path.matchesAntPattern(absPath)) return@p true
            val file = vFile.findPsiFile(project) ?: return@p true
            processor(file)
        }
    }

    /**
     * @see jsonPointer JSON指针。需要以"#/"开头。
     */
    fun processElementsInFile(jsonPointer: String, file: PsiFile, processor: (JElement) -> Boolean): Boolean {
        ProgressManager.checkCanceled()
        val location = parseJsonPointer(jsonPointer) ?: return true
        val topLevelValues = JElementManager.getTopLevelValues(file)
        if(topLevelValues.isEmpty()) return true
        if(location.isEmpty()) return topLevelValues.process(processor)
        return topLevelValues.process { element ->
            doProcessElementsInFile(location, 0, element, processor)
        }
    }

    private fun doProcessElementsInFile(location: List<String>, index: Int, element: JElement, processor: (JElement) -> Boolean): Boolean {
        ProgressManager.checkCanceled()
        val step = location.getOrNull(index) ?: return true
        val isLast = index == location.lastIndex
        if(isLast && step == "-" && element is JProperty) {
            val nextElement = element.valueElement ?: return true
            return processor(nextElement)
        }
        val nextContainerElement = when(element) {
            is JContainer -> element
            is JProperty -> element.valueElement as? JContainer
            else -> return true
        }
        val nextElements = when(nextContainerElement) {
            is JArray -> nextContainerElement.elementsSequence
            is JObject -> nextContainerElement.elementsSequence
            else -> return true
        }
        nextElements.forEach f@{ nextElement ->
            ProgressManager.checkCanceled()
            if(!matchesElement(step, nextElement)) return@f
            val r= if(isLast) {
                 processor(nextElement)
            } else {
                doProcessElementsInFile(location, index + 1, nextElement, processor)
            }
            if(!r) return false
        }
        return true
    }

    private fun matchesElement(step: String, element: JElement): Boolean {
        when {
            element is JProperty -> {
                if(step == "*") return true
                if(step == element.keyElement?.value) return true
            }
            element is JValue -> {
                val containerElement = element.parent ?: return true
                when {
                    containerElement is JProperty -> {
                        if(step == "-") return true
                    }
                    containerElement is JArray -> {
                        if(step == "-") return true
                        val index = containerElement.elementsSequence.indexOfFirst { it == element }
                        if(index == step.toIntOrNull()) return true
                    }
                }
            }
        }
        return false
    }

    /**
     * @see jsonPointer JSON指针。需要以"#/"开头。"*"用于匹配任意属性，"-"用于匹配数组中的任意值或者属性的值。
     */
    fun findElementsInFile(jsonPointer: String, file: PsiFile): List<JElement> {
        return buildList {
            processElements(jsonPointer, file) {
                this += it
                true
            }
        }
    }

    fun mergeLanguageSettings(list: Collection<JsonPointerBasedLanguageSettings>, modificationTrackers: Set<ModificationTracker> = emptySet()): JsonPointerBasedLanguageSettings? {
        if(list.isEmpty()) return null
        if(list.size == 1) return list.single()
        return JsonPointerBasedLanguageSettings(
            declarationType = list.firstNotNullOfOrNull { it.declarationType.orNull() }.orEmpty(),
            hintForDeclarations = list.firstNotNullOfOrNull { it.hintForDeclarations } ?: false,
            references = list.flatMapTo(mutableSetOf()) { it.references },
            hintForReferences = list.any { it.hintForReferences },
            inspectionForReferences = list.any { it.inspectionForReferences },
            completionForReferences = list.any { it.completionForReferences },
            modificationTrackers = list.flatMapTo(mutableSetOf()) { it.modificationTrackers } + modificationTrackers,
        )
    }

    fun getLanguageSettings(element: PsiElement): JsonPointerBasedLanguageSettings? {
        if(UtPsiManager.isIncompletePsi()) return doGetLanguageSettings(element)
        return doGetLanguageSettingsFromCache(element)
    }

    private fun doGetLanguageSettingsFromCache(element: PsiElement): JsonPointerBasedLanguageSettings? {
        return CachedValuesManager.getCachedValue(element, Keys.languageSettings) {
            val value = doGetLanguageSettings(element)
            val trackers = buildList {
                add(element.containingFile ?: element)
                if(value != null) addAll(value.modificationTrackers)
            }.toTypedArray()
            CachedValueProvider.Result.create(value, *trackers)
        }
    }

    private fun doGetLanguageSettings(element: PsiElement): JsonPointerBasedLanguageSettings? {
        val list = JsonPointerBasedLanguageSettingsProvider.EP_NAME.extensionList.mapNotNull { it.getLanguageSettings(element) }
        val value = mergeLanguageSettings(list)
        return value
    }
}
