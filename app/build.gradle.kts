import com.android.build.api.artifact.SingleArtifact
import javax.xml.parsers.DocumentBuilderFactory

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val appVersionMajor = providers.gradleProperty("appVersionMajor").get().toInt()
val appVersionPatch = providers.gradleProperty("appVersionPatch").get().toInt()
check(appVersionMajor >= 1) { "appVersionMajor must be at least 1" }
check(appVersionPatch in 0..999) { "appVersionPatch must be in 0..999" }

// Release signing comes from the environment (set by the release workflow). Without it, release APKs are unsigned.
val releaseKeystore = providers.environmentVariable("RANDOMATIZER_KEYSTORE_FILE")

android {
    namespace = "io.github.jamesmyatt.randomatizer"
    compileSdk {
        version = release(37) { minorApiLevel = 2 }
    }

    defaultConfig {
        applicationId = "io.github.jamesmyatt.randomatizer"
        minSdk = 31
        targetSdk = 37
        versionCode = appVersionMajor * 1000 + appVersionPatch
        versionName = "$appVersionMajor.$appVersionPatch"
    }

    signingConfigs {
        if (releaseKeystore.isPresent) {
            create("release") {
                storeFile = file(releaseKeystore.get())
                storePassword = providers.environmentVariable("RANDOMATIZER_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("RANDOMATIZER_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("RANDOMATIZER_KEY_PASSWORD").get()
            }
        }
    }

    buildTypes {
        // Installs alongside the release build, with its own name.
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    // F-Droid rejects the Google-encrypted dependency metadata block.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test.junit)
}

/** Fails the build if the merged manifest requests any forbidden permission. */
abstract class VerifyForbiddenPermissions : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mergedManifest: RegularFileProperty

    @get:Input
    abstract val forbidden: SetProperty<String>

    @get:OutputFile
    abstract val report: RegularFileProperty

    @TaskAction
    fun verify() {
        val document = DocumentBuilderFactory.newInstance()
            .apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(mergedManifest.get().asFile)
        val androidNs = "http://schemas.android.com/apk/res/android"
        val requested = listOf("uses-permission", "uses-permission-sdk-23").flatMap { tag ->
            val nodes = document.getElementsByTagName(tag)
            (0 until nodes.length).map { (nodes.item(it) as org.w3c.dom.Element).getAttributeNS(androidNs, "name") }
        }
        val violations = requested.filter { it in forbidden.get() }
        if (violations.isNotEmpty()) {
            throw GradleException("Merged manifest requests forbidden permission(s): ${violations.joinToString()}")
        }
        report.get().asFile.writeText("Requested permissions: ${requested.joinToString().ifEmpty { "none" }}\n")
    }
}

androidComponents {
    onVariants { variant ->
        val suffix = variant.name.replaceFirstChar { it.uppercase() }
        val verify = tasks.register<VerifyForbiddenPermissions>("verify${suffix}Permissions") {
            mergedManifest.set(variant.artifacts.get(SingleArtifact.MERGED_MANIFEST))
            forbidden.add("android.permission.INTERNET")
            report.set(layout.buildDirectory.file("reports/permissions/${variant.name}.txt"))
        }
        tasks.matching { it.name == "assemble$suffix" }.configureEach { dependsOn(verify) }
        tasks.named("check") { dependsOn(verify) }
    }
}
