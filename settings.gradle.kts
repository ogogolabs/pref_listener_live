pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
//        maven("https://gitlab.com/api/v4/projects/67253658/packages/maven")

        maven {
            url = uri("/Users/ulkoone/Documents/work/PrefListenerASPlugin/build/repo")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "My Application"
include(":app")
include(":pref_listener_live")
include(":pref_listener_core")
include(":pref_listener_prod")
include(":pref_listener_debug")

