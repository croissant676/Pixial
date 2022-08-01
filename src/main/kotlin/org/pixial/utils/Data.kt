package org.pixial.utils

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.pixial.developmentLogger
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

private val passwordEncoder: PasswordEncoder = Argon2PasswordEncoder()
val database = KMongo.createClient().getDatabase("pixial").coroutine

suspend inline fun <reified T : Any> CoroutineCollection<T>.findOneByIdOrThrow(id: Any) =
    findOneById(id) ?: notFound<T>(id)

inline fun <reified T : Any> notFound(id: Any): Nothing =
    throw NotFoundException("Could not find a ${T::class} with the id \"$id\".")

suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.IO, block)

suspend fun String.encode(): String = withIO { passwordEncoder.encode(this@encode) }

suspend infix fun String.encodesTo(other: String): Boolean = withIO {
    passwordEncoder.matches(this@encodesTo, other)
}

fun Application.checkHoconDeletionStatus() {
    val deletion = this.config["pixial.development.delete"] ?: return
    val stringValue = deletion.stringValueOrNull
    if (stringValue != null) launch {
        if (stringValue.equals("all", ignoreCase = true)) clearDatabases()
        else clearDatabases(listOf(stringValue))
    } else launch {
        clearDatabases(deletion.listValue)
    }
}

suspend fun clearDatabases(listNames: List<String>? = null) {
    val actualList = listNames ?: database.listCollectionNames()
    actualList.forEach {
        val collection = database.getCollection<Any>(it)
        collection.deleteMany()
    }
    developmentLogger.debug { "Deleted all elements of collection $actualList" }
}