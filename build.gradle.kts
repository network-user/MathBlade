// Root build file for MathBlade.
// Plugin versions live in gradle/libs.versions.toml (version catalog).
// This module only applies the JVM plugin to :core; Android plugins
// (AGP, Compose BOM, navigation, DataStore) are catalogued here for :app.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
}
