import dev.schlaubi.mikbot.gradle.GenerateDefaultTranslationBundleTask
import java.util.Locale

plugins {
    kotlin("jvm") version "1.6.20"
    kotlin("plugin.serialization") version "1.6.20"

    id("com.google.devtools.ksp") version "1.6.20-1.0.5"
    id("dev.schlaubi.mikbot.gradle-plugin") version "2.0.1"

    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

ktlint {
    disabledRules.set(listOf("no-wildcard-imports"))
}

group = "net.stckoverflw"
version = "1.0.6"

repositories {
    mavenCentral()
    maven("https://schlaubi.jfrog.io/artifactory/mikbot/")
    maven("https://maven.kotlindiscord.com/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("dev.schlaubi:mikbot-api:3.0.0-SNAPSHOT")
    ksp("dev.schlaubi:mikbot-plugin-processor:2.0.0")
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
        defaultLocale.set(Locale("en", "GB"))
    }

    assemblePlugin {
        dependsOn(generateDefaultResourceBundle)
    }

    installBot {
        botVersion.set("3.0.0-SNAPSHOT")
    }

    pluginPublishing {
        repositoryUrl.set("https://private-channel-repo.stckoverflw.net")
        targetDirectory.set(rootProject.file("ci-repo").toPath())
        projectUrl.set("https://github.com/StckOverflw/privatechannel")
    }
}
