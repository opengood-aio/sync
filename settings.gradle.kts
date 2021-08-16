plugins {
    id("de.fayard.refreshVersions") version "0.11.0"
}

refreshVersions {
    enableBuildSrcLibs()
}

rootProject.name = "sync"
