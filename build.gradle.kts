import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)

    `maven-publish`
    signing
}

group = "com.withlambda"
val isRelease = false // set to true for a release build
version = buildString {
    append("1.0.0")
    if (isRelease.not()) append("-SNAPSHOT")
}
// Release → "1.0.0"
// Snapshot → "1.0.0-SNAPSHOT"
description =
    """
    Ktor server extension library providing web bind templates
    """.trimIndent()
extra["isReleaseVersion"] = isRelease

val repoUrl = "https://github.com/withlambda/${project.name}"
val repoBranch = "main"

repositories {
    mavenLocal()
    mavenCentral()
}

val ktorIntegrationTestVersion: String = findProperty("ktorIntegrationTestVersion") as String?
    ?: libs.versions.ktor.get() // defaults to "2.0.0" (from version catalog)

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            freeCompilerArgs.add("-Xjdk-release=1.8")
        }
        compilations {
            val integrationTest = compilations.create("integrationTest") {
                defaultSourceSet {
                    // By default, IntelliJ shows [jvmIntegrationTest] in brackets for src/commonTest/kotlin.
                    // We only add this srcDir when not running in the IDE to avoid this.
                    if (System.getProperty("idea.active") == null && System.getProperty("idea.sync.active") == null) {
                        kotlin.srcDir("src/commonTest/kotlin")
                    }
                    dependencies {
                        implementation("com.withlambda:ktor2-server-bind:$version")
                        implementation(kotlin("test-junit"))
                        implementation(libs.kotlinx.serialization)
                        implementation(libs.ktor.server.test.host)
                        implementation(libs.ktor.server.auth)
                        implementation(libs.ktor.server.content.negotiation)
                        implementation(libs.ktor.server.status.pages)
                        runtimeOnly(libs.slf4j.nop)
                    }
                }
            }

            tasks.register<Test>("integrationTest") {
                description = "Runs integration tests against the published JAR from mavenLocal."
                group = "verification"

                testClassesDirs = integrationTest.output.classesDirs
                classpath = integrationTest.runtimeDependencyFiles + integrationTest.output.allOutputs

                useJUnit()

                dependsOn(tasks.named("publishToMavenLocal"))
            }

            tasks.named("compileIntegrationTestKotlinJvm") {
                dependsOn("jvmJar")
            }
        }
    }
    // Apply a specific Java toolchain to ease working on different environments.
    jvmToolchain(21)
    sourceSets {
        named("commonMain") {
            dependencies {
                // -- library dependencies
                api(libs.ktor.server.core)
                implementation(libs.ktor.server.auth)
                implementation(libs.ktor.server.status.pages)
                implementation(libs.konform)
            }
        }

        named("commonTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.serialization)
                implementation(libs.ktor.server.test.host)
                implementation(libs.ktor.server.content.negotiation)
                implementation(libs.ktor.client.content.negotiation)
            }
        }

        named("jvmTest") {
            dependencies {
                runtimeOnly(libs.slf4j.nop)
                implementation(libs.ktor.client.content.negotiation)
            }
        }
    }
}

configurations
    .filter { it.name.startsWith("integrationTest") }
    .forEach { config ->
        config.resolutionStrategy.eachDependency {
            if (requested.group == "io.ktor") {
                useVersion(ktorIntegrationTestVersion)
            }
        }
    }

val ktorIntegrationTestVersions = listOf(
    "2.0.0",
    "2.0.3",
    "2.1.0",
    "2.1.3",
    "2.2.0",
    "2.2.4",
    "2.3.0",
    "2.3.13",
)

val perVersionTasks = ktorIntegrationTestVersions.map { version ->
    val taskName = "integrationTest_${version.replace('.', '_')}"
    if (project.hasProperty("ktorIntegrationTestVersion")) {
        tasks.register(taskName) {
            description = "Worker task for Ktor $version integration tests."
            group = "verification"
            dependsOn("integrationTest")
        }
    } else {
        tasks.register<Exec>(taskName) {
            description = "Runs integration tests against Ktor $version."
            group = "verification"
            commandLine(
                "$rootDir/gradlew",
                taskName,
                "-PktorIntegrationTestVersion=$version",
                "-PskipDokka=true",
                "--project-dir",
                "$rootDir",
                "--no-daemon",
            )
        }
    }
}

tasks.register("integrationTestAllVersions") {
    description = "Runs integration tests against all supported Ktor 2 versions."
    group = "verification"
    dependsOn(perVersionTasks)
}

