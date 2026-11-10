pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MathBlade"

// Only :core for now. The :app agent adds itself here (include(":app"))
// together with its own app/build.gradle.kts.
include(":core")
include(":app")
