import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

// Version derived from git (see docs/RELEASING.md):
//   tagged commit vX.Y.Z           -> X.Y.Z
//   on release/vX.Y.Z              -> X.Y.Z-dev.<commits since main>+<hash>
//   on a branch cut from release/* -> X.Y.Z-dev.<since main>.<since release>+<hash>
//   anything else                  -> git describe
// versionCode is the number of commits, so a later build always installs over an earlier one.
fun git(vararg args: String): String = try {
    providers.exec {
        commandLine("git", *args)
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim()
} catch (e: Exception) {
    ""
}

fun versionParts(name: String): List<Int> =
    Regex("""\d+""").findAll(name).map { it.value.toInt() }.toList()

val versionComparator = Comparator<String> { a, b ->
    val pa = versionParts(a)
    val pb = versionParts(b)
    (0 until maxOf(pa.size, pb.size))
        .map { pa.getOrElse(it) { 0 }.compareTo(pb.getOrElse(it) { 0 }) }
        .firstOrNull { it != 0 } ?: 0
}

val gitVersionName: String = run {
    val exactTag = git("describe", "--tags", "--exact-match")
    if (exactTag.isNotEmpty()) return@run exactTag.removePrefix("v")

    val branch = git("rev-parse", "--abbrev-ref", "HEAD")
    val releaseBranch = if (branch.startsWith("release/")) branch else git(
        "for-each-ref", "--format=%(refname:short)", "--merged", "HEAD",
        "refs/heads/release/*", "refs/remotes/origin/release/*"
    ).lines().filter { it.isNotBlank() }.map { it.removePrefix("origin/") }.maxWithOrNull(versionComparator)

    if (releaseBranch == null) {
        return@run git("describe", "--tags", "--always").removePrefix("v").ifEmpty { "0.0.0-dev" }
    }

    val base = if (git("rev-parse", "--verify", "-q", "refs/remotes/origin/main").isNotEmpty()) "refs/remotes/origin/main" else "main"
    val target = if (git("rev-parse", "--verify", "-q", "refs/heads/$releaseBranch").isNotEmpty()) "refs/heads/$releaseBranch" else "refs/remotes/origin/$releaseBranch"
    val sinceMain = git("rev-list", "--count", "$base..$target").ifEmpty { "1" }
    val sinceRelease = git("rev-list", "--count", "$target..HEAD").ifEmpty { "0" }
    val hash = git("rev-parse", "--short", "HEAD").ifEmpty { "dev" }
    val counts = if (sinceRelease == "0") sinceMain else "$sinceMain.$sinceRelease"
    "${releaseBranch.removePrefix("release/").removePrefix("v")}-dev.$counts+$hash"
}

val gitVersionCode: Int = git("rev-list", "--count", "HEAD").toIntOrNull() ?: 1

android {
    namespace = "com.tinaut1986.porkilo"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.tinaut1986.porkilo"
        minSdk = 21
        targetSdk = 36
        versionCode = gitVersionCode
        versionName = gitVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val properties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localPropertiesFile.inputStream().use { properties.load(it) }
                properties.getProperty("release.store.file")?.let { path ->
                    storeFile = file(path)
                }
                storePassword = properties.getProperty("release.store.password")
                keyAlias = properties.getProperty("release.key.alias")
                keyPassword = properties.getProperty("release.key.password")
            }
        }
    }

    buildTypes {
        // Debug builds install next to the release app, so testing never requires uninstalling it
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    applicationVariants.all {
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.ApkVariantOutputImpl
            output.outputFileName = "PorKilo_v${versionName}_${name}.apk"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

// Room schema history, needed for automatic migrations between database versions
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.9.6")
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Code Scanner
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}