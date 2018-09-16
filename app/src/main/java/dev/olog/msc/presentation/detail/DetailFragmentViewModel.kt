package dev.olog.msc.presentation.detail

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import dev.olog.msc.domain.entity.SortArranging
import dev.olog.msc.domain.entity.SortType
import dev.olog.msc.domain.interactor.GetDetailTabsVisibilityUseCase
import dev.olog.msc.domain.interactor.all.sorted.util.*
import dev.olog.msc.presentation.detail.sort.DetailSort
import dev.olog.msc.presentation.model.DisplayableItem
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.MediaIdCategory
import dev.olog.msc.utils.k.extension.asLiveData
import dev.olog.msc.utils.k.extension.debounceFirst
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DetailFragmentViewModel @Inject constructor(
        val mediaId: MediaId,
        item: Map<MediaIdCategory, @JvmSuppressWildcards Flowable<List<DisplayableItem>>>,
        albums: Map<MediaIdCategory, @JvmSuppressWildcards Observable<List<DisplayableItem>>>,
        data: Map<String, @JvmSuppressWildcards Observable<List<DisplayableItem>>>,
        private val presenter: DetailFragmentPresenter,
        private val setSortOrderUseCase: SetSortOrderUseCase,
        private val observeSortOrderUseCase: GetSortOrderUseCase,
        private val setSortArrangingUseCase: SetSortArrangingUseCase,
        private val getSortArrangingUseCase: GetSortArrangingUseCase,
        getVisibleTabsUseCase : GetDetailTabsVisibilityUseCase,
        private val getDetailSortDataUseCase: GetDetailSortDataUseCase

) : ViewModel() {

    companion object {
        const val RECENTLY_ADDED = "RECENTLY_ADDED"
        const val MOST_PLAYED = "MOST_PLAYED"
        const val RELATED_ARTISTS = "RELATED_ARTISTS"
        const val SONGS = "SONGS"

        const val NESTED_SPAN_COUNT = 4
        const val VISIBLE_RECENTLY_ADDED_PAGES = NESTED_SPAN_COUNT * 4
        const val RELATED_ARTISTS_TO_SEE = 10
    }

    private val currentCategory = mediaId.category

    private val subscriptions = CompositeDisposable()

    val itemLiveData: LiveData<List<DisplayableItem>> = item[currentCategory]!!
            .debounceFirst()
            .onErrorReturnItem(listOf())
            .asLiveData()

    private val dataMap : Observable<MutableMap<DetailFragmentDataType, MutableList<DisplayableItem>>> =
            Observables.combineLatest(
                    item[currentCategory]!!.toObservable().debounceFirst(1, TimeUnit.SECONDS).distinctUntilChanged(),
                    data[MOST_PLAYED]!!.debounceFirst(500, TimeUnit.MILLISECONDS).distinctUntilChanged(),
                    data[RECENTLY_ADDED]!!.debounceFirst(1, TimeUnit.SECONDS).distinctUntilChanged(),
                    albums[currentCategory]!!.debounceFirst(500, TimeUnit.MILLISECONDS).distinctUntilChanged(),
                    data[RELATED_ARTISTS]!!.debounceFirst(500, TimeUnit.MILLISECONDS).distinctUntilChanged(),
                    data[SONGS]!!.debounceFirst(50, TimeUnit.MILLISECONDS).distinctUntilChanged(),
                    getVisibleTabsUseCase.execute()
            ) { item, mostPlayed, recent, albums, artists, songs, visibility ->
                presenter.createDataMap(item, mostPlayed, recent, albums, artists, songs, visibility)
            }.onErrorReturnItem(mutableMapOf())
                    .debounceFirst(100, TimeUnit.MILLISECONDS)

    override fun onCleared() {
        subscriptions.clear()
    }

    fun observeData(): LiveData<MutableMap<DetailFragmentDataType, MutableList<DisplayableItem>>> =
            dataMap.debounceFirst()
                    .asLiveData()

    fun artistMediaId(action: (MediaId) -> Unit) {
        presenter.artistMediaId()
                .subscribe({ action(it) }, Throwable::printStackTrace)
                .addTo(subscriptions)
    }

    val mostPlayedLiveData: LiveData<List<DisplayableItem>> = data[MOST_PLAYED]!!
            .debounceFirst()
            .asLiveData()

    val relatedArtistsLiveData : LiveData<List<DisplayableItem>> = data[RELATED_ARTISTS]!!
            .debounceFirst()
            .map { it.take(RELATED_ARTISTS_TO_SEE) }
            .asLiveData()

    val albumsLiveData: LiveData<List<DisplayableItem>> = albums[currentCategory]!!
            .debounceFirst()
            .asLiveData()

    val recentlyAddedLiveData: LiveData<List<DisplayableItem>> = data[RECENTLY_ADDED]!!
            .debounceFirst()
            .map { it.take(VISIBLE_RECENTLY_ADDED_PAGES) }
            .asLiveData()

    fun detailSortDataUseCase(mediaId: MediaId, action: (DetailSort) -> Unit){
        getDetailSortDataUseCase.execute(mediaId)
                .subscribe(action, Throwable::printStackTrace)
                .addTo(subscriptions)
    }

    fun observeSortOrder(action: (SortType) -> Unit) {
        observeSortOrderUseCase.execute(mediaId)
                .firstOrError()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ action(it) }, Throwable::printStackTrace)
                .addTo(subscriptions)
    }

    fun updateSortOrder(sortType: SortType){
        setSortOrderUseCase.execute(SetSortOrderRequestModel(mediaId, sortType))
                .subscribe({ }, Throwable::printStackTrace)
                .addTo(subscriptions)
    }

    fun toggleSortArranging(){
        observeSortOrderUseCase.execute(mediaId)
                .firstOrError()
                .filter { it != SortType.CUSTOM }
                .flatMapCompletable { setSortArrangingUseCase.execute() }
                .subscribe({ }, Throwable::printStackTrace)
                .addTo(subscriptions)

    }

    fun moveItemInPlaylist(from: Int, to: Int){
        presenter.moveInPlaylist(from, to)
    }

    fun removeFromPlaylist(item: DisplayableItem) {
        presenter.removeFromPlaylist(item)
                .subscribe({}, Throwable::printStackTrace)
                .addTo(subscriptions)
    }

    fun observeSorting(): Observable<Pair<SortType, SortArranging>>{
        return Observables.combineLatest(
                observeSortOrderUseCase.execute(mediaId),
                getSortArrangingUseCase.execute(),
                { sort, arranging -> Pair(sort, arranging) }
        )
    }

    fun showSortByTutorialIfNeverShown(): Completable {
        return presenter.showSortByTutorialIfNeverShown()
    }

}