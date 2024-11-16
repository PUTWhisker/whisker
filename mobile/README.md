# Add grpc to already defined android project
Working for project with `Kotlin DSL` used as build configuration language 
1. In different folder clone the [kotlin grpc repository](https://github.com/grpc/grpc-kotlin)
2. Open android studio `Project` view
3. Copy `stub-android` and `protos` directories from `grpc-kotlin/examples` directory to your project's root directory
4. Change `settings.gradle.kts` from
`include(":app")`
to
`include(":app", ":protos", "stub-androidt")`
5. Add code bellow to `ibs.versions.toml`
```kotlin
[libraries]
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version = "1.8.0" }  
grpc-protobuf = { group = "io.grpc", name = "grpc-protobuf", version = "1.62.2" }  
grpc-protobuf-lite = { group = "io.grpc", name = "grpc-protobuf-lite", version = "1.62.2" }  
grpc-netty = { group = "io.grpc", name = "grpc-netty", version = "1.62.2" }  
grpc-okhttp = { group = "io.grpc", name = "grpc-okhttp", version = "1.62.2" }  
grpc-testing = { group = "io.grpc", name = "grpc-testing", version = "1.62.2" }  
grpc-stub = { group = "io.grpc", name = "grpc-stub", version = "1.62.2" }  
grpc-kotlin-stub = { group = "io.grpc", name = "grpc-kotlin-stub", version = "1.4.1" }  
  
protoc = { group = "com.google.protobuf", name = "protoc", version = "3.25.3" }  
protobuf-kotlin = { group = "com.google.protobuf", name = "protobuf-kotlin", version = "3.25.3" }  
protobuf-kotlin-lite = { group = "com.google.protobuf", name = "protobuf-kotlin-lite", version = "3.25.3" }  
protobuf-java-util = { group = "com.google.protobuf", name = "protobuf-java-util", version = "3.25.3" }  
protoc-gen-grpc-java = { group = "io.grpc", name = "protoc-gen-grpc-java", version = "1.62.2" }  
protoc-gen-grpc-kotlin = { group = "io.grpc", name = "protoc-gen-grpc-kotlin", version = "1.4.1" }

[plugins]
android-application = { id = "com.android.application", version = "8.3.0" }  
android-library = { id = "com.android.library", version = "8.3.0" }  
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version = "1.9.0"}  
kotlin-android = { id = "org.jetbrains.kotlin.android", version = "1.9.0" }  
protobuf = { id = "com.google.protobuf", version = "0.9.4" }  
ktlint = { id = "org.jlleitschuh.gradle.ktlint", version = "12.1.0" }  
palantir-graal = { id = "com.palantir.graal", version = "0.12.0" }  
jib = { id = "com.google.cloud.tools.jib", version = "3.4.1" }
```
7. Rename occurrences of `libs.plugins.jetbrainsKotlinAndroid` to `libs.plugins.android.application`
6. Add code bellow to `dependencies` section of app's `build.gradle.kts`
```kotlin
implementation(project(":stub-android"))  
runtimeOnly(libs.grpc.okhttp)
```
7. Allow app to make api calls by adding this line to `AndroidManifest.xml`
`<uses-permission android:name="android.permission.INTERNET"/>`
