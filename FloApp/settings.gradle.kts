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
        // SQLCipher
        maven { url = uri("https://www.zetetic.net/maven/") }
        // iText community
        maven { url = uri("https://repo.itextsupport.com/releases") }
    }
}

rootProject.name = "FloApp"
include(":app")
