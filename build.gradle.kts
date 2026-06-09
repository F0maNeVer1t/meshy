// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.kapt) apply false
}

allprojects {
    afterEvaluate {
        tasks.withType<Test> {
            // Allow tests to be re-run if any optional tests are enabled
            outputs.upToDateWhen { System.getenv("OPTIONAL_TESTS") == null }
            // Use entropy-gathering device specified on command line, if any
            systemProperty("java.security.egd", System.getProperty("java.security.egd") ?: "")
        }
    }
}