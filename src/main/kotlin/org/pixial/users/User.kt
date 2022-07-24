package org.pixial.users

import io.ktor.server.application.*
import io.ktor.server.auth.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.pixial.utils.Base64Id
import org.pixial.utils.database

@Serializable
data class User(
    @SerialName("_id")
    val id: Base64Id,
    var username: String,
    var password: String,
    var email: String,
    var birthday: LocalDate,
    var profilePicture: Base64Id,
    var emailVerified: Boolean = false,
    val createdSets: MutableSet<Base64Id> = mutableSetOf(),
    val favoriteSets: MutableSet<Base64Id> = mutableSetOf(),

): Principal

val userCollection = database.getCollection<User>()

fun Application.userModule() {

}