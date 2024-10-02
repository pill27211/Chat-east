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
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven{ setUrl("https://jitpack.io") }
        maven { setUrl ("https://raw.githubusercontent.com/alexgreench/google-webrtc/master") }
    }
}

rootProject.name = "Chat_east"
include(":app")
include("my_flutter")

project(":my_flutter").projectDir = file("my_flutter")
 