plugins {
    kotlin("jvm")
    alias(libs.plugins.dokka)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    `maven-publish`
    signing
}

group = rootProject.group
version = rootProject.version
val isRelease = rootProject.extra["isReleaseVersion"] as Boolean
description = "KSP symbol processor for ktor2-server-bind — generates OpenAPI specs at compile time."

val repoUrl = "https://github.com/withlambda/${rootProject.name}"
val repoBranch = "main"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.ksp.api)
    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

dokka {
    dokkaSourceSets.configureEach {
        moduleName.set(project.name)
        includes.from("Module.md")
        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl.set(uri("$repoUrl/tree/$repoBranch/ktor2-server-bind-processor/src/main/kotlin"))
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
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            if (project.findProperty("skipDokka") != "true") {
                artifact(dokkaJavadocJar)
            }

            pom {
                name.set(project.name)
                description.set("KSP symbol processor for ktor2-server-bind — generates OpenAPI specs at compile time.")
                url.set(repoUrl)

                scm {
                    url.set(repoUrl)
                    connection.set("scm:git:git://github.com:withlambda/${rootProject.name}.git")
                    developerConnection.set("scm:git:ssh://git@github.com:withlambda/${rootProject.name}.git")
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
    }

    repositories {
        mavenLocal()
        maven {
            name = "OSSRH"
            val releasesUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (isRelease) releasesUrl else snapshotsUrl
            credentials {
                username = findProperty("OSSRH_USERNAME") as String? ?: System.getenv("OSSRH_USERNAME")
                password = findProperty("OSSRH_PASSWORD") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    setRequired({
        isRelease && gradle.taskGraph.hasTask("publish")
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

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    source.setFrom("src/main/kotlin", "src/test/kotlin")
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
