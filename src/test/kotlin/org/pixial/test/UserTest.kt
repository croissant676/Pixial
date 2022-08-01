package org.pixial.test

import io.kotest.assertions.ktor.client.shouldHaveStatus
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.datetime.LocalDate
import org.pixial.users.User
import org.pixial.users.UserSignup
import org.pixial.users.userCollection
import org.pixial.utils.clearDatabases

class UserTest : StringSpec({
    beforeTest {
        clearDatabases()
    }
    "Make sure users are added after post request" {
        testApplication {
            val client = createClient {
                install(ContentNegotiation) {
                    json()
                }
            }
            val response = client.post("/users/") {
                setBody(
                    UserSignup(
                        "croissant676",
                        "password",
                        "croissant676@outlook.com",
                        LocalDate(2000, 1, 1)
                    )
                )
                contentType(ContentType.Application.Json)
            }
            logger.info { "Resulting body = $response" }
            response shouldHaveStatus HttpStatusCode.OK
            val user = response.body<User>()
            user.username shouldBe "croissant676"
            user.password shouldNotBe "password"
            user.email shouldBe "croissant676@outlook.com"
            user.birthday shouldBe LocalDate(2000, 1, 1)
            val databaseUser = userCollection.find().first()
            user shouldBe databaseUser
        }
    }
})