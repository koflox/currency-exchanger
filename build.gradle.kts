@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt)
}

dependencies {
    detektPlugins(libs.detekt)
}

tasks.register("detektCurrencyExchanger", io.gitlab.arturbosch.detekt.Detekt::class) {
    description = "Runs detekt analysis over whole code base"
    parallel = true
    autoCorrect = true
    config.setFrom(files("$rootDir/.lint/detekt/detekt-config.yml"))
    setSource(files(projectDir))
    include("**/*.kt", "**/*.kts")
    exclude("**/build/**", "**/resources/**")
    reports {
        xml.required.set(true)
        html.required.set(false)
        txt.required.set(false)
    }
}
