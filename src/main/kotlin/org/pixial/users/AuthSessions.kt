package org.pixial.users

import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.PairSerializer
import org.litote.kmongo.MongoOperator
import org.pixial.utils.database
import org.pixial.utils.findOneByIdOrThrow

@Serializable
data class SessionPair(
    val _id: String,
    val value: String
)

object MongoSessionStorage : SessionStorage {
    private val collection = database.getCollection<SessionPair>("sessions")
    override suspend fun invalidate(id: String) {
        collection.deleteOneById(id)
    }

    override suspend fun read(id: String): String = collection.findOneByIdOrThrow(id).value
    override suspend fun write(id: String, value: String) {
        collection.save(SessionPair(id, value))
    }
}