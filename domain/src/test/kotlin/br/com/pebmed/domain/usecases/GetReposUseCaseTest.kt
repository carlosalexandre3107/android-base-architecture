package br.com.pebmed.domain.usecases

import br.com.pebmed.domain.base.ResultWrapper
import br.com.pebmed.domain.base.BaseErrorData
import br.com.pebmed.domain.entities.Repo
import br.com.pebmed.domain.entities.Owner
import br.com.pebmed.domain.repository.RepoRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.*
import org.mockito.Mockito.*

class GetReposUseCaseTest {

    @Mock
    lateinit var repoRepository: RepoRepository

    @Mock
    lateinit var getReposUseCase: GetReposUseCase

    @Mock
    lateinit var errorResultWrapper: ResultWrapper.Error<List<Repo>?, BaseErrorData<Void>>

    @Mock
    lateinit var successResultWrapper: ResultWrapper.Success<List<Repo>, BaseErrorData<Void>>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        this.getReposUseCase = GetReposUseCase(this.repoRepository)

        this.errorResultWrapper = ResultWrapper.Error()
        this.successResultWrapper = this.loadSuccessResultWrapper()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `SHOULD return success WHEN resultWrapper is success`()
    {
        `when`(
            runBlocking {
                repoRepository.getAllRemoteRepos(anyInt(), anyString())
            }
        ).thenReturn(
            this.successResultWrapper
        )

        runBlocking {
            val resultWrapper  = getReposUseCase.run(GetReposUseCase.Params(true))
            assert(resultWrapper is ResultWrapper.Success)
        }
    }

    @Test
    fun `SHOULD call loadRemoteData() once WHEN forceSync is true`()
    {
        `when`(
            runBlocking {
                repoRepository.getAllRemoteRepos(anyInt(), anyString())
            }
        ).thenReturn(
            this.successResultWrapper
        )

        runBlocking {
            val spiedUseCaseGetRepos = spy(getReposUseCase)
            spiedUseCaseGetRepos.run(GetReposUseCase.Params(true))
            verify(spiedUseCaseGetRepos, times(1)).loadRemoteData()
        }
    }

    @Test
    fun `SHOULD call loadLocalData() once WHEN forceSync is false`()
    {
        `when`(
            runBlocking {
                repoRepository.getAllLocalRepos()
            }
        ).thenReturn(
            errorResultWrapper
        )

        runBlocking {
            val spiedUseCaseGetRepos = spy(getReposUseCase)
            spiedUseCaseGetRepos.run(GetReposUseCase.Params(false))
            verify(spiedUseCaseGetRepos, times(1)).loadLocalData()
        }
    }

    private fun loadSuccessResultWrapper(): ResultWrapper.Success<List<Repo>, BaseErrorData<Void>> {
        val owner = Owner(
            id = 10
        )

        val repo = Repo(
            id = 12,
            owner = owner
        )

        val arrayOfRepos = ArrayList<Repo>()
        arrayOfRepos.add(repo)

        return ResultWrapper.Success(data = arrayOfRepos)
    }
}