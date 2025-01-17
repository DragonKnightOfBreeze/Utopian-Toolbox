package icu.windea.ut.toolbox.jast

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.ut.toolbox.core.*
import icu.windea.ut.toolbox.core.util.*
import icu.windea.ut.toolbox.lang.*

object JastManager {
    object Keys : KeyRegistry() {
        val languageSchema by createKeyDelegate<CachedValue<LanguageSchema>>(Keys)
    }

    /**
     * @see jsonPointerUrl JSON指针路径，包含可选的文件路径（ANT路径表达式）与JSON指针两部分。
     */
    fun parseJsonPointerUrl(jsonPointerUrl: String): Tuple2<String?, String> {
        if (jsonPointerUrl.isEmpty() || jsonPointerUrl == "#" || jsonPointerUrl == "#/") {
            return null to ""
        }
        if (jsonPointerUrl.startsWith("#/")) {
            return null to jsonPointerUrl
        } else {
            val i = jsonPointerUrl.indexOf("#/")
            return if (i == -1) {
                (if (jsonPointerUrl.endsWith("#")) jsonPointerUrl.dropLast(1) else jsonPointerUrl) to ""
            } else {
                jsonPointerUrl.substring(0, i) to jsonPointerUrl.substring(i)
            }
        }
    }

    /**
     * @see jsonPointer JSON指针。需要以"#/"开头。"*"用于匹配任意属性，"-"用于匹配数组中的任意值或者属性的值，".."用于返回到父节点。
     */
    fun parseJsonPointer(jsonPointer: String): List<String>? {
        val s = jsonPointer.removePrefixOrNull("#/") ?: return null
        val r = s.split('/').mapNotNull { it.orNull() }
        return r
    }

    /**
     * @see jsonPointerUrl JSON指针路径，包含可选的文件路径（ANT路径表达式）与JSON指针两部分。
     */
    fun processElements(jsonPointerUrl: String, currentFile: PsiFile, processor: Processor<JElement>): Boolean {
        ProgressManager.checkCanceled()
        val (path, jsonPointer) = parseJsonPointerUrl(jsonPointerUrl)
        val fileProcessor = { file: PsiFile ->
            processElementsInFile(jsonPointer, file, processor)
        }
        return if (path != null) {
            processFiles(path, currentFile, fileProcessor)
        } else {
            fileProcessor(currentFile)
        }
    }

