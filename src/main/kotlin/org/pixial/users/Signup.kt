package org.pixial.users

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.axay.simplekotlinmail.delivery.send
import net.axay.simplekotlinmail.email.emailBuilder
import org.litote.kmongo.eq
import org.litote.kmongo.json
import org.pixial.utils.*
import kotlin.time.Duration.Companion.days

@Serializable
data class UserSignup(
    val username: String,
    val password: String,
    val email: String,
    val birthday: LocalDate,
)

suspend fun signup(userSignup: UserSignup): User {
    validate(userSignup)
    val user = User(
        id = randomId(),
        username = userSignup.username,
        password = userSignup.password.encode(),
        email = userSignup.email,
        birthday = userSignup.birthday
    )
    userCollection.insertOne(user)
    val verificationAttempt = UserVerificationAttempt(
        token = base64Encoder.encodeToString(random.nextBytes(30)),
        user = user.id,
        email = userSignup.email,
        expirationDate = Clock.System.now() + 1.days,
        first = true
    )
    verificationCollection.insertOne(verificationAttempt)
    sendEmail(user, verificationAttempt)
    return user
}

private val validFirstChars = ('A'..'Z') + ('a'..'z')
private val validUsernameCharacters = validFirstChars + ('0'..'9') + '_'
private val validUsernameLengths = 5..20

private suspend fun validate(userSignup: UserSignup) {
    if (userCollection.findOne(User::username eq userSignup.username) != null)
        throw ConflictException("A user with the username \"${userSignup.username}\" already exists.")
    if (userCollection.findOne(User::email eq userSignup.email) != null)
        throw ConflictException("A user with the email \"${userSignup.email}\" already exists.")
    val username = userSignup.username
    if (username.first() !in validFirstChars) throw BadRequestException("The first character in the username must be a letter (A-Z, a-z)")
    username.drop(1).forEach {
        if (it !in validUsernameCharacters) throw BadRequestException("All of the letters in the username must be a letter, a number, or an underscore. (A-Z, a-z, 0-9, _)")
    }
    if (username.length !in validUsernameLengths) throw BadRequestException("The length of the username must within the range 5..20.")
}

private suspend fun sendEmail(user: User, verification: UserVerificationAttempt) {
    val link = "/verify/accept/${verification.token}"
    emailBuilder {
        to(user.email)
        from(senderEmail)
        withSubject("Hello there, ${user.username}")
        withPlainText(link)
    }.send().join()
}

@Serializable
data class UserVerificationAttempt(
    @SerialName("_id")
    val token: String,
    val user: Base64Id,
    val email: String,
    val expirationDate: Instant,
    val first: Boolean,
    var finished: Boolean = false
)

private val verificationCollection =
    database.getCollection<UserVerificationAttempt>("verifications")

fun Route.addSignupRouting() {
    post("/users") {
        val signup: UserSignup = call.receive()
        val user = signup(signup)
        call.respond(user)
    }
    route("/verify") {
        get("/accept/{id}") {
            val id = call.parameters["id"] ?: throw MissingRequestParameterException("id")
            if (id.length != 40) throw BadRequestException("Verification ID $id is not a valid id.")
            val verificationAttempt = verificationCollection.findOneByIdOrThrow(id)
            if (verificationAttempt.finished) return@get call.respond(HttpStatusCode.Conflict)
            val currentTime = Clock.System.now()
            if (currentTime > verificationAttempt.expirationDate) call.respond(HttpStatusCode.Gone)
            val user = userCollection.findOneByIdOrThrow(verificationAttempt.user)
            if (user.emailVerified) throw IllegalStateException("User email has already been verified. (${user.id} - ${verificationAttempt.token})")
            user.emailVerified = true
            verificationAttempt.finished = true
            verificationCollection.updateOneById(id, verificationAttempt)
            userCollection.updateOneById(user.id, user)
            call.respond(HttpStatusCode.OK)
        }
        delete("/{id}") {
            val id = call.parameters["id"] ?: throw MissingRequestParameterException("id")
            if (id.length != 40) throw BadRequestException("Verification ID $id is not a valid id.")
            verificationCollection.findOneByIdOrThrow(id) // Throw a not found if none exists
            verificationCollection.deleteOneById(id)
        }
    }
}