package at.thomasgorke.photofeed.ui.feed

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val feedModule = module {
    viewModel { FeedScreenViewModel(dataSource = get()) }
}