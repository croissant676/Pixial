package org.pixial.users

import org.pixial.utils.checkBR
import org.pixial.utils.encode

suspend fun evaluate(oldUser: User, newUser: User): User {
    checkBR(newUser.id == oldUser.id) { "User id must stay the same. Old id = ${oldUser.id}, new id = ${newUser.id}." }
    val resultingUser = newUser.copy()
    if (newUser.password != oldUser.password) resultingUser.password = newUser.password.encode()
    return resultingUser
}