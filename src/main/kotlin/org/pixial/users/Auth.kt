package org.pixial.users

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import org.litote.kmongo.eq
import org.pixial.utils.encodesTo

fun Application.installAuthentication() {
    authentication {
        form {
            userParamName = "username"
            validate {
                val user = userCollection.findOne(User::username eq it.name)
                    ?: return@validate null
                return@validate if (it.password encodesTo user.password) user else null
            }
        }
    }
    routing {
        route("/login") {
            authenticate {
                post {
                    call.respondRedirect("/app")
                }
            }
            get {
                call.respondHtml {
                    body {
                        form(
                            action = "/login",
                            encType = FormEncType.applicationXWwwFormUrlEncoded,
                            method = FormMethod.post
                        ) {
                            p {
                                +"Username:"
                                textInput(name = "username")
                            }
                            p {
                                +"Password:"
                                passwordInput(name = "password")
                            }
                            p {
                                submitInput() { value = "Login" }
                            }
                        }
                    }
                }
            }
        }
    }
}