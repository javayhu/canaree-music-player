package dev.olog.core.interactor.playlist

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import dev.olog.core.MediaId
import dev.olog.core.MediaIdCategory
import dev.olog.core.gateway.podcast.PodcastPlaylistGateway
import dev.olog.core.gateway.track.PlaylistGateway
import dev.olog.test.shared.MainCoroutineRule
import dev.olog.test.shared.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class ClearPlaylistUseCaseTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val playlistGateway = mock<PlaylistGateway>()
    private val podcastGateway = mock<PodcastPlaylistGateway>()
    private val sut = ClearPlaylistUseCase(
        playlistGateway, podcastGateway
    )

    @Test
    fun testInvokePodcast() = coroutineRule.runBlocking {
        // given
        val id = 1L
        val mediaId = MediaId.createCategoryValue(MediaIdCategory.PODCASTS_PLAYLIST, id.toString())

        // when
        sut(mediaId)

        // then
        verify(podcastGateway).clearPlaylist(id)
        verifyZeroInteractions(playlistGateway)
    }

    @Test
    fun testInvokeTrack() = coroutineRule.runBlocking {
        // given
        val id = 1L
        val mediaId = MediaId.createCategoryValue(MediaIdCategory.PLAYLISTS, id.toString())

        // when
        sut(mediaId)

        // then
        verify(playlistGateway).clearPlaylist(id)
        verifyZeroInteractions(podcastGateway)
    }

    @Test
    fun testInvokeWithWrongMediaId() = coroutineRule.runBlocking {
        // given
        val allowed = listOf(
            MediaIdCategory.PLAYLISTS, MediaIdCategory.PODCASTS_PLAYLIST
        )

        for (value in MediaIdCategory.values()) {
            if (value in allowed) {
                continue
            }
            try {
                val mediaId = MediaId.createCategoryValue(value, "1")

                // when
                sut(mediaId)
                Assert.fail("only $allowed is allow, instead was $value")
            } catch (ex: IllegalArgumentException) {
            }
        }

        // then
        verifyZeroInteractions(playlistGateway)
        verifyZeroInteractions(podcastGateway)
    }

}