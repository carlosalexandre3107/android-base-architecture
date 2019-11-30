package com.example.basearch.presentation.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import br.com.pebmed.domain.entities.PullRequest
import br.com.pebmed.domain.entities.User
import br.com.pebmed.domain.usecases.ListPullRequestsUseCase
import com.example.basearch.presentation.extensions.loadViewStateResourceList
import com.example.basearch.presentation.ui.ViewStateResource
import com.jraska.livedata.test
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.*

/**
 * What to test?
 * 1 - If all functions are called in correct order
 * 2 - If pass the correct params to the respective UseCase
 * 3 - If the flow follow the correct way
 */
class PullRequestListViewModelTest {

    @Rule
    @JvmField
    val taskExecutorRule = InstantTaskExecutorRule()

    @MockK(relaxUnitFun = true)
    private lateinit var listPullRequestsUseCase: ListPullRequestsUseCase

    private lateinit var pullRequest: PullRequest

    private lateinit var params: ListPullRequestsUseCase.Params

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        this.pullRequest = UsefulObjects.loadPullRequest()
        this.params = UsefulObjects.loadListPullRequestsUseCaseParams()
    }

    @Test
    fun testPullRequestListSuccessState() {
        val viewModel = PullRequestListViewModel(listPullRequestsUseCase)

        val testObserver = viewModel.pullRequestListState.test()
        testObserver.assertNoValue()




        val resultWrapper = UsefulObjects.loadSuccessResultWrapper()

        coEvery {
            listPullRequestsUseCase.run(params)
        } returns resultWrapper

        viewModel.loadPullRequestList("Owner", "RepoName")

        testObserver
            .assertValue {
                it is ViewStateResource.Loading
            }
            .assertHistorySize(1)
            .awaitNextValue()
            .assertHistorySize(2)
            .assertValue {
                it is ViewStateResource.Success
            }
            .assertValue {
                if (it is ViewStateResource.Success) {
                    it.data?.get(0)?.htmlUrl == pullRequest.htmlUrl
                } else {
                    false
                }
            }
    }

    @Test
    fun testPullRequestListEmptyState() {
        val viewModel = PullRequestListViewModel(listPullRequestsUseCase)

        val testObserver = viewModel.pullRequestListState.test()
        testObserver.assertNoValue()

        val emptyResultWrapper = UsefulObjects.loadEmptyResultWrapper()

        coEvery {
            listPullRequestsUseCase.run(params)
        } returns emptyResultWrapper

        viewModel.loadPullRequestList("Owner", "RepoName")

        testObserver
            .assertValue {
                it is ViewStateResource.Loading
            }
            .assertHistorySize(1)
            .awaitNextValue()
            .assertHistorySize(2)
            .assertValue {
                it is ViewStateResource.Empty
            }
    }

    @Test
    fun testPullRequestListErrorState() {
        val viewModel = PullRequestListViewModel(listPullRequestsUseCase)

        val testObserver = viewModel.pullRequestListState.test()
        testObserver.assertNoValue()

        val errorResultWrapper = UsefulObjects.loadErrorResultWrapper()

        coEvery {
            listPullRequestsUseCase.run(params)
        } returns errorResultWrapper

        viewModel.loadPullRequestList("Owner", "RepoName")

        testObserver
            .assertValue {
                it is ViewStateResource.Loading
            }
            .assertHistorySize(1)
            .awaitNextValue()
            .assertHistorySize(2)
            .assertValue {
                it is ViewStateResource.Error
            }
            .assertValue {
                if (it is ViewStateResource.Error) {
                    it.error?.errorMessage == errorResultWrapper.error?.errorMessage
                } else {
                    false
                }
            }
    }

    @Test
    fun test() {
        val viewModel = spyk(PullRequestListViewModel(listPullRequestsUseCase))

        val resultWrapper = UsefulObjects.loadSuccessResultWrapper()

        coEvery {
            listPullRequestsUseCase.run(params)
        } returns resultWrapper

        viewModel.loadPullRequestList("Owner", "RepoName")

        coVerifyOrder {
            viewModel.loadPullRequestList("Owner", "RepoName")
            viewModel.loadParams("Owner", "RepoName")
            listPullRequestsUseCase.run(UsefulObjects.loadListPullRequestsUseCaseParams())
        }
    }

    @After
    fun tearDown() {
    }
}