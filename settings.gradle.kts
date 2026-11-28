pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.myket.ir")
    }
}
rootProject.name = "Shapix"
include(":app")
