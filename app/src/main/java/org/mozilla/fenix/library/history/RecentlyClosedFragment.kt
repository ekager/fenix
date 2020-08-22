package org.mozilla.fenix.library.history

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_history.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import mozilla.components.concept.engine.prompt.ShareData
import mozilla.components.lib.state.ext.consumeFrom
import mozilla.components.support.base.feature.UserInteractionHandler
import org.mozilla.fenix.BrowserDirection
import org.mozilla.fenix.HomeActivity
import org.mozilla.fenix.R
import org.mozilla.fenix.browser.browsingmode.BrowsingMode
import org.mozilla.fenix.components.FenixSnackbar
import org.mozilla.fenix.components.StoreProvider
import org.mozilla.fenix.components.history.createSynchronousPagedHistoryProvider
import org.mozilla.fenix.components.metrics.Event
import org.mozilla.fenix.ext.nav
import org.mozilla.fenix.ext.requireComponents
import org.mozilla.fenix.ext.showToolbar
import org.mozilla.fenix.library.LibraryPageFragment

class RecentlyClosedFragment(override val selectedItems: Set<HistoryItem> = setOf()) :
    LibraryPageFragment<HistoryItem>(), UserInteractionHandler {
    private lateinit var historyStore: HistoryFragmentStore
    private lateinit var historyView: HistoryView
    private lateinit var historyInteractor: HistoryInteractor
    private lateinit var viewModel: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        historyStore = StoreProvider.get(this) {
            HistoryFragmentStore(
                HistoryFragmentState(
                    items = requireComponents.core.recentlyClosedStorage.getRecentlyClosedItems()
                        .map {
                            HistoryItem(
                                it.id,
                                it.title,
                                it.url,
                                it.timeClosed
                            )
                        },
                    mode = HistoryFragmentState.Mode.Normal,
                    pendingDeletionIds = emptySet(),
                    isDeletingItems = false
                )
            )
        }
        val historyController: HistoryController = DefaultHistoryController(
            historyStore,
            findNavController(),
            resources,
            FenixSnackbar.make(
                view = view,
                duration = FenixSnackbar.LENGTH_LONG,
                isDisplayedWithBrowserToolbar = false
            ),
            activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
            lifecycleScope,
            ::openItem,
            {},
            ::invalidateOptionsMenu,
            {},
            {}
        )
        historyInteractor = HistoryInteractor(
            historyController
        )
        historyView = HistoryView(view.historyLayout, historyInteractor)

        return view
    }

    private fun invalidateOptionsMenu() {
        activity?.invalidateOptionsMenu()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = HistoryViewModel(
            requireComponents.core.historyStorage.createSynchronousPagedHistoryProvider()
        )

        requireComponents.analytics.metrics.track(Event.HistoryOpened)

        setHasOptionsMenu(true)
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        consumeFrom(historyStore) {
            historyView.update(it)
        }
    }

    override fun onResume() {
        super.onResume()
        showToolbar(getString(R.string.history_recently_closed_tabs))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.library_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.close_history -> {
            close()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed(): Boolean {
        return historyView.onBackPressed()
    }

    private fun openItem(item: HistoryItem, mode: BrowsingMode? = null) {
        // TODO telemetry?

        mode?.let { (activity as HomeActivity).browsingModeManager.mode = it }

        (activity as HomeActivity).openToBrowserAndLoad(
            searchTermOrURL = item.url,
            newTab = true,
            from = BrowserDirection.FromRecentlyClosed
        )
    }

    private fun share(data: List<ShareData>) {
        // TODO telemetry?
        val directions = RecentlyClosedFragmentDirections.actionGlobalShareFragment(
            data = data.toTypedArray()
        )
        navigate(directions)
    }

    private fun navigate(directions: NavDirections) {
        findNavController().nav(
            R.id.recentlyClosedFragment,
            directions
        )
    }
}
