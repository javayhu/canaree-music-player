package dev.olog.core.entity

data class LastFmTrack(
        val id: Long,
        val title: String,
        val artist: String,
        val album: String,
        val image: String
)

data class LastFmAlbum(
        val id: Long,
        val title: String,
        val artist: String,
        val image: String
)

data class LastFmArtist(
        val id: Long,
        val image: String
)