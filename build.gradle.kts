import dev.schlaubi.mikbot.gradle.GenerateDefaultTranslationBundleTask
import java.util.Locale

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("com.google.devtools.ksp") version "1.6.10-1.0.2"
    id("dev.schlaubi.mikbot.gradle-plugin") version "1.3.1"

    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

ktlint {
    disabledRules.set(listOf("no-wildcard-imports"))
}

group = "net.stckoverflw"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://schlaubi.jfrog.io/artifactory/mikbot/")
    maven("https://schlaubi.jfrog.io/artifactory/envconf/")
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
}

dependencies {
    compileOnly("dev.schlaubi:mikbot-api:2.0.1-SNAPSHOT")
    ksp("dev.schlaubi:mikbot-plugin-processor:1.0.0")
}

mikbotPlugin {
    provider.set("StckOverflw")
    license.set("AGPL-3.0 License")
    description.set("Let Members of your Discord Server create their own Channels")
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    task<Copy>("buildAndInstall") {
        dependsOn(assemblePlugin)
        from(assemblePlugin)
        include("*.zip")
        into("plugins")
    }

    val generateDefaultResourceBundle = task<GenerateDefaultTranslationBundleTask>("generateDefaultResourceBundle") {
        defaultLocale.set(Locale("en:GB"))
    }

    assemblePlugin {
        dependsOn(generateDefaultResourceBundle)
    }

    installBot {
        botVersion.set("2.0.1-SNAPSHOT")
    }

    buildRepository {
        repositoryUrl.set("https://private-channel-repo.stckoverflw.net")
        targetDirectory.set(rootProject.file("ci-repo").toPath())
        projectUrl.set("https://github.com/StckOverflw/privatechannel")
    }
}
