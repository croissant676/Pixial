package org.pixial.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import mu.KotlinLogging

class ConflictException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
class AuthException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

fun determineStatusCode(exception: Throwable): HttpStatusCode = when (exception) {
	is BadRequestException -> HttpStatusCode.BadRequest
	is NotFoundException -> HttpStatusCode.NotFound
	is IllegalArgumentException -> HttpStatusCode.BadRequest
	is ConflictException -> HttpStatusCode.Conflict
	is AuthException -> HttpStatusCode.Unauthorized
	is UnsupportedMediaTypeException -> HttpStatusCode.UnsupportedMediaType
	is ContentTransformationException -> HttpStatusCode.NotAcceptable
	is UnsupportedOperationException -> HttpStatusCode.NotImplemented
	else -> HttpStatusCode.InternalServerError
}


@Serializable
data class ExceptionResponse(
	val timestamp: Instant,
	val status: Int,
	val error: String,
	val message: String,
	val type: String,
	val path: String
)

fun Application.installStatusPages() = install(StatusPages) {
	val logger = KotlinLogging.logger("StatusPages")
	exception<Throwable> { call, throwable ->
		logger.warn(throwable) { "An exception occurred while accepting ${call.request.path()}" }
		val statusCode = determineStatusCode(throwable)
		call.respond(
			statusCode, ExceptionResponse(
				timestamp = Clock.System.now(),
				status = statusCode.value,
				error = statusCode.description,
				message = throwable.message ?: "No message provided.",
				type = throwable.javaClass.canonicalName,
				path = call.request.path()
			)
		)
	}
}