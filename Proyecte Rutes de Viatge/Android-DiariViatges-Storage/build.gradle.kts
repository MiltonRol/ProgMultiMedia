// Fitxer build.gradle.kts arrel: declara els plugins però no els aplica.
// Cada mòdul (app) els aplica amb alias(...).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
