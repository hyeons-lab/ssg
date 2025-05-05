package com.hyeonslab.ssg.core

import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.Serializable
import java.io.File
import kotlin.use

@Serializable
data class InputOutputPair(
    val inputFilename: String,
    val outputPath: String,
    val outputFilename: String? = null,
)

fun InputOutputPair.copyResource() {
    // use the outputFilename relative to the outputPath, otherwise use the inputFilename
    val candidateOutputFilename =
        (outputFilename ?: inputFilename).let {
            "$outputPath/$it"
        }
    val parentFile = File(candidateOutputFilename).parentFile
    if (!parentFile.exists()) {
        parentFile.mkdirs()
    }
    ClassLoader.getSystemResourceAsStream(inputFilename)?.asSource()?.buffered()?.use { input ->
        File(candidateOutputFilename).let { file ->
            file.outputStream().asSink().buffered().use { outputFileBuffer ->
                outputFileBuffer.transferFrom(input)
            }
        }
    }
}
