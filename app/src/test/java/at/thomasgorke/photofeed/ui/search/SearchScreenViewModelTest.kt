@file:OptIn(ExperimentalTime::class)

package at.thomasgorke.photofeed.ui.search

import app.cash.turbine.test
import at.thomasgorke.photofeed.RepositoryResponse
import at.thomasgorke.photofeed.ViewModelDispatcherRule
import at.thomasgorke.photofeed.data.FlickrDataSource
import at.thomasgorke.photofeed.data.model.DataState
import at.thomasgorke.photofeed.data.model.FeedItem
import at.thomasgorke.photofeed.data.remote.model.TagMode
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date
import kotlin.time.ExperimentalTime

class SearchScreenViewModelTest {

    @get:Rule
    val rule = ViewModelDispatcherRule(testDispatcher = StandardTestDispatcher())

    @MockK
    lateinit var flickrDataSource: FlickrDataSource

    private lateinit var viewModel: SearchScreenViewModel

    private val testDate = Date()

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        every { flickrDataSource.getFavoritesFlow() } returns emptyFlow()
        coEvery { flickrDataSource.fetchFeedByTags(any(), any()) } returns RepositoryResponse.Success(
            emptyList()
        )

        viewModel = SearchScreenViewModel(flickrDataSource)
    }

    @Test
    fun `test debounce`() = runTest {
        // Do

        // When
        viewModel.execute(SearchScreenViewModel.Action.Search("test"))
        viewModel.execute(SearchScreenViewModel.Action.Search("testc"))
        viewModel.execute(SearchScreenViewModel.Action.Search("testca"))
        delay(500)
        viewModel.execute(SearchScreenViewModel.Action.Search("testcas"))
        viewModel.execute(SearchScreenViewModel.Action.Search("testcase"))
        advanceTimeBy(1000)

        // Then
        coVerifyOrder {
            flickrDataSource.fetchFeedByTags("testca")
            flickrDataSource.fetchFeedByTags("testcase")
        }

        coVerify(exactly = 2) { flickrDataSource.fetchFeedByTags(any(), any()) }

        assertEquals("testcase", viewModel.state.value.query)
        assertEquals(DataState.SUCCESS, viewModel.state.value.dataState)
    }

    @Test
    fun `test debounce with turbine`() = runTest {
        // Do
        val response = listOf(FeedItem("url", "title", "author", false, testDate))
        val response2 = listOf(FeedItem("url", "title", "author", false, testDate))

        coEvery { flickrDataSource.fetchFeedByTags("testca", any()) } returns
                RepositoryResponse.Success(response)

        coEvery { flickrDataSource.fetchFeedByTags("testcase", any()) } returns
                RepositoryResponse.Success(response2)


        // When
        viewModel.state.test {
            awaitItem().let {
                assertEquals(DataState.SUCCESS, it.dataState)
                assertEquals(true, it.tagModeIsAll)
            }
            viewModel.execute(SearchScreenViewModel.Action.Search("test"))
            assertEquals("test", awaitItem().query)
            viewModel.execute(SearchScreenViewModel.Action.Search("testc"))
            assertEquals("testc", awaitItem().query)
            viewModel.execute(SearchScreenViewModel.Action.Search("testca"))
            assertEquals("testca", awaitItem().query)

            delay(400)
            advanceUntilIdle()
            awaitItem().let {
                assertEquals(DataState.LOADING, it.dataState)
                assertEquals("testca", it.query)
                assertEquals(emptyList<FeedItem>(), it.result)
                assertEquals(true, it.tagModeIsAll)
            }
            awaitItem().let {
                assertEquals(DataState.SUCCESS, it.dataState)
                assertEquals("testca", it.query)
                assertEquals(response, it.result)
                assertEquals(true, it.tagModeIsAll)
            }
            viewModel.execute(SearchScreenViewModel.Action.Search("testcas"))
            assertEquals("testcas", awaitItem().query)
            viewModel.execute(SearchScreenViewModel.Action.Search("testcase"))
            assertEquals("testcase", awaitItem().query)
            delay(400)
            advanceUntilIdle()
            awaitItem().let {
                assertEquals(DataState.LOADING, it.dataState)
                assertEquals("testcase", it.query)
                assertEquals(response, it.result)
                assertEquals(true, it.tagModeIsAll)
            }
            awaitItem().let {
                assertEquals(DataState.SUCCESS, it.dataState)
                assertEquals("testcase", it.query)
                assertEquals(response2, it.result)
                assertEquals(true, it.tagModeIsAll)
            }

            viewModel.execute(SearchScreenViewModel.Action.ToggleTagMode)
            assertEquals(false, awaitItem().tagModeIsAll)
            assertEquals(DataState.LOADING, awaitItem().dataState)

            advanceUntilIdle()
            awaitItem().let {
                assertEquals(DataState.SUCCESS, it.dataState)
                assertEquals("testcase", it.query)
                assertEquals(response2, it.result)
                assertEquals(false, it.tagModeIsAll)
            }
        }

        // Then
        coVerifyOrder {
            flickrDataSource.fetchFeedByTags("testca", TagMode.ALL)
            flickrDataSource.fetchFeedByTags("testcase", TagMode.ALL)
            flickrDataSource.fetchFeedByTags("testcase", TagMode.ANY)
        }

        coVerify(exactly = 2) { flickrDataSource.fetchFeedByTags(any(), TagMode.ALL) }
        coVerify(exactly = 1) { flickrDataSource.fetchFeedByTags(any(), TagMode.ANY) }
    }
}