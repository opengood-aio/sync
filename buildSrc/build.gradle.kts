plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.4")
}
