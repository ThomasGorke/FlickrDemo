package at.thomasgorke.photofeed.ui.feed

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.koinViewModel

@RootNavGraph(start = true)
@Destination
@Composable
fun FeedScreen(
    navigator: DestinationsNavigator,
    viewModel: FeedScreenViewModel = koinViewModel()
) {

}