package com.hyeonslab.ssg.core

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain

class InputOutputPairTest :
  StringSpec({
    "should reject absolute paths in inputFilename" {
      val pair = InputOutputPair(inputFilename = "/etc/passwd", outputPath = "build/test")

      val exception = shouldThrow<IllegalArgumentException> { pair.copyResource() }
      exception.message shouldContain "cannot be an absolute path"
    }

    "should reject path traversal in inputFilename" {
      val pair = InputOutputPair(inputFilename = "../../../etc/passwd", outputPath = "build/test")

      val exception = shouldThrow<IllegalArgumentException> { pair.copyResource() }
      exception.message shouldContain "cannot traverse outside base directory"
    }

    "should reject path traversal in outputPath" {
      val pair = InputOutputPair(inputFilename = "test.txt", outputPath = "../../../tmp")

      val exception = shouldThrow<IllegalArgumentException> { pair.copyResource() }
      exception.message shouldContain "cannot traverse outside base directory"
    }

    "should reject path traversal in outputFilename" {
      val pair =
        InputOutputPair(
          inputFilename = "test.txt",
          outputPath = "build/test",
          outputFilename = "../../etc/passwd",
        )

      val exception = shouldThrow<IllegalArgumentException> { pair.copyResource() }
      exception.message shouldContain "cannot traverse outside base directory"
    }

    "should reject absolute path in outputPath" {
      val pair = InputOutputPair(inputFilename = "test.txt", outputPath = "/etc")

      val exception = shouldThrow<IllegalArgumentException> { pair.copyResource() }
      exception.message shouldContain "cannot be an absolute path"
    }

    "should normalize paths before validation" {
      // This tests that ./foo/../../../etc/passwd is caught
      val pair =
        InputOutputPair(inputFilename = "./foo/../../../etc/passwd", outputPath = "build/test")

      val exception = shouldThrow<IllegalArgumentException> { pair.copyResource() }
      exception.message shouldContain "cannot traverse outside base directory"
    }

    "should allow valid relative paths" {
      // This will fail with "Resource not found" which is expected
      // We're just testing that validation passes
      val pair =
        InputOutputPair(
          inputFilename = "css/style.css",
          outputPath = "build/test",
          outputFilename = "output/style.css",
        )

      // Should throw resource not found, NOT validation error
      val exception = shouldThrow<IllegalStateException> { pair.copyResource() }
      exception.message shouldContain "Resource not found in classpath"
    }
  })
