@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.ut.toolbox.core

import com.google.common.util.concurrent.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.actions.BaseRefactoringAction.*
import com.intellij.util.*
import icu.windea.ut.toolbox.core.util.*
import java.util.concurrent.*
import kotlin.reflect.*

//region Common Extensions
fun String.compareToIgnoreCase(other: String): Int {
    return String.CASE_INSENSITIVE_ORDER.compare(this, other)
}

inline fun <T> cancelable(block: () -> T): T {
    try {
        return block()
    } catch (e: ExecutionException) {
        val cause = e.cause
        if (cause is ProcessCanceledException) throw cause
        throw cause ?: e
    } catch (e: UncheckedExecutionException) {
        val cause = e.cause
        if (cause is ProcessCanceledException) throw cause
        throw cause ?: e
    } catch (e: ProcessCanceledException) {
        throw e
    }
}

inline fun <T> cancelable(defaultValueOnException: (Throwable) -> T, block: () -> T): T {
    try {
        return block()
    } catch (e: ExecutionException) {
        val cause = e.cause
        if (cause is ProcessCanceledException) throw cause
        return defaultValueOnException(cause ?: e)
    } catch (e: UncheckedExecutionException) {
        val cause = e.cause
        if (cause is ProcessCanceledException) throw cause
        return defaultValueOnException(cause ?: e)
    } catch (e: ProcessCanceledException) {
        throw e
    }
}

inline fun <R> runCatchingCancelable(block: () -> R): Result<R> {
    return runCatching(block).onFailure { if (it is ProcessCanceledException) throw it }
}

inline fun <T, R> T.runCatchingCancelable(block: T.() -> R): Result<R> {
    return runCatching(block).onFailure { if (it is ProcessCanceledException) throw it }
}

//com.intellij.refactoring.actions.BaseRefactoringAction.findRefactoringTargetInEditor
fun DataContext.findElement(): PsiElement? {
    var element = this.getData(CommonDataKeys.PSI_ELEMENT)
    if (element == null) {
        val editor = this.getData(CommonDataKeys.EDITOR)
        val file = this.getData(CommonDataKeys.PSI_FILE)
        if (editor != null && file != null) {
            element = getElementAtCaret(editor, file)
        }
        val languages = this.getData(LangDataKeys.CONTEXT_LANGUAGES)
        if (element == null || element is SyntheticElement || languages == null) {
            return null
        }
    }
    return element
}

fun getDefaultProject() = ProjectManager.getInstance().defaultProject

fun getTheOnlyOpenOrDefaultProject() =
    ProjectManager.getInstance().let { it.openProjects.singleOrNull() ?: it.defaultProject }

fun <T> createCachedValue(
    project: Project = getDefaultProject(),
    trackValue: Boolean = false,
    provider: CachedValueProvider<T>
): CachedValue<T> {
    return CachedValuesManager.getManager(project).createCachedValue(provider, trackValue)
}

fun <T> T.withDependencyItems(vararg dependencyItems: Any): CachedValueProvider.Result<T> {
    if (dependencyItems.isEmpty()) return CachedValueProvider.Result.create(this, ModificationTracker.NEVER_CHANGED)
    return CachedValueProvider.Result.create(this, *dependencyItems)
}

fun String.escapeXml() = if(this.isEmpty()) "" else StringUtil.escapeXmlEntities(this)
//endregion

//region Key & DataKey Related Extensions
inline fun <T> UserDataHolder.tryPutUserData(key: Key<T>, value: T?) {
    runCatchingCancelable { putUserData(key, value) }
}

inline fun <T> UserDataHolder.getOrPutUserData(key: Key<T>, action: () -> T): T {
    val data = this.getUserData(key)
    if (data != null) return data
    val newValue = action()
    if (newValue != null) putUserData(key, newValue)
    return newValue
}

inline fun <T> UserDataHolder.getOrPutUserData(key: Key<T>, nullValue: T, action: () -> T?): T? {
    val data = this.getUserData(key)
    if (data != null) return data.takeUnless { it == nullValue }
    val newValue = action()
    if (newValue != null) putUserData(key, newValue) else putUserData(key, nullValue)
    return newValue
}

fun <T, THIS : UserDataHolder> THIS.getUserDataOrDefault(key: Key<T>): T? {
    val value = this.getUserData(key)
    return when {
        value != null -> value
        key is KeyWithDefaultValue -> key.defaultValue.also { putUserData(key, it) }
        key is KeyWithFactory<*, *> -> {
            val key0 = key.cast<KeyWithFactory<T, THIS>>()
            key0.factory(this).also { putUserData(key0, it) }
        }

        else -> null
    }
}

fun <T> ProcessingContext.getOrDefault(key: Key<T>): T? {
    val value = this.get(key)
    return when {
        value != null -> value
        key is KeyWithDefaultValue -> key.defaultValue.also { put(key, it) }
        else -> null
    }
}

inline operator fun <T> Key<T>.getValue(thisRef: UserDataHolder, property: KProperty<*>): T? =
    thisRef.getUserDataOrDefault(this)

inline operator fun <T> Key<T>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T? =
    thisRef.getOrDefault(this)

inline operator fun <T, THIS : UserDataHolder> KeyWithFactory<T, THIS>.getValue(thisRef: THIS, property: KProperty<*>): T {
    return thisRef.getUserData(this) ?: factory(thisRef).also { thisRef.putUserData(this, it) }
}

inline operator fun <T> KeyWithFactory<T, ProcessingContext>.getValue(thisRef: ProcessingContext, property: KProperty<*>): T {
    return thisRef.get(this) ?: factory(thisRef).also { thisRef.put(this, it) }
}

inline operator fun <T> Key<T>.setValue(thisRef: UserDataHolder, property: KProperty<*>, value: T?) =
    thisRef.putUserData(this, value)

inline operator fun <T> Key<T>.setValue(thisRef: ProcessingContext, property: KProperty<*>, value: T?) =
    thisRef.put(this, value)

inline operator fun <T> DataKey<T>.getValue(thisRef: DataContext, property: KProperty<*>): T? =
    thisRef.getData(this)

inline operator fun <T> DataKey<T>.getValue(thisRef: AnActionEvent, property: KProperty<*>): T? =
    thisRef.dataContext.getData(this)
//endregion

//region Psi Extensions
inline fun <R> PsiElement.toChildIterator(
    crossinline transform: (PsiElement) -> R?
): Iterator<R> {
    return object : PsiElementChildIterator<R>(this) {
        override fun toResult(element: PsiElement) = transform(element)
    }
}

abstract class PsiElementChildIterator<T>(
    private val element: PsiElement,
) : Iterator<T> {
    var current: PsiElement? = null
    var next: PsiElement? = null
    var nextResult: T? = null

    override fun next(): T {
        if (!hasNext()) throw NoSuchElementException()
        current = next
        val result = nextResult!!
        advance(current!!, false)
        return result
    }

    override fun hasNext(): Boolean {
        if (current == null && next == null) {
            advance(element.firstChild, true)
        }
        return nextResult != null
    }

    private fun advance(element: PsiElement, withSelf: Boolean) {
        var current = if (withSelf) element else element.nextSibling
        while (current != null) {
            val result = toResult(current)
            if (result != null) {
                this.next = current
                this.nextResult = result
                return
            }
            current = current.nextSibling
        }
        this.next = null
        this.nextResult = null
    }

    abstract fun toResult(element: PsiElement): T?
}
//endregion
