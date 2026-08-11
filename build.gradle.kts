plugins {
    id("com.android.kotlin.multiplatform.library").version("9.3.1").apply(false)
    kotlin("multiplatform").version("2.4.10").apply(false)
    kotlin("plugin.serialization").version("2.4.10").apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
