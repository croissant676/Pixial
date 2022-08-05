package org.pixial.users

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlinx.html.*
import kotlinx.serialization.Serializable
import org.litote.kmongo.eq
import org.pixial.utils.*
import kotlin.time.Duration.Companion.seconds

@Serializable
data class UserSession(
    val userId: Base64Id
) : Principal {
    suspend fun user(): User = userCollection.findOneByIdOrThrow(userId)
}

private suspend fun createUserSession(user: User): UserSession {
    return UserSession(
        userId = user.id
    ).apply {

    }
}

fun Application.installAuthentication() {
    install(Sessions) {
        cookie<UserSession>("user_session", MongoSessionStorage) {
            cookie.encoding = CookieEncoding.BASE64_ENCODING
            identity { random.nextBytes(24).encodeBase64() }
        }
    }
    authentication {
        form("form-auth") {
            userParamName = "username"
            validate {
                val user = userCollection.findOne(User::username eq it.name)
                    ?: return@validate null
                return@validate if (it.password encodesTo user.password) user else null
            }
        }
        session<UserSession> {
            validate {
                it.user()
            }
            challenge("/login")
        }
    }
    routing {
        authenticate("form-auth") {
            post("/login") {
                val user = call.user()
                call.sessions.set(createUserSession(user))
                call.respondRedirect("/app")
            }
            get {

            }
        }
        get("/logout") {
            call.sessions.clear<UserSession>()
            call.respondRedirect("/login")
        }
    }
}