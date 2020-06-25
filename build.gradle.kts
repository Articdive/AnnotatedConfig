group = "de.articdive"
version = "1.0.0"


repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = "${rootProject.group}"
    version = "${rootProject.version}"

    repositories {
        jcenter()
        mavenCentral()
        // Sonatype Repository
        maven {
            url = uri("https://oss.sonatype.org/content/groups/public/")
        }
    }

    dependencies {
        // Jetbrains annotations
        "compileOnly"("org.jetbrains:annotations:19.0.0")
        // JUnit testing framework
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.6.2")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.6.2")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}