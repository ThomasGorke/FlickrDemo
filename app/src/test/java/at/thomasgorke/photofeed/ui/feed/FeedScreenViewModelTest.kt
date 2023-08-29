package at.thomasgorke.photofeed.ui.feed

import at.thomasgorke.photofeed.RepositoryResponse
import at.thomasgorke.photofeed.ViewModelDispatcherRule
import at.thomasgorke.photofeed.data.FlickrDataSource
import at.thomasgorke.photofeed.data.model.DataState
import at.thomasgorke.photofeed.data.model.FeedItem
import at.thomasgorke.photofeed.data.model.RepositoryException
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FeedScreenViewModelTest {

    @get:Rule
    val viewModelDispatcherRule = ViewModelDispatcherRule()

    @MockK
    lateinit var dataSource: FlickrDataSource

    private lateinit var viewModel: FeedScreenViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    /**
     * As some test results depend on the initial flow, it is not possible to define
     * it in the setup
     */
    private fun initViewModel(successBehavior: Boolean = true) {
        if (successBehavior) {
            every { dataSource.getFeedFlow() } returns flowOf(
                RepositoryResponse.Success(listOf(FeedItem("url", "title", "author", false)))
            )
        } else {
            every { dataSource.getFeedFlow() } returns flowOf(RepositoryResponse.Success(emptyList()))
        }

        viewModel = FeedScreenViewModel(dataSource)
    }

    @Test
    fun `reload should reload feed successfully`() = runTest {
        // Do
        initViewModel()
        coEvery { dataSource.fetchNewRemoteFeed() } returns RepositoryResponse.Success(Unit)

        // When
        viewModel.execute(FeedScreenViewModel.Action.Retry)

        // Then
        coVerify(exactly = 1) { dataSource.fetchNewRemoteFeed() }
        assertEquals(DataState.LOADING, viewModel.state.value.dataState)
    }

    @Test
    fun `reload feed should set error state`() = runTest {
        // Do
        initViewModel(successBehavior = false)
        coEvery { dataSource.fetchNewRemoteFeed() } returns RepositoryResponse.Error(
            RepositoryException.RemoteException(Exception())
        )

        // When
        viewModel.execute(FeedScreenViewModel.Action.Retry)

        // Then
        coVerify(exactly = 1) { dataSource.fetchNewRemoteFeed() }
        assertEquals(DataState.ERROR, viewModel.state.value.dataState)
        assertEquals(
            emptyList<FeedItem>(),
            viewModel.state.value.feed
        )
    }

    @Test
    fun `reload feed should show previous list on error`() = runTest {
        // Do
        initViewModel()
        coEvery { dataSource.fetchNewRemoteFeed() } returns RepositoryResponse.Error(
            RepositoryException.RemoteException(Exception())
        )

        // When
        viewModel.execute(FeedScreenViewModel.Action.Retry)

        // Then
        coVerify(exactly = 1) { dataSource.fetchNewRemoteFeed() }
        assertEquals(DataState.SUCCESS, viewModel.state.value.dataState)
        assertEquals(
            listOf(FeedItem("url", "title", "author", false)),
            viewModel.state.value.feed
        )
    }
}