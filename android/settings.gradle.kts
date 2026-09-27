pluginManagement {
    repositories {
        maven { url = java.net.URI("https://maven.aliyun.com/repository/public") }
        maven { url = java.net.URI("https://maven.aliyun.com/repository/google") }
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.google.devtools.ksp") {
                useModule("com.google.devtools.ksp:symbol-processing-gradle-plugin:${requested.version}")
            }
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = java.net.URI("https://maven.aliyun.com/repository/public") }
        maven { url = java.net.URI("https://maven.aliyun.com/repository/google") }
        google()
        mavenCentral()
    }
}

rootProject.name = "Dayflow"
include(":app")
