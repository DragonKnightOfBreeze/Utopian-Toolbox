import org.jetbrains.changelog.*
import org.jetbrains.intellij.platform.gradle.*
import org.jetbrains.kotlin.gradle.tasks.*
import org.jetbrains.kotlin.utils.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.2.0"
    id("org.jetbrains.changelog") version "2.0.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

fun String.toChangeLogText(): String {
    val regex1 = """[-*] \[ ].*""".toRegex()
    val regex2 = """[-*] \[X].*""".toRegex(RegexOption.IGNORE_CASE)
    return lines()
        .run {
            val start = indexOfFirst { it.startsWith("## $version") }
            val end = indexOfFirst(start + 1) { it.startsWith("## ") }.let { if (it != -1) it else size }
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

intellijPlatform {
    pluginConfiguration {
        id.set(providers.gradleProperty("pluginId"))
        name.set(providers.gradleProperty("pluginName"))
        version.set(providers.gradleProperty("pluginVersion"))
        description.set(projectDir.resolve("DESCRIPTION.md").readText())
        changeNotes.set(projectDir.resolve("CHANGELOG.md").readText().toChangeLogText())
        ideaVersion {
            sinceBuild.set(providers.gradleProperty("sinceBuild"))
            untilBuild.set(provider { null })
        }
    }
    publishing {
        token.set(providers.environmentVariable("IDEA_TOKEN"))
    }
}


repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        val type = providers.gradleProperty("platformType")
        val version = providers.gradleProperty("platformVersion")
        create(type, version)

        testFramework(TestFrameworkType.Platform)

        //optional
        bundledPlugins("com.intellij.java")
        bundledPlugins("org.jetbrains.kotlin")
        bundledPlugins("org.intellij.plugins.markdown")
    }

    //junit & opentest4j
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
}

sourceSets {
    main {
        java.srcDirs("src/main/java", "src/main/kotlin", "src/main/gen")
        resources.srcDirs("src/main/resources")
    }
    test {
        java.srcDirs("src/test/java", "src/test/kotlin")
        resources.srcDirs("src/test/resources")
    }
}

kotlin {
    jvmToolchain(21)
}

tasks {
    withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs = listOf(
                "-Xjvm-default=all",
                "-Xinline-classes",
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlin.ExperimentalStdlibApi",
            )
        }
    }
    withType<Jar> {
        from("README.md", "README_en.md", "LICENSE")
    }
    runIde {
        jvmArgumentProviders += CommandLineArgumentProvider {
            listOf("-Didea.is.internal=true")
        }
    }
}
