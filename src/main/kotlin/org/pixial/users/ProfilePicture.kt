package org.pixial.users

import org.pixial.utils.Base64Id
import org.pixial.utils.randomId

val generatedProfilePictures = mutableSetOf<Base64Id>()

fun generateProfilePictures() {
    generatedProfilePictures += randomId()
}