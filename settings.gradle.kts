pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.nexa.ai/releases")
    }
}

rootProject.name = "ZS-android"
include(":app")
include(":core:domain")
include(":core:ai")
include(":core:data")
