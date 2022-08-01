package org.pixial.utils

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.plugins.*
import io.ktor.server.routing.*
import mu.KLogger
import mu.toKLogger
import org.pixial.users.User

private var applicationLogger: KLogger? = null

val Application.kLogger: KLogger
    get() {
        if (applicationLogger == null) applicationLogger = log.toKLogger()
        return applicationLogger!!
    }

fun Route.apiRouting(block: Route.() -> Unit) = route("/api", block)

val Application.config get() = environment.config

operator fun ApplicationConfig.get(value: String) = propertyOrNull(value)

val ApplicationConfigValue.stringValue: String get() = getString()
val ApplicationConfigValue.stringValueOrNull: String? get() = kotlin.runCatching { stringValue }.getOrNull()

val ApplicationConfigValue.listValue: List<String> get() = getList()
val ApplicationConfigValue.listValueOrNull: List<String>? get() = kotlin.runCatching { listValue }.getOrNull()

fun checkBR(value: Boolean, block: () -> String = { "A check failed." }) {
    if (!value) throw BadRequestException(block())
}

fun ApplicationCall.user(): User = principal() ?: throw AuthException("No user is signed in right now.")
fun ApplicationCall.userOrNull(): User? = principal()

fun br(reason: String, cause: Throwable? = null): Nothing = throw BadRequestException(reason, cause)