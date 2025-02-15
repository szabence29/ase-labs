import com.pswidersk.gradle.python.VenvTask

plugins {
    id("hu.bme.mit.ase.shingler.gradle.application")
    id("com.pswidersk.python-plugin") version "2.7.2"
}

application {
    mainClass = "hu.bme.mit.ase.shingler.similarity.SimilarityApp"
}

val srcGenJava = "src/gen/java"

sourceSets.main {
    java.srcDir(srcGenJava)
}

dependencies {
    implementation(project(":workflow"))

    implementation(libs.slf4j.api)
    implementation(libs.picocli)

    runtimeOnly(libs.slf4j.logback.impl)

    testImplementation(libs.junit.jupiter.core)
    testImplementation(libs.junit.jupiter.params)

    testRuntimeOnly(libs.junit.jupiter.engine)
}

val installPythonPackages by tasks.registering(VenvTask::class) {
    venvExec = "pip3"

    args = listOf(
        "install",
        "jinja2",
    )
}

val generateSimilarityWorkflow by tasks.registering(VenvTask::class) {
    dependsOn(installPythonPackages)

    inputs.files(
        "src/main/python/generate.py",
        "src/main/jinja/workflow.java.j2",
        "model.json",
    )

    args = listOf(
        "src/main/python/generate.py",
        "model.json",
        "src/main/jinja/workflow.java.j2",
        "$srcGenJava/hu/bme/mit/ase/shingler/similarity/SimilarityWorkflow.java",
    )

    outputs.dir(srcGenJava)
}

tasks.compileJava {
    inputs.files(generateSimilarityWorkflow.get().outputs)
}

tasks.clean {
    delete(srcGenJava)
}
