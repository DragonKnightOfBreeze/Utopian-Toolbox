@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.ut.toolbox.core

import com.google.common.cache.*
import com.intellij.util.*
import icu.windea.ut.toolbox.core.util.*
import java.io.*
import java.nio.file.*

inline fun <reified T> Any?.cast(): T = this as T

inline fun <reified T> Any?.castOrNull(): T? = this as? T

inline fun <T : CharSequence> T.orNull() = this.takeIf { it.isNotEmpty() }

fun CharSequence.surroundsWith(prefix: Char, suffix: Char, ignoreCase: Boolean = false): Boolean {
    return startsWith(prefix, ignoreCase) && endsWith(suffix, ignoreCase)
}

fun CharSequence.surroundsWith(prefix: CharSequence, suffix: CharSequence, ignoreCase: Boolean = false): Boolean {
    return endsWith(suffix, ignoreCase) && startsWith(prefix, ignoreCase) //先匹配后缀，这样可能会提高性能
}

fun CharSequence.removeSurrounding(prefix: CharSequence, suffix: CharSequence): CharSequence {
    return removePrefix(prefix).removeSuffix(suffix)
}

fun String.removeSurrounding(prefix: CharSequence, suffix: CharSequence): String {
    return removePrefix(prefix).removeSuffix(suffix)
}

fun CharSequence.removePrefixOrNull(prefix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (startsWith(prefix, ignoreCase)) substring(prefix.length) else null
}

fun String.removePrefixOrNull(prefix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (startsWith(prefix, ignoreCase)) substring(prefix.length) else null
}

fun CharSequence.removeSuffixOrNull(suffix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (endsWith(suffix, ignoreCase)) substring(0, length - suffix.length) else null
}

fun String.removeSuffixOrNull(suffix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (endsWith(suffix, ignoreCase)) substring(0, length - suffix.length) else null
}

fun CharSequence.removeSurroundingOrNull(prefix: CharSequence, suffix: CharSequence, ignoreCase: Boolean = false): String? {
    return if (surroundsWith(prefix, suffix, ignoreCase)) substring(prefix.length, length - suffix.length) else null
}

fun String.removeSurroundingOrNull(prefix: CharSequence, suffix: CharSequence): String? {
    return if (surroundsWith(prefix, suffix)) substring(prefix.length, length - suffix.length) else null
}

fun String.isLeftQuoted(quoteChar: Char = '"'): Boolean {
    return startsWith(quoteChar)
}

fun String.isRightQuoted(quoteChar: Char = '"'): Boolean {
    return length > 1 && endsWith(quoteChar) && run {
        var n = 0
        for (i in (lastIndex - 1) downTo 0) {
            if (this[i] == '\\') n++ else break
        }
        n % 2 == 0
    }
}

fun String.isQuoted(quoteChar: Char = '"'): Boolean {
    return isLeftQuoted(quoteChar) || isRightQuoted(quoteChar)
}

fun String.quote(quoteChar: Char = '"'): String {
    val s = this
    if (s.isEmpty() || s == quoteChar.toString()) return "$quoteChar$quoteChar"
    val start = isLeftQuoted(quoteChar)
    val end = isRightQuoted(quoteChar)
    if (start && end) return s
    return buildString {
        append(quoteChar)
        s.forEach { c ->
            when (c) {
                quoteChar -> append("\\$quoteChar")
                '\\' -> append("\\\\")
                else -> append(c)
            }
        }
        append(quoteChar)
    }
}

fun String.unquote(quoteChar: Char = '"'): String {
    val s = this
    if (s.isEmpty() || s == quoteChar.toString()) return ""
    val start = isLeftQuoted(quoteChar)
    val end = isRightQuoted(quoteChar)
    return buildString {
        var escape = false
        s.forEachIndexed f@{ i, c ->
            if (start && i == 0) return@f
            if (end && i == s.lastIndex) return@f
            if (escape) {
                escape = false
                when (c) {
                    quoteChar -> append(c)
                    '\\' -> append(c)
                    else -> append('\\').append(c)
                }
            } else if (c == '\\') {
                escape = true
            } else {
                append(c)
            }
        }
    }
}

fun String.truncate(limit: Int, ellipsis: String = "..."): String {
    return if (this.length <= limit) this else this.take(limit) + ellipsis
}

fun String.toFile() = File(this)

fun String.toFileOrNull() = runCatchingCancelable { File(this) }.getOrNull()

fun String.toPath() = Path.of(this)

fun String.toPathOrNull() = runCatchingCancelable { Path.of(this) }.getOrNull()

fun String.toClass() = Class.forName(this)

