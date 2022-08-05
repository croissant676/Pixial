package org.pixial.core

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.pixial.utils.Base64Id

@Serializable
data class PixialSet(
    @SerialName("_id")
    val id: Base64Id,
    var name: String,
    val creator: Base64Id,
    val creationDate: Instant = Clock.System.now(),
    var folderId: Base64Id? = null,
    var description: String,
    var visitorCount: Int,
    var ratingSum: Int,
    var numberOfRatings: Int,
    val pairs: MutableList<PixialPair> = mutableListOf(),
)

@Serializable
data class PixialPair(
    var term: String,
    var termImage: Base64Id? = null,
    val definition: String,
    var definitionImage: Base64Id? = null
)