tasks.named("check") {
    dependsOn(tasks.named("integrationTestAllVersions"))
}

dokka {
    dokkaSourceSets.configureEach {
        // used as the project name in the header
        moduleName.set(project.name)

        // contains descriptions for the module and the packages
        includes.from("Module.md")

        // adds source links that lead to this repository, allowing readers
        // to easily find source code for inspected declarations
        sourceLink {
            localDirectory.set(file("src/commonMain/kotlin"))
            remoteUrl.set(uri("$repoUrl/tree/$repoBranch/src/commonMain/kotlin"))
            remoteLineSuffix.set("#L")
        }
    }
}

val dokkaJavadocJar = tasks.register<Jar>("dokkaJavadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaGeneratePublicationHtml"))
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.description,
                "Implementation-Version" to project.version,
            )
        )
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        if (project.findProperty("skipDokka") != "true") {
            artifact(dokkaJavadocJar)
        }

        pom {
            name.set(project.name)
            description.set(project.description)
            url.set(repoUrl)

            scm {
                url.set(repoUrl)
                connection.set("scm:git:git://github.com:withlambda/${project.name}.git")
                developerConnection.set("scm:git:ssh://git@github.com:withlambda/${project.name}.git")
            }

            issueManagement {
                system.set("GitHub Issues")
                url.set("$repoUrl/issues")
            }

            licenses {
                license {
                    name.set("Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }

            developers {
                developer {
                    id.set("withLambda")
                    name.set("withLambda Team")
                    email.set("github.withlambda@posteo.com")
                    url.set("https://github.com/withlambda")
                    timezone.set("Europe/Berlin")
                }
            }

            contributors {
                contributor {
                    name.set("Karsten Leonhardt")
                    organization.set("withLambda")
                    url.set("https://github.com/karlo0")
                    timezone.set("Europe/Berlin")
                }
            }
        }
    }

    repositories {
        mavenLocal()
        maven {
            name = "OSSRH"
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (project.extra["isReleaseVersion"] as Boolean) releasesUrl else snapshotsUrl
            credentials {
                username = findProperty("OSSRH_USERNAME") as String? ?: System.getenv("OSSRH_USERNAME")
                password = findProperty("OSSRH_PASSWORD") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    setRequired({
        (project.extra["isReleaseVersion"] as Boolean) && gradle.taskGraph.hasTask("publish")
    })

    val mapOfSigningProperties =
        with(
            project.rootDir
                .toPath()
                .resolve("env")
                .resolve("gradle-signing.private.env")
                .toFile(),
        ) {
            exists() || return@with emptyMap()
            readLines().associate {
                val (envVarName, envVarValue) = it.split("=")
                envVarName to envVarValue
            }
        }

    val getSigningProperty: (String) -> String? = { propertyName ->
        findProperty(propertyName) as String?
            ?: mapOfSigningProperties["ORG_GRADLE_PROJECT_$propertyName"]
    }

    val signingKeyId = getSigningProperty("SIGNING_KEY_ID")
    val signingKey = getSigningProperty("SIGNING_KEY")
    val signingPassword = getSigningProperty("SIGNING_PASSWORD")

    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications)
}

tasks.withType<Sign>().configureEach {
    onlyIf {
        findProperty("enableGradleSigningPlugin") as? Boolean? ?: false
    }
}

kover {
    currentProject {
        instrumentation {
            // Exclude integration test tasks from Kover instrumentation.
            // They run against the published JAR from mavenLocal (an external artifact),
            // and Kover's ClasspathEntrySnapshotTransform fails trying to process it.
            disabledForTestTasks.addAll(
                "integrationTest",
                *ktorIntegrationTestVersions.map {
                    "integrationTest_${it.replace('.', '_')}"
                }.toTypedArray()
            )
        }
    }
    reports {
        filters {
            excludes {
                // Exclude test fixtures and generated sources from coverage reporting
                packages("com.withlambda.ktor2.server.bind.fixtures")
            }
        }
    }
}

ktlint {
    filter {
        exclude("**/generated/**")
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
    source.setFrom("src/commonMain/kotlin", "src/commonTest/kotlin")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "1.8"
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(true)
        md.required.set(false)
    }
}

if (project.findProperty("skipDokka") == "true") {
    tasks.matching { it.name.contains("dokka", ignoreCase = true) }.configureEach {
        enabled = false
    }
}

if (project.findProperty("skipDetekt") == "true") {
    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        enabled = false
    }
}
