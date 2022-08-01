package org.pixial.users

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
                val result = evaluate(user, newUser)
                userCollection.updateOneById(id, result)
                call.respond(result)
            }
            delete {
                val id = call.id()
//                val user = call.user()
//                if (id != user.id) throw AuthException("Only the user can delete their account.")
                userCollection.deleteOneById(id)
            }
        }
        get {
            val list = userCollection.find().toList()
            call.respond(list)
        }
    }
}