plugins {
    application
    java
}

group = "com.editor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.formdev:flatlaf:3.2") // FlatLaf UI theming
    implementation("mysql:mysql-connector-java:8.0.33") // MySQL database driver
    implementation("com.zaxxer:HikariCP:5.0.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("com.editor.DatabaseLauncher")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // You can adjust the version if needed
    }
}
