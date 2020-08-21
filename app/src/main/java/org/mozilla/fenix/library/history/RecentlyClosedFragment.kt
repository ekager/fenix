package org.mozilla.fenix.library.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.fenix.ext.components
import org.mozilla.fenix.library.LibraryPageFragment

class RecentlyClosedFragment(override val selectedItems: Set<HistoryItem>) : LibraryPageFragment<HistoryItem>(), UserInteractionHandler {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
//        requireContext().components.core.historyStorage.getVisited()
    }

    override fun onBackPressed(): Boolean {
        TODO("Not yet implemented")
    }


}
