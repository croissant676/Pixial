package org.pixial.core

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.pixial.utils.Base64Id

@Serializable
data class Folder(
    @SerialName("_id")
    val id: Base64Id,
    val name: String,
    val creator: Base64Id,
    val creationDate: Instant,
    var description: String,
    val sets: MutableList<Base64Id> = mutableListOf()
)

@Serializable
data class FolderCreationRequest(
    val creator: Base64Id
)