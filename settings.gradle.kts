plugins {
    id("de.fayard.refreshVersions") version "0.30.1"
}

rootProject.name = "sync"

refreshVersions {
    enableBuildSrcLibs()
}
