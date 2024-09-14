import org.jetbrains.changelog.*
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.utils.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
    id("org.jetbrains.changelog") version "2.0.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

intellij {
    pluginName.set(providers.gradleProperty("pluginName"))
    type.set(providers.gradleProperty("intellijType"))
    version.set(providers.gradleProperty("intellijVersion"))

    //optional
    plugins.add("yaml")
    plugins.add("markdown")
}

repositories {
    mavenCentral()
    maven("https://www.jetbrains.com/intellij-repository/releases")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf(
                "-Xjvm-default=all",
                "-Xinline-classes",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlin.ExperimentalStdlibApi",
            )
        }
    }	
    withType<Test> {
        useJUnitPlatform()
        isScanForTestClasses = false
        include("**/*Test.class")
    }
    withType<Jar> {
        from("README.md", "README_en.md", "LICENSE")
    }
    patchPluginXml {
        fun String.toChangeLogText(): String {
            val regex1 = """[-*] \[ ].*""".toRegex()
            val regex2 = """[-*] \[X].*""".toRegex(RegexOption.IGNORE_CASE)
            return lines()
                .run {
                    val start = indexOfFirst { it.startsWith("## ${version.get()}") }
                    val end = indexOfFirst(start + 1) { it.startsWith("## ") }.let { if(it != -1) it else size }
                    subList(start + 1, end)
                }
                .mapNotNull {
                    when {
                        it.contains("(HIDDEN)") -> null //hidden
                        it.matches(regex1) -> null //undo
                        it.matches(regex2) -> "*" + it.substring(5) //done
                        else -> it
                    }
                }
                .joinToString("\n")
                .let { markdownToHTML(it) }
        }

        sinceBuild.set(providers.gradleProperty("sinceBuild"))
        untilBuild.set(providers.gradleProperty("untilBuild"))
        pluginDescription.set(projectDir.resolve("DESCRIPTION.md").readText())
        changeNotes.set(projectDir.resolve("CHANGELOG.md").readText().toChangeLogText())
    }
    publishPlugin {
        token.set(providers.environmentVariable("IDEA_TOKEN"))
    }
}