    /**
     * @param path ANT路径表达式，相对于项目目录，或者相对于当前文件[currentFile]（此时需要以"./"开头）。
     */
    fun processFiles(path: String, currentFile: PsiFile, processor: Processor<PsiFile>): Boolean {
        ProgressManager.checkCanceled()
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
            return processor.process(file)
        }
        if (!isWildcardFileName) {
            return FilenameIndex.processFilesByName(fileName, true, GlobalSearchScope.projectScope(project)) p@{ vFile ->
                ProgressManager.checkCanceled()
                if (vFile.isDirectory) return@p true
                if (!vFile.path.matchesAntPattern(absPath)) return@p true
                val file = vFile.findPsiFile(project) ?: return@p true
                processor.process(file)
            }
        }
        return ProjectRootManager.getInstance(project).fileIndex.iterateContentUnderDirectory(rootFile) p@{ vFile ->
            ProgressManager.checkCanceled()
            if (vFile.isDirectory) return@p true
            if (!vFile.path.matchesAntPattern(absPath)) return@p true
            val file = vFile.findPsiFile(project) ?: return@p true
            processor.process(file)
        }
    }

    /**
     * @see jsonPointer JSON指针。需要以"#/"开头。相对于[file]的顶级节点。
     */
    fun processElementsInFile(jsonPointer: String, file: PsiFile, processor: Processor<JElement>): Boolean {
        ProgressManager.checkCanceled()
        val location = parseJsonPointer(jsonPointer) ?: return true
        val topLevelValues = JElementManager.getTopLevelValues(file)
        if (topLevelValues.isEmpty()) return true
        if (location.isEmpty()) return topLevelValues.process(processor)
        return topLevelValues.process { element ->
            doProcessElements(location, 0, element, processor)
        }
    }

    /**
     * @see jsonPointer JSON指针。需要以"#/"开头。相对于[position]所属的数组或对象节点，包括自身。
     */
    fun processElementsInContainer(jsonPointer: String, position: JElement, processor: Processor<JElement>): Boolean {
        ProgressManager.checkCanceled()
        val location = parseJsonPointer(jsonPointer) ?: return true
        if (location.isEmpty()) return processor.process(position)
        val parentElement = doGetContainerElement(position, withSelf = true) ?: return true
        return doProcessElements(location, 0, parentElement, processor)
    }

    private fun doProcessElements(location: List<String>, index: Int, element: JElement, processor: Processor<JElement>): Boolean {
        ProgressManager.checkCanceled()
        val step = location.getOrNull(index) ?: return true
        if (step == "..") {
            val parentElement = doGetContainerElement(element) ?: return true
            return doProcessElements(location, index + 1, parentElement, processor)
        }
        val isLast = index == location.lastIndex
        if (isLast && step == "-" && element is JProperty) {
            val nextElement = element.valueElement ?: return true
            return processor.process(nextElement)
        }
        val nextContainerElement = when (element) {
            is JContainer -> element
            is JProperty -> element.valueElement as? JContainer
            else -> return true
        }
        val nextElements = when (nextContainerElement) {
            is JArray -> nextContainerElement.elementsSequence
            is JObject -> nextContainerElement.elementsSequence
            else -> return true
        }
        nextElements.forEach f@{ nextElement ->
            ProgressManager.checkCanceled()
            if (!matchesElement(step, nextElement)) return@f
            val r = if (isLast) {
                processor.process(nextElement)
            } else {
                doProcessElements(location, index + 1, nextElement, processor)
            }
            if (!r) return false
        }
        return true
    }

    private fun doGetContainerElement(element: JElement, withSelf: Boolean = false): JElement? {
        if(withSelf && (element is JArray || element is JObject)) return element
        return runReadAction {
            when (element) {
                is JProperty -> element.parent
                is JPropertyKey -> element.parent?.parent
                is JValue -> element.parent?.let { if (it is JProperty) it.parent else it }
                else -> null
            }
        }
    }

    fun matchesElement(step: String, element: JElement): Boolean {
        when {
            element is JProperty -> {
                if (step == "*") return true
                if (step == element.keyElement?.value) return true
            }
            element is JValue -> {
                val containerElement = element.parent ?: return true
                when {
                    containerElement is JProperty -> {
                        if (step == "-") return true
                    }
                    containerElement is JArray -> {
                        if (step == "-") return true
                        val index = containerElement.elementsSequence.indexOfFirst { it == element }
                        if (index == step.toIntOrNull()) return true
                    }
                }
            }
        }
        return false
    }
    
    fun getLanguageSchema(element: PsiElement): LanguageSchema? {
        if (UtPsiManager.isIncompletePsi()) return doGetLanguageSchema(element)
        return doGetLanguageSchemaFromCache(element)
    }

    private fun doGetLanguageSchemaFromCache(element: PsiElement): LanguageSchema? {
        return CachedValuesManager.getCachedValue(element, Keys.languageSchema) {
            val providers = LanguageSchemaProvider.EP_NAME.extensionList
            providers.firstNotNullOfOrNull p@{ provider ->
                val value = provider.getLanguageSchema(element) ?: return@p null
                val tracker = provider.getModificationTracker(element)
                val trackers = buildList {
                    add(element.containingFile ?: element)
                    tracker?.let { add(it) }
                }.toTypedArray()
                CachedValueProvider.Result.create(value, *trackers)
            }
        }
    }

    private fun doGetLanguageSchema(element: PsiElement): LanguageSchema? {
        val providers = LanguageSchemaProvider.EP_NAME.extensionList
        return providers.firstNotNullOfOrNull { it.getLanguageSchema(element) }
    }
}
