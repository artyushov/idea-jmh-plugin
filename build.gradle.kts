plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.15.0"
}

group = "com.github.artyushov.idea-jmh-plugin"
version = "1.4.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.2")
    type.set("IC")

    plugins.set(listOf("java"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("")
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
