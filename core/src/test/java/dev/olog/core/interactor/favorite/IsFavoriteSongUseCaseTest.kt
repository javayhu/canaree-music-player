package dev.olog.core.interactor.favorite

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import dev.olog.core.entity.favorite.FavoriteTrackType
import dev.olog.core.gateway.FavoriteGateway
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class IsFavoriteSongUseCaseTest {

    private val gateway = mock<FavoriteGateway>()
    private val sut = IsFavoriteSongUseCase(gateway)

    @Test
    fun testInvoke() = runBlockingTest {
        val id = 1L
        val type = FavoriteTrackType.TRACK

        // when
        sut(id, type)

        // then
        verify(gateway).isFavorite(id, type)
    }

}