package dev.olog.msc.data.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import dev.olog.msc.data.entity.LastPlayedArtistEntity
import dev.olog.msc.domain.entity.Artist
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
abstract class LastPlayedArtistDao {

    @Query("SELECT * FROM last_played_artists ORDER BY dateAdded DESC LIMIT 20")
    abstract fun getAll(): Flowable<List<LastPlayedArtistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    internal abstract fun insertImpl(entity: LastPlayedArtistEntity)

    @Query("DELETE FROM last_played_artists WHERE id = :artistId")
    internal abstract fun deleteImpl(artistId: Long)

    open fun insertOne(artist: Artist) : Completable {
        return Completable.fromCallable{ deleteImpl(artist.id) }
                .andThen { insertImpl(LastPlayedArtistEntity(
                        artist.id, artist.name
                )) }
    }

}
