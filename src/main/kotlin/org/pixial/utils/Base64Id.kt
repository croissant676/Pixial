package org.pixial.utils

import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.FlexibleDecoder
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bson.BsonType
import java.security.SecureRandom
import java.util.*
import kotlin.random.asKotlinRandom

val base64Encoder = Base64.getUrlEncoder()!!
val base64Decoder = Base64.getUrlDecoder()!!
val random = SecureRandom().asKotlinRandom()

@Serializable(with = Base64IdSerializer::class)
class Base64Id internal constructor(internal val data: ByteArray) {
	override fun equals(other: Any?): Boolean = other is Base64Id && data.contentEquals(other.data)
	override fun hashCode(): Int = data.contentHashCode()
	override fun toString(): String = base64Encoder.encodeToString(data)
}

fun idFromString(source: String) =
	idFromStringOrNull(source) ?: throw IllegalArgumentException("Base64 identifier string $source is not a valid id.")

fun idFromStringBR(source: String) =
	idFromStringOrNull(source) ?: throw BadRequestException("Base64 identifier string $source is not a valid id.")

fun idFromStringOrNull(source: String): Base64Id? = if (source.length != 20) null else runCatching {
	Base64Id(base64Decoder.decode(source))
}.getOrNull()

fun randomId(): Base64Id = Base64Id(random.nextBytes(15))

object Base64IdSerializer : KSerializer<Base64Id> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Base64Id", PrimitiveKind.STRING)
	override fun serialize(encoder: Encoder, value: Base64Id) =
		if (encoder is BsonEncoder) encoder.encodeByteArray(value.data) else encoder.encodeString(value.toString())

	override fun deserialize(decoder: Decoder): Base64Id =
		if (decoder is FlexibleDecoder && decoder.reader.currentBsonType == BsonType.BINARY) Base64Id(decoder.reader.readBinaryData().data)
		else idFromString(decoder.decodeString())
}

fun ApplicationCall.id(parameterName: String = "id"): Base64Id =
	idFromStringBR(parameters[parameterName] ?: throw MissingRequestParameterException(parameterName))

fun ApplicationCall.idOrNull(parameterName: String = "id"): Base64Id? {
	val parameter = parameters[parameterName] ?: return null
	return idFromStringOrNull(parameter)
}

fun ApplicationCall.idQuery(parameterName: String = "id"): Base64Id =
	idFromStringBR(request.queryParameters[parameterName] ?: throw MissingRequestParameterException(parameterName))

fun ApplicationCall.idQueryOrNull(parameterName: String = "id"): Base64Id? {
	val parameter = request.queryParameters[parameterName] ?: return null
	return idFromStringOrNull(parameter)
}