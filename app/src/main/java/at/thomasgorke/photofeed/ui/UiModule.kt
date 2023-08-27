package at.thomasgorke.photofeed.ui

import at.thomasgorke.photofeed.ui.favorites.FavoriteScreenViewModel
import at.thomasgorke.photofeed.ui.feed.FeedScreenViewModel
import at.thomasgorke.photofeed.ui.search.SearchScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val uiModule = module {
    viewModel { FeedScreenViewModel(dataSource = get()) }
    viewModel { SearchScreenViewModel(flickrDataSource = get()) }
    viewModel { FavoriteScreenViewModel(dataSource = get()) }
}
