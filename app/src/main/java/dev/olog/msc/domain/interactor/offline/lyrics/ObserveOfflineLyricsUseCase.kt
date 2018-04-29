package dev.olog.msc.domain.interactor.offline.lyrics

import dev.olog.msc.domain.entity.Song
import dev.olog.msc.domain.executors.IoScheduler
import dev.olog.msc.domain.gateway.OfflineLyricsGateway
import dev.olog.msc.domain.interactor.base.ObservableUseCaseUseCaseWithParam
import dev.olog.msc.domain.interactor.detail.item.GetSongUseCase
import dev.olog.msc.utils.MediaId
import io.reactivex.Observable
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import javax.inject.Inject

class ObserveOfflineLyricsUseCase @Inject constructor(
        executors: IoScheduler,
        private val getSongUseCase: GetSongUseCase,
        private val gateway: OfflineLyricsGateway

) : ObservableUseCaseUseCaseWithParam<String, Long>(executors) {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun buildUseCaseObservable(id: Long): Observable<String> {
        return getSongUseCase.execute(MediaId.songId(id))
                .map { getLyricsFromMetadata(it) }
                .onErrorResumeNext(gateway.observeLyrics(id))
    }

    private fun getLyricsFromMetadata(song: Song): String {
        val file = File(song.path)
        val audioFile = AudioFileIO.read(file)
        val tag = audioFile.tagAndConvertOrCreateAndSetDefault
        return tag.getFirst(FieldKey.LYRICS)
    }

}