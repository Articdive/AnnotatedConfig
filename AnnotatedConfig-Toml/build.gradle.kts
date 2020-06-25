dependencies {
    api(project(":AnnotatedConfig-Core"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "de.articdive"
            artifactId = "annotated-config-toml"
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