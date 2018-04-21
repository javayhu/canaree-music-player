package dev.olog.msc.music.service.player.crossfade

import dev.olog.msc.music.service.model.MediaEntity
import dev.olog.msc.music.service.player.CustomExoPlayer
import dev.olog.msc.utils.assertMainThread
import javax.inject.Inject

private enum class CurrentPlayer {
    PLAYER_NOT_SET,
    PLAYER_ONE,
    PLAYER_TWO
}

class CrossFadePlayer @Inject internal constructor(
        private val playerOne: CrossFadePlayerImpl,
        private val playerTwo: CrossFadePlayerImpl

): CustomExoPlayer<MediaEntity> {

    private var current = CurrentPlayer.PLAYER_NOT_SET

    override fun prepare(mediaEntity: MediaEntity, bookmark: Long){
        assertMainThread()

        val player = getNextPlayer()
        player.prepare(mediaEntity.toSimpleCrossFadeModel(), bookmark)
    }

    override fun play(mediaEntity: MediaEntity, hasFocus: Boolean, isTrackEnded: Boolean) {
        assertMainThread()
        val player = getNextPlayer()
        player.play(mediaEntity.toSimpleCrossFadeModel(), hasFocus, isTrackEnded)
        if (!isTrackEnded){
            getSecondaryPlayer().stop()
        }
    }

    override fun resume() {
        getCurrentPlayer().resume()
    }

    override fun pause() {
        getCurrentPlayer().pause()
        getSecondaryPlayer().stop()
    }

    override fun seekTo(where: Long) {
        getCurrentPlayer().seekTo(where)
        getSecondaryPlayer().stop()
    }

    override fun isPlaying(): Boolean = getCurrentPlayer().isPlaying()
    override fun getBookmark(): Long = getCurrentPlayer().getBookmark()
    override fun getDuration(): Long = getCurrentPlayer().getDuration()

    override fun setVolume(volume: Float) {
        getCurrentPlayer().setVolume(volume)
        getSecondaryPlayer().setVolume(volume)
    }

    private fun getNextPlayer(): CrossFadePlayerImpl {
        current = when (current){
            CurrentPlayer.PLAYER_NOT_SET,
            CurrentPlayer.PLAYER_TWO -> CurrentPlayer.PLAYER_ONE
            CurrentPlayer.PLAYER_ONE -> CurrentPlayer.PLAYER_TWO
        }

        return when (current){
            CurrentPlayer.PLAYER_ONE -> playerOne
            CurrentPlayer.PLAYER_TWO -> playerTwo
            else -> throw IllegalStateException("invalid current player")
        }
    }

    private fun getCurrentPlayer(): CrossFadePlayerImpl {
        return when (current){
            CurrentPlayer.PLAYER_ONE -> playerOne
            CurrentPlayer.PLAYER_TWO -> playerTwo
            else -> throw IllegalStateException("invalid secondary player")
        }
    }

    private fun getSecondaryPlayer(): CrossFadePlayerImpl {
        return when (current){
            CurrentPlayer.PLAYER_ONE -> playerTwo
            CurrentPlayer.PLAYER_TWO -> playerOne
            else -> throw IllegalStateException("invalid secondary player")
        }
    }

    private fun MediaEntity.toSimpleCrossFadeModel(): CrossFadePlayerImpl.Model {
        return CrossFadePlayerImpl.Model(this, false, -1)
    }

}