fun String.toKClass() = Class.forName(this).kotlin

/**
 * 判断当前输入是否匹配指定的通配符表达式。使用"?"匹配单个字符，使用"*"匹配任意个字符。
 */
fun String.matchesPattern(pattern: String, ignoreCase: Boolean = false): Boolean {
    if (pattern.isEmpty() && this.isNotEmpty()) return false
    if (pattern == "*") return true
    val cache = if (ignoreCase) patternToRegexCache2 else patternToRegexCache1
    val path0 = this
    val pattern0 = pattern
    return cache.get(pattern0).matches(path0)
}

private val patternToRegexCache1 = CacheBuilder.newBuilder().maximumSize(10000)
    .buildCache<String, Regex> { it.patternToRegexString().toRegex() }
private val patternToRegexCache2 = CacheBuilder.newBuilder().maximumSize(10000)
    .buildCache<String, Regex> { it.patternToRegexString().toRegex(RegexOption.IGNORE_CASE) }

private fun String.patternToRegexString(): String {
    val s = this
    return buildString {
        append("\\Q")
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c == '*' -> append("\\E.*\\Q")
                c == '?' -> append("\\E.\\Q")
                else -> append(c)
            }
            i++
        }
        append("\\E")
    }
}

/**
 * 判断当前输入是否匹配指定的ANT表达式。使用"?"匹配单个子路径中的单个字符，使用"*"匹配单个子路径中的任意个字符，使用"**"匹配任意个字符。
 */
fun String.matchesAntPattern(pattern: String, ignoreCase: Boolean = false, trimSeparator: Boolean = true): Boolean {
    if (pattern.isEmpty() && this.isNotEmpty()) return false
    if (pattern == "**") return true
    val cache = if (ignoreCase) antPatternToRegexCache2 else antPatternToRegexCache1
    val path0 = this.let { if (trimSeparator) it.trim('/') else it }
    val pattern0 = pattern.let { if (trimSeparator) it.trim('/') else it }
    return cache.get(pattern0).matches(path0)
}

private val antPatternToRegexCache1 = CacheBuilder.newBuilder().maximumSize(10000)
    .buildCache<String, Regex> { it.antPatternToRegexString().toRegex() }
private val antPatternToRegexCache2 = CacheBuilder.newBuilder().maximumSize(10000)
    .buildCache<String, Regex> { it.antPatternToRegexString().toRegex(RegexOption.IGNORE_CASE) }

private fun String.antPatternToRegexString(): String {
    val s = this
    var r = buildString {
        append("\\Q")
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c == '*' -> {
                    val nc = s.getOrNull(i + 1)
                    if (nc == '*') {
                        i++
                        append("\\E.*\\Q")
                    } else {
                        append("\\E[^/]*\\Q")
                    }
                }

                c == '?' -> append("\\E[^/]\\Q")
                else -> append(c)
            }
            i++
        }
        append("\\E")
    }
    r = r.replace("\\E\\Q", "")
    r = r.replace("/\\E.*\\Q/", "\\E(/[^/]*)*\\Q")
    return r
}

/**
 * 规范化当前路径。将路径分隔符统一替换成"/"，并去除所有作为前后缀的分隔符。
 */
fun String.normalizePath(): String {
    val builder = StringBuilder()
    var separatorFlag = false
    this.trim('/', '\\').forEach { c ->
        if (c == '/' || c == '\\') {
            separatorFlag = true
        } else if (separatorFlag) {
            separatorFlag = false
            builder.append('/').append(c)
        } else {
            builder.append(c)
        }
    }
    return builder.toString().intern()
}

inline fun <T> Array<T>?.orNull() = this?.takeIf { it.isNotEmpty() }

inline fun <T : Collection<*>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

inline fun <T : Map<*, *>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

inline fun <T> Iterable<T>.process(processor: (T) -> Boolean): Boolean {
    for (e in this) {
        val result = processor(e)
        if (!result) return false
    }
    return true
}

inline fun <K, V> Map<K, V>.process(processor: (Map.Entry<K, V>) -> Boolean): Boolean {
    for (entry in this) {
        val result = processor(entry)
        if (!result) return false
    }
    return true
}

inline fun <T> Iterable<T>.process(processor: Processor<T>): Boolean {
    return process { processor.process(it) }
}

inline fun <K, V> Map<K, V>.process(processor: Processor<Map.Entry<K, V>>): Boolean {
    return process { processor.process(it) }
}

fun Collection<String>.truncate(limit: Int, ellipsis: String = "..."): List<String> {
    return take(limit).let { if (size > limit) it + ellipsis else it }
}
