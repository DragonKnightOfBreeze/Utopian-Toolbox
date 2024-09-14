@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.ut.toolbox

import com.google.common.cache.CacheBuilder
import icu.windea.ut.toolbox.util.buildCache
import java.io.File
import java.nio.file.Path

inline fun <T> Array<T>?.orNull() = this?.takeIf { it.isNotEmpty() }

inline fun <T : Collection<*>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

inline fun <T : Map<*, *>> T?.orNull() = this?.takeIf { it.isNotEmpty() }

inline fun <reified T> Any?.cast(): T = this as T

inline fun <reified T> Any?.castOrNull(): T? = this as? T

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
    if(pattern.isEmpty() && this.isNotEmpty()) return false
    if(pattern == "*") return true
    val cache = if(ignoreCase) patternToRegexCache2 else patternToRegexCache1
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
        while(i < s.length) {
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
    if(pattern.isEmpty() && this.isNotEmpty()) return false
    if(pattern == "**") return true
    val cache = if(ignoreCase) antPatternToRegexCache2 else antPatternToRegexCache1
    val path0 = this.let { if(trimSeparator) it.trim('/') else it }
    val pattern0 = pattern.let { if(trimSeparator) it.trim('/') else it }
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
        while(i < s.length) {
            val c = s[i]
            when {
                c == '*' -> {
                    val nc = s.getOrNull(i + 1)
                    if(nc == '*') {
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
        if(c == '/' || c == '\\') {
            separatorFlag = true
        } else if(separatorFlag) {
            separatorFlag = false
            builder.append('/').append(c)
        } else {
            builder.append(c)
        }
    }
    return builder.toString().intern()
}

