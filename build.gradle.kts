plugins {
    id("java-library")
    alias(libs.plugins.run.paper)
    alias(libs.plugins.resource.factory.paper)
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.helpch.at/releases/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly("me.clip:placeholderapi:2.11.6")
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

paperPluginYaml {
    main = "net.cyberflame.consistentxp.ConsistentXP"
    bootstrapper = "net.cyberflame.consistentxp.ConsistentXPBootstrap"
    loader = "net.cyberflame.consistentxp.ConsistentXPLoader"
    apiVersion = "26.3"

    authors.addAll("CyberFlame")
    website = "https://cyberfla.me/"
    prefix = "[Consistent XP] "
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(27)
}

tasks {
    test {
        useJUnitPlatform()
    }
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        jvmArgs("-Xms2G", "-Xmx2G")
    }
}

