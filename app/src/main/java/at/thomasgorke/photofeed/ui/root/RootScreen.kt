package at.thomasgorke.photofeed.ui.root

import androidx.compose.runtime.Composable
import at.thomasgorke.photofeed.ui.NavGraphs
import com.ramcosta.composedestinations.DestinationsNavHost

@Composable
fun RootScreen() {
    DestinationsNavHost(navGraph = NavGraphs.root)
}