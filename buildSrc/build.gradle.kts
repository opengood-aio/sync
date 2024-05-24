import java.util.Properties

plugins {
    `kotlin-dsl`
}

val properties = Properties().apply {
    load(rootProject.file("../versions.properties").reader())
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:" + versionFor("com.fasterxml.jackson.dataformat..jackson-dataformat-yaml"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:" + versionFor("com.fasterxml.jackson.module..jackson-module-kotlin"))
    implementation("com.github.kittinunf.fuel:fuel:" + versionFor("com.github.kittinunf.fuel..fuel"))
    implementation("com.jayway.jsonpath:json-path:" + versionFor("com.jayway.jsonpath..json-path"))
    implementation("com.lordcodes.turtle:turtle:" + versionFor("com.lordcodes.turtle..turtle"))
    implementation("org.apache.commons:commons-lang3:" + versionFor("org.apache.commons..commons-lang3"))
    implementation("org.dom4j:dom4j:" + versionFor("org.dom4j..dom4j"))
}

fun versionFor(name: String): String {
    val key = "version.$name"
    return if (properties.containsKey(key)) {
        properties[key].toString()
    } else {
        ""
    }
}
