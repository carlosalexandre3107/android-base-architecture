package br.com.pebmed.data.repository

import br.com.pebmed.data.remote.model.response.PullRequestResponse
import br.com.pebmed.data.remote.model.response.UserResponse
import br.com.pebmed.data.remote.source.PullRequestRemoteDataSource
import br.com.pebmed.domain.base.ResultWrapper
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class PullRequestRepositoryImplTest {

    private lateinit var pullRequestRemoteDataSource: PullRequestRemoteDataSource
    private lateinit var user: UserResponse
    private lateinit var pullRequest: PullRequestResponse

    @Before
    fun setUp() {
        pullRequestRemoteDataSource = mockk()

        user = DataUsefulObjects.loadUserResponse()
        pullRequest = DataUsefulObjects.loadPullRequestResponse(user)
    }

    @Test
    fun listPullRequests() {
        coEvery {
            pullRequestRemoteDataSource.listPullRequests(any(), any())
        } returns ResultWrapper(
            success = listOf(pullRequest)
        )

        runBlocking {
            PullRequestRepositoryImpl(pullRequestRemoteDataSource).listPullRequests(
                owner = "",
                repoName = ""
            )
        }


        coVerify {
            pullRequestRemoteDataSource.listPullRequests(any(), any())
        }

        confirmVerified(pullRequestRemoteDataSource)
    }

    @Test
    fun `SHOULD call functions in the correct order`() {

        coEvery {
            pullRequestRemoteDataSource.listPullRequests(any(), any())
        } returns ResultWrapper(
            success = listOf(pullRequest)
        )

        val pullRequestRepositoryImpl =
            spyk(PullRequestRepositoryImpl(pullRequestRemoteDataSource), recordPrivateCalls = true)
        runBlocking {
            pullRequestRepositoryImpl.listPullRequests("", "")
        }

        coVerify {
            pullRequestRepositoryImpl.listPullRequests(any(), any())
            pullRequestRepositoryImpl["handleListPullRequestsSuccess"]()
            pullRequestRepositoryImpl["handleListPullRequestsError"]()
        }

        coVerifySequence {
            pullRequestRepositoryImpl.listPullRequests(any(), any())
            pullRequestRepositoryImpl["handleListPullRequestsSuccess"]()
            pullRequestRepositoryImpl["handleListPullRequestsError"]()
        }
    }

    @After
    fun tearDown() {
    }
}