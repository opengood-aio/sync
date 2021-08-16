import kotlin.String

/**
 * Generated by
 *    $ ./gradlew buildSrcLibs
 * Re-run when you add a new dependency to the build
 *
 * Find which updates are available by running
 *     $ ./gradlew refreshVersions
 * And edit the file `versions.properties`
 *
 * See https://github.com/jmfayard/refreshVersions
 */
object Libs {
    const val jackson_module_kotlin: String = "com.fasterxml.jackson.module:jackson-module-kotlin:_"

    const val de_fayard_buildsrclibs_gradle_plugin: String =
            "de.fayard.buildSrcLibs:de.fayard.buildSrcLibs.gradle.plugin:_"

    const val org_jetbrains_kotlin_jvm_gradle_plugin: String =
            "org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:_"

    const val org_jetbrains_kotlin_plugin_allopen_gradle_plugin: String =
            "org.jetbrains.kotlin.plugin.allopen:org.jetbrains.kotlin.plugin.allopen.gradle.plugin:_"

    const val kotlin_allopen: String = "org.jetbrains.kotlin:kotlin-allopen:_"

    const val kotlin_scripting_compiler_embeddable: String =
            "org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:_"

    const val kotlin_stdlib: String = "org.jetbrains.kotlin:kotlin-stdlib"
}
