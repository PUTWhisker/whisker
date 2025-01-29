// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.protobuf) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ktlint) apply false
}
subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        filter {
            exclude {
                it.file.path.startsWith(project.layout.buildDirectory.get().dir("generated").toString())
            }
        }
    }
}

tasks.create("assemble").dependsOn(":server:installDist")

//rootProject.extra["serverUrl"] = "http://10.0.2.2:50051/"
rootProject.extra["serverUrl"] = "http://whisper.cs.put.poznan.pl:80/"
