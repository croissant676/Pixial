package org.pixial.users

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.Identity.encode
import org.pixial.utils.checkBR
import org.pixial.utils.encode
import org.pixial.utils.findOneByIdOrThrow
import org.pixial.utils.id

fun Route.addUserRouting() {
    route("/users") {
        route("/{id}") {
            get {
                val id = call.id()
                val user = userCollection.findOneByIdOrThrow(id)
                call.respond(user)
            }
            put {
                val id = call.id()
                val user = userCollection.findOneByIdOrThrow(id)
                val newUser: User = call.receive()
                checkBR(newUser.id == id)
                if (newUser.password != user.password) newUser.password = newUser.password.encode()
                userCollection.updateOneById(id, newUser)
                call.respond(newUser)
            }
            delete {

            }
        }
    }
}