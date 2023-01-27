package com.myapplications.mywatchlist.data.mappers

import com.myapplications.mywatchlist.data.entities.CastMemberForMovieEntity
import com.myapplications.mywatchlist.data.entities.CastMemberForTvEntity
import com.myapplications.mywatchlist.domain.entities.CastMember
import com.myapplications.mywatchlist.domain.entities.Movie
import com.myapplications.mywatchlist.domain.entities.TV

/**
 * Converts a [CastMember] to [CastMemberForMovieEntity]
 * @param movieId is the id of the [Movie] the [CastMember] is associated with.
 */
fun CastMember.toCastMemberForMovieEntity(movieId: Long): CastMemberForMovieEntity {
    return CastMemberForMovieEntity(
        id = this.id,
        name = this.name,
        character = this.character,
        pictureLink = this.pictureLink,
        movieId = movieId
    )
}

/**
 * Converts a [CastMember] to [CastMemberForTvEntity]
 * @param tvId is the id of the [TV] the [CastMember] is associated with.
 */
fun CastMember.toCastMemberForTvEntity(tvId: Long): CastMemberForTvEntity {
    return CastMemberForTvEntity(
        id = this.id,
        name = this.name,
        character = this.character,
        pictureLink = this.pictureLink,
        tvId = tvId
    )
}

/**
 * Converts a list of [CastMember]s to a list of [CastMemberForMovieEntity]
 * @param movieId is the id of the [Movie] the [CastMember]s are associated with.
 */
fun List<CastMember>.toCastMembersForMovieEntity(movieId: Long): List<CastMemberForMovieEntity> {
    return this.map {
        it.toCastMemberForMovieEntity(movieId)
    }
}

/**
 * Converts a list of [CastMember]s to a list of [CastMemberForTvEntity]
 * @param tvId is the id of the [TV] the [CastMember]s are associated with.
 */
fun List<CastMember>.toCastMembersForTvEntity(tvId: Long): List<CastMemberForTvEntity> {
    return this.map {
        it.toCastMemberForTvEntity(tvId)
    }
}

/**
 * @return [CastMember] with full picture link that includes the base url instead of only the ending.
 */
fun CastMember.withFullProfilePictureLinks(
    imagesBaseUrl: String,
    profileImageSize: String
): CastMember {
    // Check if link is not null and whether it already contains "http" which would indicate a full link
    val link = if (this.pictureLink != null && !this.pictureLink.contains("http")) {
        imagesBaseUrl + profileImageSize + this.pictureLink
    } else {
        null
    }
    return CastMember(
        id = this.id,
        name = this.name,
        character = this.character,
        pictureLink = link
    )
}

/**
 * @return list of [CastMember]s with full picture links that includes the base url instead of only
 * the ending.
 */
fun List<CastMember>.withFullProfilePictureLinks(
    imagesBaseUrl: String,
    profileImageSize: String
): List<CastMember> {
    return this.map {
        it.withFullProfilePictureLinks(imagesBaseUrl, profileImageSize)
    }
}