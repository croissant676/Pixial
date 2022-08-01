package org.pixial.test

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.StringSpecScope
import io.ktor.server.testing.*
import mu.KLogger
import mu.KotlinLogging

private val loggerMap = mutableMapOf<StringSpecScope, KLogger>()

val StringSpecScope.logger: KLogger
    get() = loggerMap.getOrPut(this) { KotlinLogging.logger("Test-${testCase.name.testName}.") }