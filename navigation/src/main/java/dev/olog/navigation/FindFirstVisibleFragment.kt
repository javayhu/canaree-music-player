package dev.olog.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import dev.olog.core.extensions.getTopFragment
import dev.olog.navigation.screens.FragmentScreen
import timber.log.Timber

private val basicFragments = listOf(
    FragmentScreen.LIBRARY_TRACKS.tag,
    FragmentScreen.LIBRARY_PODCAST.tag,
    FragmentScreen.SEARCH.tag,
    FragmentScreen.QUEUE.tag
)

fun findFirstVisibleFragment(fragmentManager: FragmentManager): Fragment? {
    var topFragment = fragmentManager.getTopFragment()
    if (topFragment == null) {
        topFragment = fragmentManager.fragments
            .filter { it.isVisible }
            .firstOrNull { basicFragments.contains(it.tag) }
    }
    if (topFragment == null) {
        Timber.e("Navigator: Something went wrong, for some reason no fragment was found")
    }
    return topFragment
}