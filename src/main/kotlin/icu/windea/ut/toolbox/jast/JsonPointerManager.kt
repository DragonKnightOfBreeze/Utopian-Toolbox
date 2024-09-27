package icu.windea.ut.toolbox.jast

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
import icu.windea.ut.toolbox.*
import icu.windea.ut.toolbox.util.*

object JsonPointerManager {
    object Keys : KeyRegistry() {
        val languageSettings by createKeyDelegate<CachedValue<JsonPointerBasedLanguageSettings>>(Keys)
    }

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
        return if (splitter.schemaId != null) {
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
        val relPath = if (isRelative) path.removePrefix("./").trim('/') else path.trim('/')

        val project = currentFile.project
        val rootFile = if (isRelative) currentFile.parent?.virtualFile else project.guessProjectDir()
        if (rootFile == null) return true
        val absPath = "${rootFile.path}/$relPath".normalizePath()

        if (!isWildcard) {
            val vFile = VfsUtil.findRelativeFile(absPath, null) ?: return true
            if (vFile.isDirectory) return true
            val file = vFile.findPsiFile(project) ?: return true
            return processor(file)
        }
        if (!isWildcardFileName) {
            return FilenameIndex.processFilesByName(fileName, true, GlobalSearchScope.projectScope(project)) p@{ vFile ->
                if (vFile.isDirectory) return@p true
                if (!vFile.path.matchesAntPattern(absPath)) return@p true
                val file = vFile.findPsiFile(project) ?: return@p true
                processor(file)
            }
        }
        return ProjectRootManager.getInstance(project).fileIndex.iterateContentUnderDirectory(rootFile) p@{ vFile ->
            if (vFile.isDirectory) return@p true
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
        if (topLevelValues.isEmpty()) return true
        if (location.isEmpty()) return topLevelValues.process(processor)
        return topLevelValues.process { element ->
            doProcessElementsInFile(location, 0, element, processor)
        }
    }

    private fun doProcessElementsInFile(location: List<String>, index: Int, element: JElement, processor: (JElement) -> Boolean): Boolean {
        val step = location.getOrNull(index) ?: return true
        val isLast = index == location.lastIndex
        val nextElements = when (element) {
            is JArray -> element.elementsSequence
            is JObject -> element.elementsSequence
            else -> return true
        }
        nextElements.forEach f@{ nextElement ->
            if (!matchesElement(step, nextElement)) return@f
            if (isLast) {
                val r = processor(nextElement)
                if (!r) return false
            } else {
                val r = doProcessElementsInFile(location, index + 1, nextElement, processor)
                if (!r) return false
            }
        }
        return true
    }

    private fun matchesElement(step: String, element: JElement): Boolean {
        when {
            element is JProperty -> {
                if (step == "*") return true
                if (step == element.keyElement?.value) return true
            }
            element is JValue -> {
                val containerElement = element.parent?.castOrNull<JArray>() ?: return false
                if (step == "-") return true
                val index = containerElement.elementsSequence.indexOfFirst { it == element }
                if (index == step.toIntOrNull()) return true
            }
        }
        return false
    }

    /**
     * @see jsonPointer JSON指针。需要以"#/"开头。
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
        if (list.isEmpty()) return null
        if (list.size == 1) return list.single()
        return JsonPointerBasedLanguageSettings(
            references = list.flatMapTo(mutableSetOf()) { it.references },
            completion = list.flatMapTo(mutableSetOf()) { it.completion },
            modificationTrackers = list.flatMapTo(mutableSetOf()) { it.modificationTrackers } + modificationTrackers,
        )
    }

    fun getLanguageSettings(element: PsiElement): JsonPointerBasedLanguageSettings? {
        return CachedValuesManager.getCachedValue(element, Keys.languageSettings) {
            val list = JsonPointerBasedLanguageSettingsProvider.EP_NAME.extensionList.mapNotNull { it.getLanguageSettings(element) }
            val value = mergeLanguageSettings(list)
            val trackers = buildList {
                add(element.containingFile ?: element)
                if (value != null) addAll(value.modificationTrackers)
            }.toTypedArray()
            CachedValueProvider.Result.create(value, *trackers)
        }
    }

    fun getNameForLanguageSettings(element: JElement): String? {
        return when {
            element is JProperty -> element.keyElement?.value
            element is JPropertyKey -> element.value
            element is JString -> element.value
            else -> null
        }
    }
}
