import org.jreleaser.model.Active

plugins {
    id("java-library")
    id("maven-publish")
    id("org.jreleaser") version "1.18.0"
}

group = "io.github.kaktushose"
version = "0.2.3"


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Proteus")
                description.set("A modern, bidirectional type adapting library for Java.")
                url.set("https://github.com/Kaktushose/proteus")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                developers {
                    developer {
                        name.set("Kaktushose")
                    }
                    developer {
                        name.set("Goldmensch")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/kaktushose/proteus.git")
                    developerConnection.set("scm:git:ssh://github.com/kaktushose/proteus.git")
                    url.set("https://github.com/Kaktushose/proteus")
                }
            }
        }
    }

    repositories {
        maven {
            setUrl(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    project {
        copyright = "Kaktushose & Goldmensch"
    }


    signing {
        active = Active.ALWAYS
        armored = true
    }

    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active = Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                    setStage("UPLOAD")
                }
            }
        }
    }
}

tasks.jreleaserDeploy {
    dependsOn(tasks.publish)
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }

    withSourcesJar()
    withJavadocJar()
}

tasks.javadoc {
    val options = options as StandardJavadocDocletOptions
    options.overview = "src/main/javadoc/overview.md"
    options.encoding = "UTF-8"
    options.addBooleanOption("Xdoclint:none,-missing", true)
    options.tags("apiNote:a:API Note:", "implSpec:a:Implementation Requirements:", "implNote:a:Implementation Note:")
}
