package br.com.pebmed.domain.usecases

import br.com.pebmed.domain.base.BaseErrorData
import br.com.pebmed.domain.extensions.getCurrentDateTime
import br.com.pebmed.domain.extensions.toCacheFormat
import br.com.pebmed.domain.base.ResultWrapper
import br.com.pebmed.domain.entities.Repo
import br.com.pebmed.domain.repository.RepoRepository

class GetReposUseCase(
    private val repoRepository: RepoRepository
) : BaseUseCase<ResultWrapper<List<Repo>?, String>, GetReposUseCase.Params>() {

    override suspend fun run(params: Params): ResultWrapper<List<Repo>?, String> {
        return if (params.forceSync) {
            this.loadRemoteData()
        } else {
            this.loadLocalData()
        }
    }

    suspend fun loadRemoteData(): ResultWrapper<List<Repo>?, String> {
        val resultWrapper = repoRepository.getAllRemoteRepos(
                page = 1,
                language = "kotlin"
            )

        return when (resultWrapper) {
            is ResultWrapper.Success -> {
                this.handleRemoteSuccess(resultWrapper)
            }

            is ResultWrapper.Error -> {
                this.handleRemoteError(resultWrapper)
            }
        }
    }

    suspend fun loadLocalData(): ResultWrapper<List<Repo>?, String> {
        return when (val resultWrapper = repoRepository.getAllLocalRepos()) {
            is ResultWrapper.Success -> {
                ResultWrapper.Success(resultWrapper.data)
            }

            is ResultWrapper.Error -> {
                ResultWrapper.Error(resultWrapper.data?.errorMessage)
            }
        }
    }

    private fun handleRemoteError(
        errorResultWrapper: ResultWrapper.Error<List<Repo>, BaseErrorData<Void>>
    ): ResultWrapper.Error<List<Repo>?, String> {
        return ResultWrapper.Error(errorResultWrapper.data?.errorMessage)
    }

    fun handleRemoteSuccess(
        successResultWrapper: ResultWrapper.Success<List<Repo>, BaseErrorData<Void>>
    ): ResultWrapper.Success<List<Repo>?, String> {
        if (successResultWrapper.data.isNotEmpty()) {
            repoRepository.saveLastSyncDate(getCurrentDateTime().toCacheFormat())
        }

        return ResultWrapper.Success(successResultWrapper.data)
    }

    data class Params(val forceSync: Boolean)
}