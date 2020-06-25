dependencies {
    api(project(":AnnotatedConfig-Core"))
    api("com.amihaiemil.web:eo-yaml:5.1.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "de.articdive"
            artifactId = "annotated-config-yaml"
            version = "${rootProject.version}"

            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "Articdive-Nexus-OSS-3-Repository"
            url = uri("https://repo.articdive.de/repository/maven-releases")
            credentials {
                username="${rootProject.properties["repository_username"]}"
                password="${rootProject.properties["repository_password"]}"
            }
        }
    }
}