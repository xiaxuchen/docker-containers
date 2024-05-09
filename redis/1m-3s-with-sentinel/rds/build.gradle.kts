import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.palantir.docker") version "0.36.0"
}

group = "org.originit"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks {
    // 获取 dockerPrepare 任务的引用
    val dockerPrepare by getting {
        // 声明 bootJar 作为 dockerPrepare 的输入
        dependsOn("bootJar")
    }

    docker {
        setDockerfile(file("Dockerfile"))
        name = "xxcisbest/${project.name}"
        println("@#${project.layout.buildDirectory.get().asFile.absolutePath}/libs/${project.name}-${project.version}.jar")
        files(file("${project.layout.buildDirectory.get().asFile.absolutePath}/libs/${project.name}-${project.version}.jar"))
    }

    // 创建一个新的任务，在docker之后启动容器
    val dockerRun by creating {
        dependsOn(dockerPrepare)
        // 通过 dependsOn 来指定依赖关系
        dependsOn("docker")
        // 通过 doFirst 来指定任务执行前的操作
        doFirst {
            // 通过 exec 来执行 shell 命令
            exec {
                commandLine("docker", "stop", "rds")
                commandLine("docker", "rm", "rds")
                // 通过 commandLine 来指定命令
                commandLine("docker", "run", "-p", "8080:8080","--name rds","--network=redis-cluster-with-sentinel_private","xxcisbest/${project.name}")
            }
        }
    }
}


configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.redisson:redisson-spring-boot-starter:3.29.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
