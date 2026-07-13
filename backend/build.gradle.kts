plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "2.4.0"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
}

group = "org.example.notifier"
version = "1.0.11"
java.sourceCompatibility = JavaVersion.VERSION_17

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

detekt {
    ignoreFailures = true
    config.setFrom(file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

kover {
    if (System.getenv("CI") != null) {
        disable()
    } else {
        reports {
            filters {
                excludes {
                    classes(
                        "org.example.notifier.infrastructure.dto.request.*",
                        "org.example.notifier.infrastructure.dto.response.*",
                        "org.example.notifier.infrastructure.external.*",
                        "org.example.notifier.infrastructure.external.coderbyte.*",
                    )
                }
            }
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.7.3")

    // Mail
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.sun.mail:jakarta.mail:2.0.1")

    // DynamoDB
    implementation(platform("software.amazon.awssdk:bom:2.20.26"))
    implementation("software.amazon.awssdk:dynamodb")
    implementation("software.amazon.awssdk:dynamodb-enhanced")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // Google OAuth
    implementation("com.google.api-client:google-api-client:2.2.0")

    // Auth0 JWT
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("com.auth0:jwks-rsa:0.22.1")

    //aws
    implementation(platform("aws.sdk.kotlin:bom:1.2.7"))
    implementation("aws.sdk.kotlin:s3")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")
    implementation("com.squareup.okhttp3:okhttp-coroutines:5.0.0-alpha.14")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // OpenAPI / Swagger UI
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.springframework.security:spring-security-test")
}

tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }
    maxHeapSize = "512m"
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        // xml.required.set(true)   // Enable if needed
        // txt.required.set(true)   // Enable if needed
        // sarif.required.set(true) // Enable if needed
        // md.required.set(true)    // Enable if needed
    }
}

tasks.check {
    dependsOn(tasks.detekt)
}

// Load .env file for local development only — not a runtime dependency, not in the JAR
tasks.bootRun {
    val envFile = file(".env")
    if (envFile.exists()) {
        envFile.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") && it.contains("=") }
            .forEach { line ->
                val (key, value) = line.split("=", limit = 2)
                environment(key.trim(), value.trim())
            }
    }
}

springBoot {
    buildInfo()
}
configurations.all {
    resolutionStrategy {
        force(
            "com.squareup.okhttp3:okhttp:5.0.0-alpha.14",
            "com.squareup.okhttp3:okhttp-coroutines:5.0.0-alpha.14",
            "com.squareup.okio:okio:3.9.0",
            "aws.smithy.kotlin:http-client-engine-okhttp:1.2.2",
            "aws.smithy.kotlin:runtime-core:1.2.2"
        )

        eachDependency {
            when ("${requested.group}:${requested.name}") {
                "com.squareup.okhttp3:okhttp" -> useVersion("5.0.0-alpha.14")
                "com.squareup.okhttp3:okhttp-coroutines" -> useVersion("5.0.0-alpha.14")
                "com.squareup.okio:okio" -> useVersion("3.9.0")
            }
        }
    }
}

gitProperties {
    failOnNoGitDirectory = false
}