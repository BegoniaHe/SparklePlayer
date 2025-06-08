plugins {
    java
    application
}

group = "com.sparkle"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.compileJava {
    options.compilerArgs.add("-Xlint:deprecation")
    options.encoding = "UTF-8"
}

application {
    mainClass.set("com.sparkle.enterProgram.EnterProgram")
}

repositories {
    mavenCentral()
    flatDir {
        dirs("sparkleplayer/libs", "sparkleplayer/libs/musique")
    }
}

dependencies {
    // Apache Commons libraries
    implementation("commons-beanutils:commons-beanutils:1.7.0")
    implementation("commons-codec:commons-codec:1.10")
    implementation("commons-collections:commons-collections:3.1")
    implementation("commons-lang:commons-lang:2.5")
    implementation("commons-logging:commons-logging:1.2")
    
    // JSON processing
    implementation("net.sf.ezmorph:ezmorph:1.0.3")
    implementation("net.sf.json-lib:json-lib:2.1:jdk15")
    
    // Logging
    implementation("log4j:log4j:1.2.8")
    
    // Derby database
    implementation("org.apache.derby:derby:10.4.2.0")
    
    // Local JAR dependencies from sparkleplayer/libs
    implementation(group = "", name = "JPlayer-lib", version = "", ext = "jar")
    implementation(group = "", name = "TweenEngine-lib", version = "", ext = "jar")
    
    // Audio codec libraries from sparkleplayer/libs/musique
    implementation(group = "", name = "alacdecoder-1.0", version = "", ext = "jar")
    implementation(group = "", name = "cuelib-1.2.1", version = "", ext = "jar")
    implementation(group = "", name = "discogs-0.1", version = "", ext = "jar")
    implementation(group = "", name = "jaad-0.7.3", version = "", ext = "jar")
    implementation(group = "", name = "jaudiotagger-2.0.4", version = "", ext = "jar")
    implementation(group = "", name = "javaFlacEncoder-0.1", version = "", ext = "jar")
    implementation(group = "", name = "javalayer-1.0.1", version = "", ext = "jar")
    implementation(group = "", name = "jflac-1.3", version = "", ext = "jar")
    implementation(group = "", name = "jmac-1.74", version = "", ext = "jar")
    implementation(group = "", name = "jorbis-0.0.17", version = "", ext = "jar")
    implementation(group = "", name = "tta-1.0", version = "", ext = "jar")
    implementation(group = "", name = "vorbis-java-1.0.0-beta", version = "", ext = "jar")
    implementation(group = "", name = "wavpack-1.1", version = "", ext = "jar")
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src"))
        }
        resources {
            setSrcDirs(listOf("src"))
            exclude("**/*.java")
        }
    }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "com.sparkle.enterProgram.EnterProgram"
        )
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val copyResources = tasks.register<Copy>("copyResources") {
    from("sparkle/resources")
    into("$buildDir/resources/main/sparkle")
}

tasks.processResources {
    dependsOn(copyResources)
}

val fatJar = tasks.register<Jar>("fatJar") {
    archiveClassifier.set("fat")
    manifest {
        attributes(
            "Main-Class" to "com.sparkle.enterProgram.EnterProgram"
        )
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get())
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.run.configure {
    workingDir = projectDir
    systemProperty("user.dir", projectDir.absolutePath)
}

val sourcesJar = tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

artifacts {
    add("archives", sourcesJar)
    add("archives", fatJar)
}
