plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.jayway.jsonpath:json-path:2.4.0")
    implementation("com.lordcodes.turtle:turtle:0.6.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.dom4j:dom4j:2.1.3")
}
